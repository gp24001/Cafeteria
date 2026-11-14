package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.*;
import com.ppi115.cafefe.entity.Pago;
import jakarta.inject.Inject;
import jakarta.json.bind.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.Serializable;
import java.util.*;
import java.util.logging.*;

/**
 * Expone las operaciones REST de pago en formato JSON.
 * Ruta base:/resources/v1/pago
 */
@Path("pago")
@Produces(MediaType.APPLICATION_JSON)
public class PagoResource implements Serializable {
    public PagoResource() {
    }

    /** DAO que consulta y persiste los registros de este recurso. */
    @Inject PagoDAO pagoDAO;
    /** Información de la ruta para construir la ubicación de los registros creados. */
    @Context UriInfo uriInfo;

    /**
     * Consulta una página de pago y añade el total de registros al encabezado Total-registros.
     * GET /resources/v1/pago?min=0&max=50
     *
     * @param min posición inicial de la página, desde cero; por defecto es 0
     * @param max cantidad de registros solicitados, entre 1 y 50; por defecto es 50
     * @return 200 con el arreglo JSON y el total; 422 con Wrong-Param si el rango es inválido, o una respuesta de error
     */
    @GET
    public Response findRange(@QueryParam("min") @DefaultValue("0") int min,
            @QueryParam("max") @DefaultValue("50") int max) {
        if (min < 0 || max < 1 || max > 50) {
            return Response.status(422).header("Wrong-Param", "min,max").build();
        }
        try {
            return Response.ok(json(pagoDAO.findRange(min, max))).type(MediaType.APPLICATION_JSON)
                    .header("Total-registros", pagoDAO.contar()).build();
        } catch (Exception ex) { return error(ex); }
    }

    /**
     * Busca un registro de pago mediante su identificador UUID.
     * GET /resources/v1/pago/{id}
     *
     * @param id UUID del registro, recibido como texto en la ruta
     * @return 200 con el registro JSON; 404 si no existe, 400 si el UUID es inválido, o una respuesta de error
     */
    @GET @Path("{id}")
    public Response findById(@PathParam("id") String id) {
        try {
            Pago registro = pagoDAO.findById(UUID.fromString(id));
            return registro == null ? error(404, "No se encontró el registro")
                    : Response.ok(json(registro)).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception ex) { return error(ex); }
    }

    /** Servicio que coordina las transacciones de facturación y cobro. */
    @Inject FacturacionService facturacion;
    /** Datos JSON de la factura y los detalles del pago que se desea registrar. */
    public static class SolicitudPago {
        /** Construye la solicitud vacía que JSON-B completará con los datos recibidos. */
        public SolicitudPago() {
        }

        /** UUID de la factura a la que se aplicará el pago. */
        public UUID idFactura;
        /** Detalles de los importes y medios de pago; debe contener al menos uno. */
        public List<Detalle> detalles;
        /** Observaciones opcionales de la operación. */
        public String observaciones;
    }
    /** Importe y datos de uno de los medios utilizados para realizar el pago. */
    public static class Detalle {
        /** Construye la solicitud vacía que JSON-B completará con los datos recibidos. */
        public Detalle() {
        }

        /** Importe positivo del detalle, con hasta dos decimales. */
        public java.math.BigDecimal monto;
        /** Nombre del medio de pago registrado en el detalle. */
        public String tipoPago;
        /** Referencia opcional de la transacción o comprobante externo. */
        public String referenciaExterna;
        /** Observaciones opcionales de la operación. */
        public String observaciones;
    }

    /**
     * Registra un pago con sus detalles e importes. El servicio comprueba el saldo y evita cobros superiores al pendiente.
     * POST /resources/v1/pago
     *
     * @param solicitud factura, detalles de los medios de pago y observaciones; no debe ser nula
     * @return 201 con el registro creado y su ubicación en Location; 422 si faltan datos obligatorios, o una respuesta de error
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(SolicitudPago solicitud) {
        if (solicitud == null) return error(422, "El cuerpo de la petición es obligatorio");
        try {
            java.util.List<com.ppi115.cafefe.entity.PagoDetalle> detalles = new java.util.ArrayList<>();
            if (solicitud.detalles != null) for (Detalle entrada : solicitud.detalles) {
                if (entrada == null) return error(422, "El detalle de pago es obligatorio");
                var detalle = new com.ppi115.cafefe.entity.PagoDetalle();
                detalle.setMonto(entrada.monto);
                detalle.setTipoPago(entrada.tipoPago);
                detalle.setReferenciaExterna(entrada.referenciaExterna);
                detalle.setObservaciones(entrada.observaciones);
                detalles.add(detalle);
            }
            Pago nuevo = facturacion.registrarPago(solicitud.idFactura, detalles, solicitud.observaciones);
            return Response.created(uriInfo.getAbsolutePathBuilder().path(nuevo.getIdPago().toString()).build())
                    .type(MediaType.APPLICATION_JSON).entity(json(nuevo)).build();
        } catch (Exception ex) { return error(ex); }
    }

    /**
     * Convierte los datos a JSON con sangría, incluyendo únicamente el UUID de cada relación.
     *
     * @param entidad registro, colección o valor que se desea serializar
     * @return representación JSON con saltos de línea y sangría
     * @throws RuntimeException si JSON-B no puede serializar los datos
     * @throws IllegalStateException si no se pueden liberar los recursos de JSON-B
     */
    private String json(Object entidad) {
        return RestJson.serializar(entidad);
    }

    /**
     * Construye una respuesta JSON de error y elimina saltos de línea de los encabezados App-Error y X-Message.
     *
     * @param estado código de estado HTTP que se enviará al cliente
     * @param mensaje descripción del error; si es nula se utiliza un mensaje general
     * @return respuesta con el estado recibido, los encabezados de error y un cuerpo JSON con el mensaje
     */
    private Response error(int estado, String mensaje) {
        String texto = mensaje == null ? "No se pudo completar la operación" : mensaje;
        return Response.status(estado).type(MediaType.APPLICATION_JSON)
                .header("App-Error", texto.replace('\r', ' ').replace('\n', ' '))
                .header("X-Message", texto.replace('\r', ' ').replace('\n', ' '))
                .entity(Map.of("mensaje", texto)).build();
    }

    /**
     * Traduce las excepciones de validación a estados HTTP y registra los fallos inesperados sin exponer detalles internos.
     *
     * @param error excepción capturada al consultar o modificar el recurso
     * @return 422 para validaciones del DAO, 400 para argumentos inválidos o 500 para errores inesperados
     */
    private Response error(Exception error) {
        Throwable causa = error;
        while (causa.getCause() != null && !(causa instanceof DefaultDAO.DatosInvalidosException)
                && !(causa instanceof IllegalArgumentException)) causa = causa.getCause();
        if (causa instanceof DefaultDAO.DatosInvalidosException) return error(422, causa.getMessage());
        if (causa instanceof IllegalArgumentException) return error(400, causa.getMessage());
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al procesar el recurso REST", error);
        return error(500, "No se pudo completar la operación");
    }
}
