package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.DefaultDAO;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.persistence.Id;
import jakarta.ws.rs.core.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Datos de ejemplo y comprobaciones JSON compartidos por las pruebas de los recursos REST. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
abstract class AbstractResourceTest<T> {
    protected final Class<T> tipoEntidad;
    protected final String ruta;
    @Mock protected UriInfo uriInfo;
    protected final UUID idPadre;

    protected AbstractResourceTest(Class<T> tipoEntidad, String ruta) {
        this.tipoEntidad = tipoEntidad;
        this.ruta = ruta;
        String padre = ruta.split("/")[0];
        this.idPadre = ruta.contains("{") ? idEjemplo(Character.toUpperCase(padre.charAt(0)) + padre.substring(1)) : null;
    }

    private static final JsonObject EJEMPLOS = cargarEjemplos();

    private static JsonObject cargarEjemplos() {
        try (var lector = Json.createReader(Objects.requireNonNull(
                AbstractResourceTest.class.getResourceAsStream("/ejemplos-rest.json")))) {
            return lector.readObject();
        }
    }

    protected static UUID idEjemplo(String nombre) {
        return UUID.fromString(EJEMPLOS.getJsonObject(nombre).getString("id" + nombre));
    }

    /** Datos aislados de una sola prueba. Cada lectura refleja la última escritura del recurso. */
    protected Map<UUID, T> simularAlmacen(DefaultDAO<T> dao) {
        Map<UUID, T> registros = new LinkedHashMap<>();
        doAnswer(llamada -> {
            T nuevo = llamada.getArgument(0);
            UUID id = (UUID) campoId().get(nuevo);
            assertNotNull(id);
            assertNull(registros.putIfAbsent(id, nuevo));
            return null;
        }).when(dao).crear(any());
        doAnswer(llamada -> {
            T editado = llamada.getArgument(0);
            assertNotNull(registros.replace((UUID) campoId().get(editado), editado));
            return null;
        }).when(dao).modificar(any());
        doAnswer(llamada -> {
            assertNotNull(registros.remove(llamada.getArgument(0)));
            return null;
        }).when(dao).eliminar(any());
        when(dao.findById(any(UUID.class))).thenAnswer(llamada -> registros.get(llamada.getArgument(0)));
        return registros;
    }

    protected String jsonDe(Object valor) throws Exception {
        try (var json = JsonbBuilder.create(new JsonbConfig().withFormatting(true))) {
            return json.toJson(valor);
        }
    }

    protected void comprobarContenido(Response respuesta, Object esperado) throws Exception {
        try (var lector = Json.createReader(new java.io.StringReader(RestJson.serializar(esperado)));
             var recibido = Json.createReader(new java.io.StringReader((String) respuesta.getEntity()))) {
            assertEquals(lector.readValue(), recibido.readValue());
        }
    }

    protected T entidad(UUID id) throws Exception {
        T registro;
        try (var json = JsonbBuilder.create()) {
            registro = json.fromJson(EJEMPLOS.getJsonObject(tipoEntidad.getSimpleName()).toString(), tipoEntidad);
        }
        campoId().set(registro, id);
        return registro;
    }

