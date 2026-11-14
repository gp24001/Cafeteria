package com.ppi115.cafefe.boundary.rest;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

/** Devuelve un error de cliente cuando JSON-B no puede leer el cuerpo recibido. */
@Provider
public class JsonInvalidoMapper implements ExceptionMapper<JsonbException> {
    @Override
    public Response toResponse(JsonbException error) {
        String mensaje = "El cuerpo debe contener un JSON válido";
        return Response.status(400).type(MediaType.APPLICATION_JSON)
                .header("App-Error", mensaje).header("X-Message", mensaje)
                .entity(Map.of("mensaje", mensaje)).build();
    }
}
