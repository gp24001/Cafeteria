package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.*;
import com.ppi115.cafefe.entity.PagoDetalle;
import com.ppi115.cafefe.entity.Pago;
import jakarta.inject.Inject;
import jakarta.json.bind.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.Serializable;
import java.util.*;
import java.util.logging.*;

/**
 * Expone las asociaciones de pagoDetalle pertenecientes a pago en formato JSON. Las consultas son de solo lectura; los detalles se registran mediante facturación o pagos.
 * Ruta base:/resources/v1/pago/{id_pago}/detalle
 */
@Path("pago/{id_pago}/detalle")
@Produces(MediaType.APPLICATION_JSON)
public class PagoDetalleResource implements Serializable {
    public PagoDetalleResource() {
    }

    /** DAO que consulta y persiste los registros de este recurso. */
    @Inject PagoDetalleDAO pagoDetalleDAO;
    /** UUID de pago que limita todas las operaciones de este recurso. */
    @PathParam("id_pago")
    String idPago;
    /** Información de la ruta para construir la ubicación de los registros creados. */
    @Context UriInfo uriInfo;

    /**
     * Consulta una página de asociaciones de la entidad indicada en la ruta y añade su total en Total-registros.
     * GET /resources/v1/pago/{id_pago}/detalle?min=0&max=50
     *
     * @param min posición inicial de la página, desde cero; por defecto es 0
     * @param max cantidad de registros solicitados, entre 1 y 50; por defecto es 50
     * @return 200 con las asociaciones y el total de esa entidad; 404 si la entidad principal no existe; 422 con Wrong-Param si el rango es inválido, o una respuesta de error
     */
    @GET
    public Response findRange(@QueryParam("min") @DefaultValue("0") int min,
            @QueryParam("max") @DefaultValue("50") int max) {
        if (min < 0 || max < 1 || max > 50) {
            return Response.status(422).header("Wrong-Param", "min,max").build();
        }
        try {
            UUID padre = obtenerPadre();
            return Response.ok(json(pagoDetalleDAO.findRangePorRelacion("idPago", padre, min, max)))
                    .type(MediaType.APPLICATION_JSON)
                    .header("Total-registros", pagoDetalleDAO.contarPorRelacion("idPago", padre)).build();
        } catch (Exception ex) { return error(ex); }
    }

    /**
     * Busca una asociación por UUID y comprueba que pertenezca a la entidad indicada en la ruta.
     * GET /resources/v1/pago/{id_pago}/detalle/{id}
     *
     * @param id UUID del registro, recibido como texto en la ruta
     * @return 200 con el registro JSON; 404 si no existe o pertenece a otra entidad, 400 si el UUID es inválido, o una respuesta de error
     */
    @GET @Path("{id}")
    public Response findById(@PathParam("id") String id) {
        try {
            UUID identificador = UUID.fromString(id);
            UUID padre = obtenerPadre();
            PagoDetalle registro = pagoDetalleDAO.findById(identificador);
            return !pertenece(registro, padre) ? error(404, "No se encontró el registro")
                    : Response.ok(json(registro)).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception ex) { return error(ex); }
    }

    /**
     * Interpreta el UUID de la entidad principal y comprueba que exista.
     * @return UUID de la entidad principal indicada en la ruta
     * @throws IllegalArgumentException si el identificador no es un UUID válido
     * @throws NotFoundException si no existe la entidad principal
     */
    private UUID obtenerPadre() {
        UUID padre = UUID.fromString(idPago);
        if (!pagoDetalleDAO.existe(Pago.class, padre)) throw new NotFoundException("No se encontró la entidad principal");
        return padre;
    }

    /**
     * Comprueba que la asociación consultada pertenezca a la entidad de la ruta.
     * @param registro asociación recuperada del pagoDetalleDAO, o null si no existe
     * @param padre UUID de la entidad principal indicada en la ruta
     * @return true si la asociación existe y pertenece a esa entidad
     */
    private boolean pertenece(PagoDetalle registro, UUID padre) {
        return registro != null && registro.getIdPago() != null
                && padre.equals(registro.getIdPago().getIdPago());
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
     * @return 404 si no existe la entidad principal, 422 para validaciones, 400 para argumentos inválidos o 500 para errores inesperados
     */
    private Response error(Exception error) {
        Throwable causa = error;
        while (causa.getCause() != null && !(causa instanceof DefaultDAO.DatosInvalidosException)
                && !(causa instanceof IllegalArgumentException)) causa = causa.getCause();
        if (causa instanceof NotFoundException) return error(404, causa.getMessage());
        if (causa instanceof DefaultDAO.DatosInvalidosException) return error(422, causa.getMessage());
        if (causa instanceof IllegalArgumentException) return error(400, causa.getMessage());
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al procesar el recurso REST", error);
        return error(500, "No se pudo completar la operación");
    }
}
