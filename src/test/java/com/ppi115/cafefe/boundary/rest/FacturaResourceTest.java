package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.*;
import com.ppi115.cafefe.entity.Factura;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas unitarias de consultas, emisión, anulación e importes de las facturas. */
class FacturaResourceTest extends AbstractResourceTest<Factura> {
    @Mock private FacturaDAO facturaDAO;
    @Mock private FacturacionService facturacion;
    @InjectMocks private FacturaResource recurso;

    FacturaResourceTest() { super(Factura.class, "factura"); }
    @Order(2)
    @Test void consultarPagina() throws Exception {
        var registros = List.of(entidad(idEjemplo("Factura")));
        when(facturaDAO.findRange(0, 5)).thenReturn(registros);
        when(facturaDAO.contar()).thenReturn(1);
        try (Response respuesta = recurso.findRange(0, 5)) {
            imprimirRespuesta("Listado paginado", "GET", rutaConcreta() + "?min=0&max=5", respuesta);
            assertEquals(200, respuesta.getStatus());
            assertEquals("1", respuesta.getHeaderString("Total-registros"));
            comprobarContenido(respuesta, registros);
        }
    }

    @Order(3)
    @Test void consultarPorId() throws Exception {
        UUID id = idEjemplo("Factura");
        var registro = entidad(id);
        when(facturaDAO.findById(id)).thenReturn(registro);
        try (Response respuesta = recurso.findById(id.toString())) {
            imprimirRespuesta("Consulta por ID", "GET", rutaConcreta() + "/" + id, respuesta);
            assertEquals(200, respuesta.getStatus());
            comprobarContenido(respuesta, registro);
        }
    }

