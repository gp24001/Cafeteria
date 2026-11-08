package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** CRUD, consultas y reglas comerciales contra PostgreSQL, con rollback. */
public class FacturaDAOIntegracionTest extends ComercialDAOIntegracionBase<Factura> {
    public FacturaDAOIntegracionTest() { super(Factura.class, FacturaDAO::new); }

    @Test
    void consultaPorCajaYValidaSusReferencias() {
        var dao = dao(new FacturaDAO());
        assertTrue(dao.findByCaja(caja.getIdCaja()).contains(factura));
        assertTrue(dao.findByCaja(UUID.randomUUID()).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> dao.findByCaja(null));
        Factura nueva = registroValido(Factura.class); nueva.setIdCaja(new Caja(UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class, () -> dao.crear(nueva));
        nueva.setIdCaja(caja); caja.setActivo(false); em.flush();
        assertThrows(IllegalArgumentException.class, () -> dao.crear(nueva));
    }
    @Test
    void protegeFacturaConPagos() {
        assertThrows(IllegalArgumentException.class, () -> dao(new FacturaDAO()).eliminar(factura.getIdFactura()));
    }
}
