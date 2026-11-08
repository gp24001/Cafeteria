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
}
