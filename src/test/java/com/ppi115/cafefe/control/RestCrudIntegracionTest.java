package com.ppi115.cafefe.control;

import com.ppi115.cafefe.boundary.rest.*;
import jakarta.servlet.http.*;
import jakarta.ws.rs.core.*;
import static org.mockito.Mockito.*;
import com.ppi115.cafefe.entity.*;
import jakarta.json.bind.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Adaptador REST, JSON-B y persistencia reales, con rollback como los otros DAO. */
public class RestCrudIntegracionTest extends ComercialDAOIntegracionBase<Caja> {
    public RestCrudIntegracionTest() { super(Caja.class, CajaDAO::new); }
    private <R> R recurso(Class<R> tipo, Object repo) throws Exception {
        R r = tipo.getConstructor().newInstance();
        String entidad = tipo.getSimpleName().replace("Resource", "");
        inyectar(r, tipo, Character.toLowerCase(entidad.charAt(0)) + entidad.substring(1) + "DAO", repo);
        if (tipo == OrdenProductoResource.class) inyectar(r, tipo, "idOrden", orden.getIdOrden().toString());
        var uri = mock(UriInfo.class); when(uri.getAbsolutePathBuilder()).thenAnswer(x -> UriBuilder.fromUri("http://localhost/caja"));
        inyectar(r, tipo, "uriInfo", uri);
        return r;
    }

    @Test void paginaJsonNoRecorreRelacionesInversas() throws Exception {
        CajaResource r = recurso(CajaResource.class, dao(new CajaDAO()));
        try (Response respuesta = r.findRange(0, 2); Jsonb json = JsonbBuilder.create()) {
            assertEquals(200, respuesta.getStatus());
            String cuerpo = respuesta.getEntity().toString();
            assertTrue(json.fromJson(cuerpo, List.class).size() <= 2);
            assertFalse(cuerpo.contains("facturaList"));
        }
    }

    @Test void crudDevuelveJSONYNoDejaDatosTrasRollback() throws Exception {
        CajaResource r = recurso(CajaResource.class, dao(new CajaDAO()));
        Caja nueva = registroValido(Caja.class);
        nueva.setNombre("Caja de mostrador");
        nueva.setObservaciones("Turno de mañana");
        try (Jsonb json = JsonbBuilder.create()) {
            String id;
            try (Response respuesta = r.crear(nueva)) {
                mostrarPaso("POST", "caja", respuesta, json);
                assertEquals(201, respuesta.getStatus());
                Caja creada = json.fromJson((String) respuesta.getEntity(), Caja.class);
                id = creada.getIdCaja().toString();
                assertTrue(respuesta.getLocation().getPath().endsWith("/" + id));
            }
            String ruta = "caja/" + id;
            verificarCajaGuardada(r, id, nueva, json);

            Caja editada = new Caja();
            editada.setNombre("Caja de terraza");
            editada.setActivo(true);
            editada.setObservaciones("Turno de tarde");
            try (Response respuesta = r.actualizar(id, editada)) {
                mostrarPaso("PUT", ruta, respuesta, json);
                assertEquals(200, respuesta.getStatus());
            }
            verificarCajaGuardada(r, id, editada, json);

            try (Response respuesta = r.eliminar(id)) {
                mostrarPaso("DELETE", ruta, respuesta, json);
                assertEquals(204, respuesta.getStatus());
                assertFalse(respuesta.hasEntity());
            }
            em.flush();
            em.clear();
            try (Response respuesta = r.findById(id)) {
                mostrarPaso("GET", ruta, respuesta, json);
                assertEquals(404, respuesta.getStatus());
            }
        }
    }

    private void verificarCajaGuardada(CajaResource recurso, String id, Caja esperada, Jsonb json) {
        em.flush();
        em.clear();
        try (Response respuesta = recurso.findById(id)) {
            mostrarPaso("GET", "caja/" + id, respuesta, json);
            assertEquals(200, respuesta.getStatus());
            Caja guardada = json.fromJson((String) respuesta.getEntity(), Caja.class);
            assertEquals(id, guardada.getIdCaja().toString());
            assertEquals(esperada.getNombre(), guardada.getNombre());
            assertEquals(esperada.getActivo(), guardada.getActivo());
            assertEquals(esperada.getObservaciones(), guardada.getObservaciones());
        }
    }

    private void mostrarPaso(String metodo, String ruta, Response respuesta, Jsonb json) {
        Object entidad = respuesta.getEntity();
        String cuerpo = "(sin cuerpo)";
        if (entidad != null) cuerpo = entidad instanceof String texto ? texto : json.toJson(entidad);
        System.out.printf("%n%s /resources/v1/%s%nStatus: %d%nEntity: %s%n", metodo, ruta, respuesta.getStatus(), cuerpo);
    }

