package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** CRUD, consultas y reglas comerciales contra PostgreSQL, con rollback. */
public class FacturaOrdenProductoDAOIntegracionTest extends ComercialDAOIntegracionBase<FacturaOrdenProducto> {
    public FacturaOrdenProductoDAOIntegracionTest() { super(FacturaOrdenProducto.class, FacturaOrdenProductoDAO::new); }

    @Test
    void consultaPorFacturaYRechazaFacturarDosVeces() {
        var dao = dao(new FacturaOrdenProductoDAO());
        FacturaOrdenProducto detalle = registroValido(FacturaOrdenProducto.class); dao.crear(detalle); em.flush();
        assertTrue(dao.findByFactura(factura.getIdFactura()).contains(detalle));
        assertTrue(dao.findByFactura(UUID.randomUUID()).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> dao.findByFactura(null));
        assertThrows(IllegalArgumentException.class, () -> dao.crear(registroValido(FacturaOrdenProducto.class)));
    }
}
