package com.ppi115.cafefe.boundary.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;

@Provider
public class CorsResponseFilter implements ContainerResponseFilter{

    public static final String METODOS="GET, POST, PUT, DELETE, OPTIONS, HEAD";
    public final static int MAX_AGE=60 * 60 * 45;
    public final static String DEFAULT_ALLOWED_HEADERS="origin, accept, content-type, Total-Registros";
    public final static String DEFAULT_EXPOSED_HEADERS="location,info, Total-Registros, Wrong-Param, App-Error, X-Message";
    
    /**
     * Añade a la respuesta REST los encabezados que describen la política CORS.
     *
     * @param reqCtx contexto de la solicitud del que se leen las cabeceras adicionales
     * @param respCtx contexto de la respuesta cuyos encabezados se actualizan
     * @throws IOException si ocurre un error de entrada o salida al filtrar la respuesta
     */
    @Override
    public void filter(ContainerRequestContext reqCtx, ContainerResponseContext respCtx) throws IOException {
       MultivaluedMap<String, Object> headers = respCtx.getHeaders();
       headers.add("Access-Control-Allow-Origin", "*");
       headers.add("Access-Control-Allow-Headers", getCabecerasPermitidas(reqCtx));
       headers.add("Access-Control-Expose-Headers", getCabecerasExpuestas(reqCtx));
       headers.add("Access-Control-Allow-Credentials", true);
       headers.add("Access-Control-Allow-Methods", METODOS);
       headers.add("Access-Control-Max-Age", MAX_AGE);
       
    }
    
    /**
     * Combina los valores de {@code Access-Control-Allow-Headers} presentes en la
     * solicitud con las cabeceras de petición permitidas por defecto.
     *
     * @param ctx contexto de la solicitud
     * @return valor de {@code Access-Control-Allow-Headers} para la respuesta
     */
    String getCabecerasPermitidas(ContainerRequestContext ctx){
        List<String> headers= ctx.getHeaders().get("Access-Control-Allow-Headers");
        return crearListaCabeceras(headers, DEFAULT_ALLOWED_HEADERS);
    }
    
    /**
     * Combina los valores de {@code Access-Control-Expose-Headers} presentes en la
     * solicitud con las cabeceras de respuesta expuestas por defecto al navegador.
     *
     * @param ctx contexto de la solicitud
     * @return valor de {@code Access-Control-Expose-Headers} para la respuesta
     */
    String getCabecerasExpuestas(ContainerRequestContext ctx){
        List<String> headers= ctx.getHeaders().get("Access-Control-Expose-Headers");
        return crearListaCabeceras(headers, DEFAULT_EXPOSED_HEADERS);
    }
    
    
    
    
    /**
     * Concatena las cabeceras recibidas en su orden original, separadas por comas,
     * y añade al final las cabeceras predeterminadas.
     *
     * @param cabeceras valores adicionales recibidos en la solicitud
     * @param cabecerasDefault lista predeterminada de cabeceras
     * @return lista combinada, o solo la predeterminada si la lista recibida es nula o vacía
     */
    String crearListaCabeceras(List<String> cabeceras, String cabecerasDefault){
        if(cabeceras==null || cabeceras.isEmpty()){
            return cabecerasDefault;
        }
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < cabeceras.size(); i++){
        String cabecera = cabeceras.get(i);
        sb.append(cabecera);
        sb.append(",");
    }
        sb.append(cabecerasDefault);
        return sb.toString();
    }
    
}
