package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Flujo completo orden-factura-pago usando DAO, validación y PostgreSQL. */
public class FacturacionIntegracionTest extends ComercialDAOIntegracionBase<Factura> {
    public FacturacionIntegracionTest() { super(Factura.class, FacturaDAO::new); }

    private Factura emitir() {
        return servicio().emitirFactura(caja.getIdCaja(), empleadoRol.getIdEmpleadoRol(),
                "Cliente prueba", List.of(linea.getIdOrdenProducto()), "Integración");
    }

    private PagoDetalle importe(String valor) {
        PagoDetalle d = new PagoDetalle();
        d.setMonto(new BigDecimal(valor));
        d.setTipoPago("EFECTIVO");
        d.setReferenciaExterna("Referencia de integración");
        return d;
    }

    @Test
    void rechazaUnaParteYFacturaLaOrdenCompleta() {
        OrdenProducto segunda = registroValido(OrdenProducto.class);
        segunda.setPrecio(new BigDecimal("1.50"));
        dao(new OrdenProductoDAO()).crear(segunda); em.flush();
        FacturacionService s = servicio();
        int antes = dao(new FacturaDAO()).contar();
        assertThrows(IllegalArgumentException.class, this::emitir);
        assertEquals(antes, dao(new FacturaDAO()).contar());
        Factura completa = s.emitirOrden(caja.getIdCaja(), empleadoRol.getIdEmpleadoRol(),
                "Cliente", orden.getIdOrden(), null);
        assertEquals(2, dao(new FacturaOrdenProductoDAO()).findByFactura(completa.getIdFactura()).size());
        assertEquals(0, s.total(completa.getIdFactura()).compareTo(new BigDecimal("4.00")));
        assertFalse(s.findOrdenesPendientes().contains(orden));
    }

    @Test
    void noMezclaProductosDeDosOrdenes() {
        Orden otra = registroValido(Orden.class); dao(new OrdenDAO()).crear(otra);
        OrdenProducto ajena = registroValido(OrdenProducto.class); ajena.setIdOrden(otra);
        dao(new OrdenProductoDAO()).crear(ajena); em.flush();
        assertThrows(IllegalArgumentException.class, () -> servicio().emitirFactura(caja.getIdCaja(),
                empleadoRol.getIdEmpleadoRol(), "Cliente",
                List.of(linea.getIdOrdenProducto(), ajena.getIdOrdenProducto()), null));
    }

    @Test
    void persisteLaAsignacionSeleccionadaEnLaFactura() {
        UUID responsable = empleadoRol.getIdEmpleadoRol();
        Factura emitida = servicio().emitirOrden(caja.getIdCaja(), responsable, "Cliente", orden.getIdOrden(), null);
        UUID id = emitida.getIdFactura();
        em.flush();
        em.clear();
        assertEquals(responsable, em.find(Factura.class, id).getIdEmpleadoRol().getIdEmpleadoRol());
    }

    @Test
    void emiteConPrecioPersistidoYCobraEnDosPagos() {
        FacturacionService s = servicio();
        Factura f = emitir();
        UUID id = f.getIdFactura();
        assertEquals(0, s.total(id).compareTo(new BigDecimal("2.50")));
        Pago primero = s.registrarPago(id, List.of(importe("0.50"), importe("0.50")), "Primera entrega");
        assertEquals("PARCIAL", f.getEstado());
        assertEquals(2, dao(new PagoDetalleDAO()).findByPago(primero.getIdPago()).size());
        assertEquals(0, s.saldo(id).compareTo(new BigDecimal("1.50")));
        s.registrarPago(id, List.of(importe("1.50")), "Saldo");
        em.flush(); em.clear();
        assertEquals("PAGADA", dao(new FacturaDAO()).findById(id).getEstado());
        assertEquals(0, s.saldo(id).signum());
        assertEquals(2, dao(new PagoDAO()).findByFactura(id).size());
        assertThrows(IllegalArgumentException.class, () -> s.registrarPago(id, List.of(importe("0.01")), null));
    }

    @Test
    void evitaSobrecobrosSinGuardarPagoNiDetalles() {
        Factura f = emitir();
        FacturacionService s = servicio();
        int pagosAntes = dao(new PagoDAO()).contar();
        int detallesAntes = dao(new PagoDetalleDAO()).contar();
        assertThrows(IllegalArgumentException.class,
                () -> s.registrarPago(f.getIdFactura(), List.of(importe("2.51")), null));
        assertEquals(pagosAntes, dao(new PagoDAO()).contar());
        assertEquals(detallesAntes, dao(new PagoDetalleDAO()).contar());
        assertEquals("EMITIDA", f.getEstado());
    }

