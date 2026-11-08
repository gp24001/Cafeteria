package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** CRUD, consultas y reglas comerciales contra PostgreSQL , con rollback. */
public class CajaDAOIntegracionTest extends ComercialDAOIntegracionBase<Caja> {
    public CajaDAOIntegracionTest() { super(Caja.class, CajaDAO::new); }

    @Test
    void soloListaActivasYProtegeCajaConFacturas() {
        var dao = dao(new CajaDAO());
        Caja inactiva = registroValido(Caja.class); inactiva.setActivo(false); dao.crear(inactiva);
        em.flush();
        assertTrue(dao.findActivas().contains(caja));
        assertFalse(dao.findActivas().contains(inactiva));
        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(caja.getIdCaja()));
    }
    @Test
    void rechazaNombreVacio() {
        Caja nueva = registroValido(Caja.class); nueva.setNombre(" ");
        assertThrows(IllegalArgumentException.class, () -> dao(new CajaDAO()).crear(nueva));
    }
}
