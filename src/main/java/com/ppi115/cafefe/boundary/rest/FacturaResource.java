package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.*;
import com.ppi115.cafefe.entity.Factura;
import jakarta.inject.Inject;
import jakarta.json.bind.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.Serializable;
import java.util.*;
import java.util.logging.*;

/**
 * Expone las operaciones REST de factura en formato JSON.
 * Ruta base:/resources/v1/factura
 */
@Path("factura")
@Produces(MediaType.APPLICATION_JSON)
public class FacturaResource implements Serializable {
    public FacturaResource() {
    }

    /** DAO que consulta y persiste los registros de este recurso. */
    @Inject FacturaDAO facturaDAO;
    /** Información de la ruta para construir la ubicación de los registros creados. */
    @Context UriInfo uriInfo;

    /**
     * Consulta una página de factura y añade el total de registros al encabezado Total-registros.
     * GET /resources/v1/factura?min=0&max=50
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
            return Response.ok(json(facturaDAO.findRange(min, max))).type(MediaType.APPLICATION_JSON)
                    .header("Total-registros", facturaDAO.contar()).build();
        } catch (Exception ex) { return error(ex); }
    }

    /**
     * Busca un registro de factura mediante su identificador UUID.
     * GET /resources/v1/factura/{id}
     *
     * @param id UUID del registro, recibido como texto en la ruta
     * @return 200 con el registro JSON; 404 si no existe, 400 si el UUID es inválido, o una respuesta de error
     */
    @GET @Path("{id}")
    public Response findById(@PathParam("id") String id) {
        try {
            Factura registro = facturaDAO.findById(UUID.fromString(id));
            return registro == null ? error(404, "No se encontró el registro")
                    : Response.ok(json(registro)).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception ex) { return error(ex); }
    }

    /** Servicio que coordina las transacciones de facturación y cobro. */
    @Inject FacturacionService facturacion;
    /** Datos JSON necesarios para facturar una orden completa. */
    public static class SolicitudFactura {
        /** Construye la solicitud vacía que JSON-B completará con los datos recibidos. */
        public SolicitudFactura() {
        }

        /** UUID de la caja existente utilizada para emitir la factura. */
        public UUID idCaja;
        /** UUID de la asignación del empleado responsable de emitir la factura. */
        public UUID idEmpleadoRol;
        /** UUID de la orden que se facturará completa. */
        public UUID idOrden;
        /** Nombre del cliente que se mostrará en la factura. */
        public String cliente;
        /** Observaciones opcionales de la operación. */
        public String observaciones;
    }
    /** Datos JSON que justifican la anulación de una factura. */
    public static class SolicitudAnulacion {
        /** Construye la solicitud vacía que JSON-B completará con los datos recibidos. */
        public SolicitudAnulacion() {
        }

        /** Motivo obligatorio que se conserva en el historial de la factura. */
        public String motivo;
    }

    /**
     * Emite la factura de una orden completa. La solicitud identifica la asignación del empleado responsable.
     * POST /resources/v1/factura
     *
     * @param solicitud caja, asignación del empleado, orden completa, cliente y observaciones de la factura; no debe ser nula
     * @return 201 con el registro creado y su ubicación en Location; 422 si faltan datos obligatorios, o una respuesta de error
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(SolicitudFactura solicitud) {
        if (solicitud == null) return error(422, "El cuerpo de la petición es obligatorio");
        try {
            Factura nueva = facturacion.emitirOrden(solicitud.idCaja, solicitud.idEmpleadoRol,
                    solicitud.cliente, solicitud.idOrden, solicitud.observaciones);
            return Response.created(uriInfo.getAbsolutePathBuilder().path(nueva.getIdFactura().toString()).build())
                    .type(MediaType.APPLICATION_JSON).entity(json(nueva)).build();
        } catch (Exception ex) { return error(ex); }
    }

    /**
     * Anula una factura indicando el motivo y conserva su historial. Rechaza la operación si existen pagos registrados.
     * POST /resources/v1/factura/{id}/anular
     *
     * @param id UUID de la factura que se desea anular
     * @param solicitud motivo de anulación que se incorporará a las observaciones
     * @return 200 con la factura anulada; 404 si no existe, 422 si falta el motivo, o una respuesta de error
     */
    @POST @Path("{id}/anular")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response anular(@PathParam("id") String id, SolicitudAnulacion solicitud) {
        if (solicitud == null) return error(422, "El motivo de anulación es obligatorio");
        try {
            UUID uuid = UUID.fromString(id);
            if (facturaDAO.findById(uuid) == null) return error(404, "No se encontró la factura");
            facturacion.anularFactura(uuid, solicitud.motivo);
            return Response.ok(json(facturaDAO.findById(uuid))).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception ex) { return error(ex); }
    }

    /**
     * Consulta el total facturado, el importe pagado y el saldo pendiente calculados por el servicio.
     * GET /resources/v1/factura/{id}/totales
     *
     * @param id UUID de la factura cuyos importes se consultan
     * @return 200 con total, pagado y saldo; 404 si no existe la factura, 400 si el UUID es inválido, o una respuesta de error
     */
    @GET @Path("{id}/totales")
    public Response totales(@PathParam("id") String id) {
        try {
            UUID uuid = UUID.fromString(id);
            if (facturaDAO.findById(uuid) == null) return error(404, "No se encontró la factura");
            return Response.ok(java.util.Map.of("total", facturacion.total(uuid),
                    "pagado", facturacion.pagado(uuid), "saldo", facturacion.saldo(uuid))).build();
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
