package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.EmpleadoRol;

/** Prueba CRUD, consultas y validación de EmpleadoRolDAO contra PostgreSQL real. */
public class EmpleadoRolDAOIntegracionTest extends DAOIntegracionBase<EmpleadoRol> {
    /**
     * Inicializa EmpleadoRolDAOIntegracionTest con los datos recibidos.
     */
    public EmpleadoRolDAOIntegracionTest() { super(EmpleadoRol.class, EmpleadoRolDAO::new); }

    /** Verifica el filtro por empleado y la exclusión de asignaciones inactivas. */
    @org.junit.jupiter.api.Test
    void consultarRolesDelEmpleado() {
        var dao = dao(new EmpleadoRolDAO());
        var inactivo = registroValido(EmpleadoRol.class);
        inactivo.setActivo(false);
        dao.crear(inactivo);
        org.junit.jupiter.api.Assertions.assertTrue(dao.findByIdEmpleado(empleado.getIdEmpleado()).contains(empleadoRol));
        org.junit.jupiter.api.Assertions.assertFalse(dao.findByIdEmpleado(empleado.getIdEmpleado()).contains(inactivo));
        org.junit.jupiter.api.Assertions.assertTrue(dao.findByIdEmpleado(java.util.UUID.randomUUID()).isEmpty());
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> dao.findByIdEmpleado(null));
    }
}