    @Test void noAceptaRangosInvalidos() throws Exception {
        CajaResource r = recurso(CajaResource.class, dao(new CajaDAO()));
        try (Response respuesta = r.findRange(-1, 5)) { assertEquals(422, respuesta.getStatus()); }
        try (Response respuesta = r.findRange(0, 0)) { assertEquals(422, respuesta.getStatus()); }
    }

    @Test void noPermiteModificarNiEliminarOrdenFacturada() throws Exception {
        servicio().emitirOrden(caja.getIdCaja(), empleadoRol.getIdEmpleadoRol(), "Cliente", orden.getIdOrden(), null);
        OrdenResource ordenes = recurso(OrdenResource.class, dao(new OrdenDAO()));
        try (Response respuesta = ordenes.eliminar(orden.getIdOrden().toString())) { assertEquals(422, respuesta.getStatus()); }
        OrdenProductoResource lineas = recurso(OrdenProductoResource.class, dao(new OrdenProductoDAO()));
        OrdenProducto editada = new OrdenProducto(linea.getIdOrdenProducto());
        editada.setIdOrden(orden); editada.setIdProducto(producto); editada.setPrecio(BigDecimal.ONE);
        try (Response respuesta = lineas.actualizar(linea.getIdOrdenProducto().toString(), editada)) {
            assertEquals(422, respuesta.getStatus());
        }
        assertEquals(0, linea.getPrecio().compareTo(new BigDecimal("2.50")));
    }

    @Test void jsonNoPermiteCrearColeccionesEnCascada() throws Exception {
        try (Jsonb json = JsonbBuilder.create()) {
            Empleado entrada = json.fromJson("{\"nombre\":\"Prueba\",\"empleadoRolList\":[{}]}", Empleado.class);
            assertNull(entrada.getEmpleadoRolList());
            Factura f = json.fromJson("{\"pagoList\":[{}],\"facturaOrdenProductoList\":[{}]}", Factura.class);
            assertNull(f.getPagoList()); assertNull(f.getFacturaOrdenProductoList());
        }
    }

    /** Comprueba con PostgreSQL el filtro, el conteo y la paginación de las siete asociaciones. */
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"ProductoTipoProducto", "ProductoCaracteristica",
            "DescuentoProducto", "EmpleadoRol", "OrdenProducto", "FacturaOrdenProducto", "PagoDetalle"})
    void consultarRelacion(String entidad) throws Exception {
        switch (entidad) {
            case "ProductoTipoProducto" -> comprobarRelacion(dao(new ProductoTipoProductoDAO()), registroValido(ProductoTipoProducto.class), "idProducto");
            case "ProductoCaracteristica" -> comprobarRelacion(dao(new ProductoCaracteristicaDAO()), registroValido(ProductoCaracteristica.class), "idProducto");
            case "DescuentoProducto" -> comprobarRelacion(dao(new DescuentoProductoDAO()), registroValido(DescuentoProducto.class), "idDescuento");
            case "EmpleadoRol" -> comprobarRelacion(dao(new EmpleadoRolDAO()), empleadoRol, "idEmpleado");
            case "OrdenProducto" -> comprobarRelacion(dao(new OrdenProductoDAO()), linea, "idOrden");
            case "FacturaOrdenProducto" -> comprobarRelacion(dao(new FacturaOrdenProductoDAO()), registroValido(FacturaOrdenProducto.class), "idFactura");
            case "PagoDetalle" -> comprobarRelacion(dao(new PagoDetalleDAO()), registroValido(PagoDetalle.class), "idPago");
            default -> throw new AssertionError(entidad);
        }
    }

    private <T> void comprobarRelacion(DefaultDAO<T> dao, T registro, String relacion) throws Exception {
        if (!em.contains(registro)) dao.crear(registro);
        var campo = registro.getClass().getDeclaredField(relacion);
        campo.setAccessible(true);
        Object padre = campo.get(registro);
        var clave = padre.getClass().getDeclaredField(relacion);
        clave.setAccessible(true);
        UUID id = (UUID) clave.get(padre);
        assertTrue(dao.existe(padre.getClass(), id));
        List<T> asociaciones = dao.findAllPorRelacion(relacion, id);
        assertTrue(asociaciones.contains(registro));
        assertEquals(asociaciones.size(), dao.contarPorRelacion(relacion, id));
        assertEquals(1, dao.findRangePorRelacion(relacion, id, 0, 1).size());
        assertEquals(asociaciones.size() - 1, dao.findRangePorRelacion(relacion, id, 1, 1000).size());
        UUID ajeno = UUID.randomUUID();
        assertEquals(List.of(), dao.findAllPorRelacion(relacion, ajeno));
        assertEquals(List.of(), dao.findRangePorRelacion(relacion, ajeno, 0, 5));
        assertEquals(0, dao.contarPorRelacion(relacion, ajeno));
        assertFalse(dao.existe(padre.getClass(), ajeno));
    }
}

