package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Empleado;

/** Prueba CRUD, consultas y validación de EmpleadoDAO contra PostgreSQL real. */
public class EmpleadoDAOIntegracionTest extends DAOIntegracionBase<Empleado> {
    /**
     * Inicializa EmpleadoDAOIntegracionTest con los datos recibidos.
     */
    public EmpleadoDAOIntegracionTest() { super(Empleado.class, EmpleadoDAO::new); }

    /** Verifica que la consulta excluye empleados inactivos. */
    @org.junit.jupiter.api.Test
    void consultarSoloActivos() {
        var dao = dao(new EmpleadoDAO());
        var inactivo = registroValido(Empleado.class);
        inactivo.setActivo(false);
        dao.crear(inactivo);
        org.junit.jupiter.api.Assertions.assertTrue(dao.findActivos().contains(empleado));
        org.junit.jupiter.api.Assertions.assertFalse(dao.findActivos().contains(inactivo));
    }
}