    @Test
    void rechazaLineasRepetidasInexistentesYFacturadas() {
        FacturacionService s = servicio();
        UUID id = linea.getIdOrdenProducto();
        assertThrows(IllegalArgumentException.class, () -> s.emitirFactura(caja.getIdCaja(),
                empleadoRol.getIdEmpleadoRol(), "Cliente", List.of(id, id), null));
        assertThrows(IllegalArgumentException.class, () -> s.emitirFactura(caja.getIdCaja(),
                empleadoRol.getIdEmpleadoRol(), "Cliente", List.of(UUID.randomUUID()), null));
        emitir();
        int facturasAntes = dao(new FacturaDAO()).contar();
        assertThrows(IllegalArgumentException.class, this::emitir);
        assertEquals(facturasAntes, dao(new FacturaDAO()).contar());
    }

    @Test
    void anulaSinBorrarHistorialYPermiteReemitir() {
        FacturacionService s = servicio();
        Factura f = emitir();
        s.anularFactura(f.getIdFactura(), "Error al seleccionar cliente");
        em.flush();
        assertEquals("ANULADA", f.getEstado());
        assertEquals(1, dao(new FacturaOrdenProductoDAO()).findByFactura(f.getIdFactura()).size());
        assertThrows(IllegalArgumentException.class,
                () -> s.registrarPago(f.getIdFactura(), List.of(importe("1")), null));
        assertNotNull(emitir());
    }

    @Test
    void noAnulaUnaFacturaConCobros() {
        FacturacionService s = servicio();
        Factura f = emitir();
        s.registrarPago(f.getIdFactura(), List.of(importe("1")), null);
        assertThrows(IllegalArgumentException.class, () -> s.anularFactura(f.getIdFactura(), "Prueba"));
        assertEquals("PARCIAL", f.getEstado());
    }

    @Test
    void rechazaCajaYAsignacionInactivas() {
        caja.setActivo(false); em.flush();
        assertThrows(IllegalArgumentException.class, this::emitir);
        caja.setActivo(true); empleadoRol.setActivo(false); em.flush();
        assertThrows(IllegalArgumentException.class, this::emitir);
    }

    @Test
    void conservaPrecioFacturadoAunqueCambieLaOrden() {
        Factura f = emitir();
        linea.setPrecio(new BigDecimal("10.00")); em.flush(); em.clear();
        assertEquals(0, servicio().total(f.getIdFactura()).compareTo(new BigDecimal("2.50")));
    }

    @Test
    void rollbackRevierteFacturaLineasYPagos() {
        FacturacionService s = servicio();
        Factura f = emitir();
        Pago p = s.registrarPago(f.getIdFactura(), List.of(importe("2.50")), null);
        UUID facturaId = f.getIdFactura(); UUID pagoId = p.getIdPago();
        UUID detalleId = dao(new PagoDetalleDAO()).findByPago(pagoId).getFirst().getIdPagoDetalle();
        em.flush(); em.getTransaction().rollback(); em.clear(); em.getTransaction().begin();
        assertNull(em.find(Factura.class, facturaId));
        assertNull(em.find(Pago.class, pagoId));
        assertNull(em.find(PagoDetalle.class, detalleId));
        assertTrue(dao(new FacturaOrdenProductoDAO()).findByFactura(facturaId).isEmpty());
    }

    @Test
    void rechazaImportesInvalidosYDetallesAjenos() {
        Factura f = emitir(); FacturacionService s = servicio();
        assertThrows(IllegalArgumentException.class, () -> s.registrarPago(f.getIdFactura(), List.of(), null));
        for (String valor : List.of("0", "-1", "0.001", "1000000"))
            assertThrows(IllegalArgumentException.class,
                    () -> s.registrarPago(f.getIdFactura(), List.of(importe(valor)), null));
        PagoDetalle ajeno = importe("1"); ajeno.setIdPago(pago);
        assertThrows(IllegalArgumentException.class,
                () -> s.registrarPago(f.getIdFactura(), List.of(ajeno), null));
    }
}
