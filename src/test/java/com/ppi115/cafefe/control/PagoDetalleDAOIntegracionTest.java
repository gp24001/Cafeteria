package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** CRUD, consultas y reglas comerciales contra PostgreSQL, con rollback. */
public class PagoDetalleDAOIntegracionTest extends ComercialDAOIntegracionBase<PagoDetalle> {
    public PagoDetalleDAOIntegracionTest() { super(PagoDetalle.class, PagoDetalleDAO::new); }

    @Test
    void consultaPorPagoYRechazaImportesInvalidos() {
        var dao = dao(new PagoDetalleDAO());
        PagoDetalle detalle = registroValido(PagoDetalle.class); dao.crear(detalle); em.flush();
        assertTrue(dao.findByPago(pago.getIdPago()).contains(detalle));
        assertTrue(dao.findByPago(UUID.randomUUID()).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> dao.findByPago(null));
        for (BigDecimal monto : new BigDecimal[]{null, BigDecimal.ZERO, new BigDecimal("-1"), new BigDecimal("1.001"), new BigDecimal("1000000")}) {
            PagoDetalle invalido = registroValido(PagoDetalle.class); invalido.setMonto(monto);
            assertThrows(IllegalArgumentException.class, () -> dao.crear(invalido));
        }
    }
}