    protected Field campoId() {
        Field campo = Arrays.stream(tipoEntidad.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class)).findFirst().orElseThrow();
        campo.setAccessible(true);
        return campo;
    }

    protected void imprimirRespuesta(String caso, String metodo, String destino, Response respuesta) throws Exception {
        imprimirRespuesta(caso, metodo, destino, null, respuesta);
    }

    protected void imprimirRespuesta(String caso, String metodo, String destino, String solicitud, Response respuesta) throws Exception {
        Object entidad = respuesta.getEntity();
        String cuerpo;
        if (entidad == null) {
            cuerpo = "(sin cuerpo)";
        } else if (entidad instanceof String texto) {
            cuerpo = texto;
        } else {
            try (var json = JsonbBuilder.create()) {
                cuerpo = json.toJson(entidad);
            }
        }
        System.out.printf("%n=== %s | %s ===%n%s /resources/v1/%s%n", tipoEntidad.getSimpleName(), caso, metodo, destino);
        if (solicitud != null) System.out.println("Solicitud: " + solicitud);
        System.out.println("Status: " + respuesta.getStatus());
        for (String encabezado : List.of("Location", "Total-registros")) {
            String valor = respuesta.getHeaderString(encabezado);
            if (valor != null) System.out.println(encabezado + ": " + valor);
        }
        System.out.println("Entity: " + cuerpo);
    }

    protected Object leerJson(Response respuesta, Class<?> tipo) throws Exception {
        try (var jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(respuesta.getEntity().toString(), tipo);
        }
    }

    protected void prepararUbicacion() {
        when(uriInfo.getAbsolutePathBuilder()).thenAnswer(i ->
                UriBuilder.fromUri("http://localhost/resources/v1/" + rutaConcreta()));
    }

    protected void comprobarCreacion(Response respuesta, UUID id) throws Exception {
        assertEquals(201, respuesta.getStatus());
        assertEquals(URI.create("http://localhost/resources/v1/" + rutaConcreta() + "/" + id), respuesta.getLocation());
        comprobarRegistro(respuesta, id);
    }

    protected void comprobarRegistro(Response respuesta, UUID id) throws Exception {
        assertEquals(MediaType.APPLICATION_JSON_TYPE, respuesta.getMediaType());
        Map<?, ?> cuerpo = (Map<?, ?>) leerJson(respuesta, Map.class);
        assertEquals(id.toString(), cuerpo.get(campoId().getName()));
    }

    protected void comprobarError(Response respuesta, int estado) {
        assertEquals(estado, respuesta.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, respuesta.getMediaType());
        assertNotNull(respuesta.getHeaderString("App-Error"));
        assertEquals(respuesta.getHeaderString("App-Error"), respuesta.getHeaderString("X-Message"));
        assertTrue(respuesta.getEntity() instanceof Map);
        assertTrue(((Map<?, ?>) respuesta.getEntity()).containsKey("mensaje"));
    }

    protected String rutaConcreta() {
        if (idPadre == null) return ruta;
        return ruta.replaceFirst("\\{[^}]+\\}", idPadre.toString());
    }

    protected void comprobarIdDeRutaEnEdicion(DefaultDAO<T> dao,
            BiFunction<String, T, Response> actualizar) throws Exception {
        UUID idRuta = UUID.randomUUID();
        UUID idCuerpo = UUID.randomUUID();
        T cambios = entidad(idCuerpo);
        when(dao.findById(idRuta)).thenReturn(entidad(idRuta), cambios);

        doAnswer(llamada -> {
            assertEquals(idRuta, campoId().get(llamada.getArgument(0)));
            return null;
        }).when(dao).modificar(any());

        try (Response respuesta = actualizar.apply(idRuta.toString(), cambios)) {
            assertEquals(200, respuesta.getStatus());
            comprobarRegistro(respuesta, idRuta);
        }
        verify(dao).modificar(cambios);
        verify(dao, never()).findById(idCuerpo);
    }

    protected void comprobarEdicionInexistente(DefaultDAO<T> dao,
            BiFunction<String, T, Response> actualizar) throws Exception {
        UUID id = UUID.randomUUID();
        try (Response respuesta = actualizar.apply(id.toString(), entidad(id))) {
            comprobarError(respuesta, 404);
        }
        verify(dao).findById(id);
        verify(dao, never()).modificar(any());
        verify(dao, never()).crear(any());
    }

    protected void comprobarBorradoConRelaciones(DefaultDAO<T> dao,
            Function<String, Response> eliminar) throws Exception {
        UUID id = UUID.randomUUID();
        when(dao.findById(id)).thenReturn(entidad(id));
        String motivo = "El registro tiene datos relacionados";
        doThrow(new DefaultDAO.DatosInvalidosException(motivo)).when(dao).eliminar(id);

        try (Response respuesta = eliminar.apply(id.toString())) {
            comprobarError(respuesta, 422);
            assertEquals(motivo, ((Map<?, ?>) respuesta.getEntity()).get("mensaje"));
        }
    }
}
