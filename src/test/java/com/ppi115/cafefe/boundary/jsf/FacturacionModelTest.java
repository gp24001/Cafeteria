package com.ppi115.cafefe.boundary.jsf;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FacturacionModelTest {
    @Test void quitarSegundoDetalleConservaElPrimero() {
        FacturacionModel modelo = sinVista();
        modelo.getDetalle().setMonto(new BigDecimal("1.00"));
        modelo.getDetalle().setTipoPago("EFECTIVO");
        modelo.agregarDetalle();
        var primero = modelo.getDetalles().getFirst();
        modelo.getDetalle().setMonto(new BigDecimal("2.00"));
        modelo.getDetalle().setTipoPago("TARJETA");
        modelo.agregarDetalle();
        var segundo = modelo.getDetalles().getLast();
        modelo.quitarDetalle(segundo);
        assertEquals(1, modelo.getDetalles().size());
        assertSame(primero, modelo.getDetalles().getFirst());
        assertEquals(0, modelo.getTotalDetalles().compareTo(new BigDecimal("1.00")));
    }

    @Test void importeInvalidoNoSeAgregaNiSePierdeElFormulario() {
        FacturacionModel modelo = sinVista();
        var entrada = modelo.getDetalle();
        entrada.setMonto(new BigDecimal("-1")); entrada.setTipoPago("EFECTIVO");
        modelo.agregarDetalle();
        assertTrue(modelo.getDetalles().isEmpty());
        assertSame(entrada, modelo.getDetalle());
    }

    private FacturacionModel sinVista() {
        return new FacturacionModel() {
            @Override protected jakarta.faces.context.FacesContext getFacesContext() { return null; }
        };
    }

    @Test void rechazaFraccionesDeCentavoSinRedondearElCobro() {
        var modelo = sinVista();
        modelo.getDetalle().setTipoPago("EFECTIVO");
        modelo.getDetalle().setMonto(new BigDecimal("1.005"));
        modelo.agregarDetalle();
        assertTrue(modelo.getDetalles().isEmpty());
        assertEquals(new BigDecimal("1.005"), modelo.getDetalle().getMonto());
    }

    @Test void permiteCorregirElMedioDePagoSinDuplicarElImporte() {
        var modelo = sinVista();
        modelo.getDetalle().setMonto(new BigDecimal("7.25"));
        modelo.getDetalle().setTipoPago(" ");
        modelo.agregarDetalle();
        assertTrue(modelo.getDetalles().isEmpty());
        modelo.getDetalle().setTipoPago("TARJETA");
        modelo.agregarDetalle();
        assertEquals(1, modelo.getDetalles().size());
        assertEquals(new BigDecimal("7.25"), modelo.getTotalDetalles());
        assertNull(modelo.getDetalle().getMonto());
    }

    @Test void sumaCobroMixtoConPrecisionDeCentavos() {
        var modelo = sinVista();
        modelo.getDetalle().setTipoPago("EFECTIVO");
        modelo.getDetalle().setMonto(new BigDecimal("2.10"));
        modelo.agregarDetalle();
        modelo.getDetalle().setTipoPago("TARJETA");
        modelo.getDetalle().setMonto(new BigDecimal("3.20"));
        modelo.agregarDetalle();
        assertEquals(new BigDecimal("5.30"), modelo.getTotalDetalles());
        assertEquals(2, modelo.getDetalles().size());
    }
}
