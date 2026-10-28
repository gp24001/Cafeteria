package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Rol;

/** Prueba CRUD, consultas y validación de RolDAO contra PostgreSQL real. */
public class RolDAOIntegracionTest extends DAOIntegracionBase<Rol> {
    /**
     * Inicializa RolDAOIntegracionTest con los datos recibidos.
     */
    public RolDAOIntegracionTest() { super(Rol.class, RolDAO::new); }

    /** Verifica que el selector recibe únicamente roles activos. */
    @org.junit.jupiter.api.Test
    void consultarSoloActivos() {
        var dao = dao(new RolDAO());
        var inactivo = registroValido(Rol.class);
        inactivo.setActivo(false);
        dao.crear(inactivo);
        org.junit.jupiter.api.Assertions.assertTrue(dao.findActivos().contains(rol));
        org.junit.jupiter.api.Assertions.assertFalse(dao.findActivos().contains(inactivo));
    }
}
