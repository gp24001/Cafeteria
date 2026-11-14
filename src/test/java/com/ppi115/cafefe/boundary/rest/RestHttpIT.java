package com.ppi115.cafefe.boundary.rest;

import jakarta.json.bind.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

/** Ejecutar explícitamente con -Dtest=RestHttpIT, después de desplegar el WAR en Liberty. */
class RestHttpIT {
    private static final String BASE = System.getProperty("cafefe.http.url", "http://127.0.0.1:9080/cafefe-1.0-SNAPSHOT/");
    private static HttpClient cliente;
    private static Jsonb json;

    static Stream<String> entidades() {
        return Stream.of("Caja", "Caracteristica", "Descuento", "DescuentoProducto",
            "Empleado", "EmpleadoRol", "Factura", "FacturaOrdenProducto", "Orden", "OrdenProducto",
            "Pago", "PagoDetalle", "Producto", "ProductoCaracteristica", "ProductoTipoProducto", "Rol",
            "TipoCaracteristica", "TipoDescuento", "TipoProducto");
    }

    static HttpResponse<String> api(String metodo, String ruta, String cuerpo) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(BASE + "resources/v1/" + ruta))
                .timeout(Duration.ofSeconds(25)).header("Accept", "application/json");
        if (cuerpo != null) builder.header("Content-Type", "application/json");
        builder.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(cuerpo));
        return cliente.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @BeforeAll static void preparar() {
        json = JsonbBuilder.create();
        cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }
    @AfterAll static void cerrar() throws Exception {
        cliente.close();
        json.close();
    }

    private record Ruta(String valor, boolean padreExiste) { }

    private static Ruta resolverRuta(String nombre) throws Exception {
        Class<?> tipo = Class.forName("com.ppi115.cafefe.boundary.rest." + nombre + "Resource");
        String ruta = tipo.getAnnotation(jakarta.ws.rs.Path.class).value();
        if (!ruta.contains("{")) return new Ruta(ruta, true);
        var campo = Arrays.stream(tipo.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(jakarta.ws.rs.PathParam.class)).findFirst().orElseThrow();
        String padre = campo.getName().substring(2);
        padre = Character.toLowerCase(padre.charAt(0)) + padre.substring(1);
        var respuesta = api("GET", padre + "?max=1", null);
        assertEquals(200, respuesta.statusCode(), respuesta.body());
        var padres = json.fromJson(respuesta.body(), List.class);
        String id = padres.isEmpty() ? UUID.randomUUID().toString()
                : String.valueOf(((Map<?, ?>) padres.getFirst()).get(campo.getName()));
        return new Ruta(ruta.replace("{" + campo.getAnnotation(jakarta.ws.rs.PathParam.class).value() + "}", id), !padres.isEmpty());
    }

    @Test void consultarRelacionesPublicas() throws Exception {
        for (String nombre : List.of("ProductoTipoProducto", "ProductoCaracteristica", "DescuentoProducto")) {
            var ruta = resolverRuta(nombre);
            var respuesta = api("GET", ruta.valor(), null);
            assertEquals(ruta.padreExiste() ? 200 : 404, respuesta.statusCode(), respuesta.body());
        }
    }

    @Test void rechazarRutasAnteriores() throws Exception {
        for (String nombre : List.of("ProductoTipoProducto", "ProductoCaracteristica", "DescuentoProducto",
                "EmpleadoRol", "OrdenProducto", "FacturaOrdenProducto", "PagoDetalle")) {
            String ruta = Character.toLowerCase(nombre.charAt(0)) + nombre.substring(1);
            assertEquals(404, api("GET", ruta, null).statusCode(), ruta);
        }
    }

    @ParameterizedTest @MethodSource("entidades")
    void contratoHTTPDeCadaEntidad(String nombre) throws Exception {
        Ruta rutaResuelta = resolverRuta(nombre);
        String ruta = rutaResuelta.valor();
        var pagina = api("GET", ruta + "?min=0&max=2", null);
        assertEquals(rutaResuelta.padreExiste() ? 200 : 404, pagina.statusCode(), nombre + ": " + pagina.body());
        if (!rutaResuelta.padreExiste()) {
            assertTrue(pagina.headers().firstValue("App-Error").isPresent());
            return;
        }
        assertTrue(pagina.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        var registros = json.fromJson(pagina.body(), List.class);
        assertTrue(registros.size() <= 2);
        if (!registros.isEmpty()) {
            var primero = (java.util.Map<?, ?>) registros.getFirst();
            for (var entrada : primero.entrySet()) {
                if (entrada.getValue() instanceof Map<?, ?> referencia) {
                    assertEquals(1, referencia.size(), "La relación solo debe contener su UUID");
                    assertTrue(referencia.containsKey(entrada.getKey()));
                    assertDoesNotThrow(() -> UUID.fromString(referencia.get(entrada.getKey()).toString()));
                }
            }
            String clave = "id" + nombre;
            var registro = api("GET", ruta + "/" + primero.get(clave), null);
            assertEquals(200, registro.statusCode(), nombre + ": " + registro.body());
            assertEquals(primero.get(clave), json.fromJson(registro.body(), java.util.Map.class).get(clave));
        }
        assertTrue(Integer.parseInt(pagina.headers().firstValue("Total-registros").orElseThrow()) >= 0);
        assertEquals(404, api("GET", ruta + "/" + UUID.randomUUID(), null).statusCode(), nombre);
        assertEquals(400, api("GET", ruta + "/uuid-invalido", null).statusCode(), nombre);
        var rango = api("GET", ruta + "?min=-1&max=0", null);
        assertEquals(422, rango.statusCode(), nombre + ": " + rango.body());
        assertEquals("min,max", rango.headers().firstValue("Wrong-Param").orElseThrow());
        if (!Set.of("FacturaOrdenProducto", "PagoDetalle").contains(nombre))
            assertEquals(422, api("POST", ruta, "null").statusCode(), nombre);
    }

    @Test void consultarTiposDeDescuento() throws Exception {
        var respuesta = api("GET", "tipoDescuento?min=0&max=2", null);
        System.out.printf("GET %s%nHTTP %d%n%s%n",
                respuesta.uri(), respuesta.statusCode(), respuesta.body());
        assertEquals(200, respuesta.statusCode());
        assertTrue(json.fromJson(respuesta.body(), List.class).size() <= 2);
    }

    @Test void consultasYValidacionesDeFormatoHttp() throws Exception {
        assertEquals(200, api("GET", "empleado", null).statusCode());
        assertEquals(422, api("POST", "producto", "null").statusCode());
        // Liberty rechaza la conversión del QueryParam antes de invocar el recurso.
        assertEquals(404, api("GET", "tipoDescuento?min=texto&max=2", null).statusCode());
        assertEquals(400, api("POST", "producto", "{json-invalido").statusCode());
    }

    @Test void detallesSeConsultanPeroNoSeEscribenAislados() throws Exception {
        for (String nombre : List.of("FacturaOrdenProducto", "PagoDetalle")) {
            String ruta = resolverRuta(nombre).valor();
            assertEquals(405, api("POST", ruta, "{}").statusCode());
            assertEquals(405, api("DELETE", ruta + "/" + UUID.randomUUID(), null).statusCode());
        }
    }

    @Test void validacionesRechazanDatosSinGuardarlos() throws Exception {
        var antes = api("GET", "producto?min=0&max=1", null).headers().firstValue("Total-registros").orElseThrow();
        var invalido = api("POST", "producto", "{\"nombre\":\"Producto inválido REST\",\"activo\":true,\"precioSugerido\":-1}");
        assertEquals(422, invalido.statusCode(), invalido.body());
        var despues = api("GET", "producto?min=0&max=1", null).headers().firstValue("Total-registros").orElseThrow();
        assertEquals(antes, despues);
        assertEquals(422, api("POST", "factura", "{\"cliente\":\"Prueba\",\"idCaja\":\""
                + UUID.randomUUID() + "\",\"idOrden\":\"" + UUID.randomUUID() + "\"}").statusCode());
        assertEquals(422, api("POST", "pago", "{\"idFactura\":\"" + UUID.randomUUID()
                + "\",\"detalles\":[{\"monto\":1,\"tipoPago\":\"EFECTIVO\"}]}").statusCode());
    }

}