    @Order(6)
    @Test void consultarRegistroInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        try (Response respuesta = recurso.findById(id.toString())) {
            imprimirRespuesta("Registro inexistente", "GET", rutaConcreta() + "/" + id, respuesta);
            comprobarError(respuesta, 404);
        }
        verify(facturaDAO).findById(id);
    }

    private FacturaResource.SolicitudFactura solicitud() {
        var solicitud = new FacturaResource.SolicitudFactura();
        solicitud.idEmpleadoRol = idEjemplo("EmpleadoRol");
        solicitud.idCaja = idEjemplo("Caja");
        solicitud.idOrden = idEjemplo("Orden");
        solicitud.cliente = "Cliente de prueba";
        solicitud.observaciones = "Orden completa";
        return solicitud;
    }

    @Order(1)
    @Test void crearFactura() throws Exception {
        var solicitud = solicitud();
        UUID id = idEjemplo("Factura");
        Factura emitida = entidad(id);
        emitida.getIdCaja().setIdCaja(solicitud.idCaja);
        emitida.getIdEmpleadoRol().setIdEmpleadoRol(solicitud.idEmpleadoRol);
        when(facturacion.emitirOrden(solicitud.idCaja, solicitud.idEmpleadoRol, solicitud.cliente,
                solicitud.idOrden, solicitud.observaciones)).thenReturn(emitida);
        prepararUbicacion();
        try (Response respuesta = recurso.crear(solicitud)) {
            imprimirRespuesta("Emitir factura", "POST", rutaConcreta(), jsonDe(solicitud), respuesta);
            comprobarContenido(respuesta, emitida);
            comprobarCreacion(respuesta, id);
        }
        verify(facturacion).emitirOrden(solicitud.idCaja, solicitud.idEmpleadoRol, solicitud.cliente,
                solicitud.idOrden, solicitud.observaciones);
        verify(facturaDAO, never()).crear(any());
    }

    @Test void rechazarFacturaSinDatos() throws Exception {
        try (Response respuesta = recurso.crear(null)) { comprobarError(respuesta, 422); }
        verifyNoInteractions(facturacion, facturaDAO);
    }

    @Test void rechazarOrdenFacturada() throws Exception {
        var solicitud = solicitud();
        when(facturacion.emitirOrden(solicitud.idCaja, solicitud.idEmpleadoRol, solicitud.cliente,
                solicitud.idOrden, solicitud.observaciones))
                .thenThrow(new DefaultDAO.DatosInvalidosException("La orden ya está facturada"));
        try (Response respuesta = recurso.crear(solicitud)) {
            comprobarError(respuesta, 422);
            assertNull(respuesta.getLocation());
        }
    }

    @Order(4)
    @Test void anularFactura() throws Exception {
        UUID id = idEjemplo("Factura");
        Factura anulada = entidad(id);
        anulada.setEstado("ANULADA");
        when(facturaDAO.findById(id)).thenReturn(new Factura(id), anulada);
        var solicitud = new FacturaResource.SolicitudAnulacion();
        solicitud.motivo = "Pedido cancelado";
        try (Response respuesta = recurso.anular(id.toString(), solicitud)) {
            imprimirRespuesta("Anular factura", "POST", rutaConcreta() + "/" + id + "/anular", jsonDe(solicitud), respuesta);
            comprobarContenido(respuesta, anulada);
            assertEquals(200, respuesta.getStatus());
            comprobarRegistro(respuesta, id);
            assertEquals("ANULADA", ((Map<?, ?>) leerJson(respuesta, Map.class)).get("estado"));
        }
        verify(facturacion).anularFactura(id, "Pedido cancelado");
    }

    @Test void rechazarAnulacionSinMotivo() throws Exception {
        try (Response respuesta = recurso.anular(UUID.randomUUID().toString(), null)) { comprobarError(respuesta, 422); }
        verifyNoInteractions(facturaDAO, facturacion);
    }

    @Test void rechazarAnulacionConIdInvalido() throws Exception {
        try (Response respuesta = recurso.anular("invalido", new FacturaResource.SolicitudAnulacion())) {
            comprobarError(respuesta, 400);
        }
        verifyNoInteractions(facturaDAO, facturacion);
    }

    @Test void rechazarAnulacionInexistente() throws Exception {
        try (Response respuesta = recurso.anular(UUID.randomUUID().toString(), new FacturaResource.SolicitudAnulacion())) {
            comprobarError(respuesta, 404);
        }
        verifyNoInteractions(facturacion);
    }

    @Test void rechazarAnulacionConPagos() throws Exception {
        UUID id = UUID.randomUUID();
        when(facturaDAO.findById(id)).thenReturn(new Factura(id));
        var solicitud = new FacturaResource.SolicitudAnulacion();
        solicitud.motivo = "Pedido cancelado";
        doThrow(new DefaultDAO.DatosInvalidosException("La factura tiene pagos registrados"))
                .when(facturacion).anularFactura(id, solicitud.motivo);
        try (Response respuesta = recurso.anular(id.toString(), solicitud)) { comprobarError(respuesta, 422); }
        verify(facturaDAO).findById(id);
    }

    @Order(5)
    @Test void consultarTotales() throws Exception {
        UUID id = idEjemplo("Factura");
        when(facturaDAO.findById(id)).thenReturn(new Factura(id));
        when(facturacion.total(id)).thenReturn(new BigDecimal("10.00"));
        when(facturacion.pagado(id)).thenReturn(new BigDecimal("4.00"));
        when(facturacion.saldo(id)).thenReturn(new BigDecimal("6.00"));
        try (Response respuesta = recurso.totales(id.toString())) {
            imprimirRespuesta("Totales de factura", "GET", rutaConcreta() + "/" + id + "/totales", respuesta);
            assertEquals(200, respuesta.getStatus());
            assertEquals(Map.of("total", new BigDecimal("10.00"), "pagado", new BigDecimal("4.00"),
                    "saldo", new BigDecimal("6.00")), respuesta.getEntity());
        }
        verify(facturacion).total(id);
        verify(facturacion).pagado(id);
        verify(facturacion).saldo(id);
    }

    @Test void rechazarTotalesSinFactura() throws Exception {
        try (Response respuesta = recurso.totales(UUID.randomUUID().toString())) { comprobarError(respuesta, 404); }
        verifyNoInteractions(facturacion);
    }

    @Test void rechazarTotalesConIdInvalido() throws Exception {
        try (Response respuesta = recurso.totales("invalido")) { comprobarError(respuesta, 400); }
        verifyNoInteractions(facturaDAO, facturacion);
    }
}
