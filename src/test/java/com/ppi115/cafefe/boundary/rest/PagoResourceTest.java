package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.*;
import com.ppi115.cafefe.entity.*;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas unitarias de consultas y registro de pagos con sus detalles. */
class PagoResourceTest extends AbstractResourceTest<Pago> {
    @Mock private PagoDAO pagoDAO;
    @Mock private FacturacionService facturacion;
    @InjectMocks private PagoResource recurso;

    PagoResourceTest() { super(Pago.class, "pago"); }
    @Order(2)
    @Test void consultarPagina() throws Exception {
        var registros = List.of(entidad(idEjemplo("Pago")));
        when(pagoDAO.findRange(0, 5)).thenReturn(registros);
        when(pagoDAO.contar()).thenReturn(1);
        try (Response respuesta = recurso.findRange(0, 5)) {
            imprimirRespuesta("Listado paginado", "GET", rutaConcreta() + "?min=0&max=5", respuesta);
            assertEquals(200, respuesta.getStatus());
            assertEquals("1", respuesta.getHeaderString("Total-registros"));
            comprobarContenido(respuesta, registros);
        }
    }

    @Order(3)
    @Test void consultarPorId() throws Exception {
        UUID id = idEjemplo("Pago");
        var registro = entidad(id);
        when(pagoDAO.findById(id)).thenReturn(registro);
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
        verify(pagoDAO).findById(id);
    }

    private PagoResource.SolicitudPago solicitud() {
        var solicitud = new PagoResource.SolicitudPago();
        solicitud.idFactura = idEjemplo("Factura");
        solicitud.observaciones = "Cobro de prueba";
        var efectivo = new PagoResource.Detalle();
        efectivo.monto = new BigDecimal("2.50");
        efectivo.tipoPago = "EFECTIVO";
        efectivo.observaciones = "Primer medio";
        var tarjeta = new PagoResource.Detalle();
        tarjeta.monto = new BigDecimal("1.25");
        tarjeta.tipoPago = "TARJETA";
        tarjeta.referenciaExterna = "REF-PRUEBA";
        solicitud.detalles = List.of(efectivo, tarjeta);
        return solicitud;
    }

    @Order(1)
    @Test void registrarPago() throws Exception {
        var solicitud = solicitud();
        UUID id = idEjemplo("Pago");
        Pago pago = entidad(id);
        pago.getIdFactura().setIdFactura(solicitud.idFactura);
        when(facturacion.registrarPago(eq(solicitud.idFactura), anyList(), eq(solicitud.observaciones)))
                .thenReturn(pago);
        prepararUbicacion();
        try (Response respuesta = recurso.crear(solicitud)) {
            imprimirRespuesta("Registrar pago", "POST", rutaConcreta(), jsonDe(solicitud), respuesta);
            comprobarContenido(respuesta, pago);
            comprobarCreacion(respuesta, id);
        }
        verify(facturacion).registrarPago(eq(solicitud.idFactura), argThat(detalles ->
                detalles.size() == 2
                && new BigDecimal("2.50").equals(detalles.get(0).getMonto())
                && "EFECTIVO".equals(detalles.get(0).getTipoPago())
                && "Primer medio".equals(detalles.get(0).getObservaciones())
                && new BigDecimal("1.25").equals(detalles.get(1).getMonto())
                && "TARJETA".equals(detalles.get(1).getTipoPago())
                && "REF-PRUEBA".equals(detalles.get(1).getReferenciaExterna())
                && detalles.stream().allMatch(d -> d.getIdPago() == null && d.getIdPagoDetalle() == null)),
                eq(solicitud.observaciones));
        verify(pagoDAO, never()).crear(any());
    }

    @Test void rechazarPagoSinDatos() throws Exception {
        try (Response respuesta = recurso.crear(null)) { comprobarError(respuesta, 422); }
        verifyNoInteractions(facturacion, pagoDAO);
    }

    @Test void rechazarDetalleNulo() throws Exception {
        var solicitud = solicitud();
        solicitud.detalles = Arrays.asList(solicitud.detalles.getFirst(), null);
        try (Response respuesta = recurso.crear(solicitud)) { comprobarError(respuesta, 422); }
        verifyNoInteractions(facturacion, pagoDAO);
    }

    @Test void rechazarPagoSinDetalles() throws Exception {
        var solicitud = solicitud();
        solicitud.detalles = null;
        when(facturacion.registrarPago(eq(solicitud.idFactura), anyList(), eq(solicitud.observaciones)))
                .thenThrow(new DefaultDAO.DatosInvalidosException("Registre al menos un detalle de pago"));
        try (Response respuesta = recurso.crear(solicitud)) { comprobarError(respuesta, 422); }
        verify(facturacion).registrarPago(solicitud.idFactura, List.of(), solicitud.observaciones);
    }

    @Test void rechazarPagoExcedido() throws Exception {
        var solicitud = solicitud();
        when(facturacion.registrarPago(eq(solicitud.idFactura), anyList(), eq(solicitud.observaciones)))
                .thenThrow(new DefaultDAO.DatosInvalidosException("El pago supera el saldo de la factura"));
        try (Response respuesta = recurso.crear(solicitud)) {
            comprobarError(respuesta, 422);
            assertNull(respuesta.getLocation());
        }
        verifyNoInteractions(pagoDAO);
    }
}
