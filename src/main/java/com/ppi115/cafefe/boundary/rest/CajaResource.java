package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.*;
import com.ppi115.cafefe.entity.Caja;
import jakarta.inject.Inject;
import jakarta.json.bind.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.Serializable;
import java.util.*;
import java.util.logging.*;

/**
 * Expone las operaciones REST de caja en formato JSON.
 * Ruta base:/resources/v1/caja
 */
@Path("caja")
@Produces(MediaType.APPLICATION_JSON)
public class CajaResource implements Serializable {
    public CajaResource() {
    }

    /** DAO que consulta y persiste los registros de este recurso. */
    @Inject CajaDAO cajaDAO;
    /** Información de la ruta para construir la ubicación de los registros creados. */
    @Context UriInfo uriInfo;

    /**
     * Consulta una página de caja y añade el total de registros al encabezado Total-registros.
     * GET /resources/v1/caja?min=0&max=50
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
            return Response.ok(json(cajaDAO.findRange(min, max))).type(MediaType.APPLICATION_JSON)
                    .header("Total-registros", cajaDAO.contar()).build();
        } catch (Exception ex) { return error(ex); }
    }

    /**
     * Busca un registro de caja mediante su identificador UUID.
     * GET /resources/v1/caja/{id}
     *
     * @param id UUID del registro, recibido como texto en la ruta
     * @return 200 con el registro JSON; 404 si no existe, 400 si el UUID es inválido, o una respuesta de error
     */
    @GET @Path("{id}")
    public Response findById(@PathParam("id") String id) {
        try {
            Caja registro = cajaDAO.findById(UUID.fromString(id));
            return registro == null ? error(404, "No se encontró el registro")
                    : Response.ok(json(registro)).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception ex) { return error(ex); }
    }

    /**
     * Crea un registro de caja con un UUID generado en el servidor y las validaciones del DAO.
     * POST /resources/v1/caja
     *
     * @param nuevo datos JSON del registro que se desea crear; no debe ser nulo
     * @return 201 con el registro creado y su ubicación en Location; 422 si faltan datos obligatorios, o una respuesta de error
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(Caja nuevo) {
        if (nuevo == null) return error(422, "El cuerpo de la petición es obligatorio");
        try {
            nuevo.setIdCaja(UUID.randomUUID());
            cajaDAO.crear(nuevo);
            return Response.created(uriInfo.getAbsolutePathBuilder().path(nuevo.getIdCaja().toString()).build())
                    .type(MediaType.APPLICATION_JSON).entity(json(nuevo)).build();
        } catch (Exception ex) { return error(ex); }
    }

    /**
     * Actualiza un registro de caja conservando el UUID indicado en la ruta y aplicando las validaciones del DAO.
     * PUT /resources/v1/caja/{id}
     *
     * @param id UUID del registro que se desea modificar
     * @param modificado datos JSON que reemplazan los valores editables del registro; no debe ser nulo
     * @return 200 con el registro actualizado; 404 si no existe, 400 si el UUID es inválido, 422 ante datos inválidos, o una respuesta de error
     */
    @PUT @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") String id, Caja modificado) {
        if (modificado == null) return error(422, "El cuerpo de la petición es obligatorio");
        try {
            UUID identificador = UUID.fromString(id);
            Caja actual = cajaDAO.findById(identificador);
            if (actual == null) return error(404, "No se encontró el registro");
            modificado.setIdCaja(identificador);
            cajaDAO.modificar(modificado);
            return Response.ok(json(cajaDAO.findById(identificador))).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception ex) { return error(ex); }
    }

    /**
     * Elimina el registro de caja si existe y sus relaciones y reglas de negocio permiten la operación.
     * DELETE /resources/v1/caja/{id}
     *
     * @param id UUID del registro que se desea eliminar
     * @return 204 sin cuerpo al eliminar; 404 si no existe, 400 si el UUID es inválido, o una respuesta de error
     */
    @DELETE @Path("{id}")
    public Response eliminar(@PathParam("id") String id) {
        try {
            UUID identificador = UUID.fromString(id);
            Caja actual = cajaDAO.findById(identificador);
            if (actual == null) return error(404, "No se encontró el registro");
            cajaDAO.eliminar(identificador);
            return Response.noContent().build();
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
