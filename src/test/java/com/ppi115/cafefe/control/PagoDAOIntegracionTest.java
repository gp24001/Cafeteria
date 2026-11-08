package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** CRUD, consultas y reglas comerciales contra PostgreSQL, con rollback. */
public class PagoDAOIntegracionTest extends ComercialDAOIntegracionBase<Pago> {
    public PagoDAOIntegracionTest() { super(Pago.class, PagoDAO::new); }

    @Test
    void consultaPorFacturaYRechazaAnuladas() {
        var dao = dao(new PagoDAO());
        assertTrue(dao.findByFactura(factura.getIdFactura()).contains(pago));
        assertTrue(dao.findByFactura(UUID.randomUUID()).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> dao.findByFactura(null));
        factura.setEstado("ANULADA"); em.flush();
        assertThrows(IllegalArgumentException.class, () -> dao.crear(registroValido(Pago.class)));
    }
    @Test
    void protegePagoConDetalles() {
        dao(new PagoDetalleDAO()).crear(registroValido(PagoDetalle.class)); em.flush();
        assertThrows(IllegalArgumentException.class, () -> dao(new PagoDAO()).eliminar(pago.getIdPago()));
    }
}
