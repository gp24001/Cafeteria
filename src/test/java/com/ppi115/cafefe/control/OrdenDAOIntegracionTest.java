package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Orden;

/** Prueba CRUD, consultas y validación de OrdenDAO contra PostgreSQL real. */
public class OrdenDAOIntegracionTest extends DAOIntegracionBase<Orden> {
    /**
     * Inicializa OrdenDAOIntegracionTest con los datos recibidos.
     */
    public OrdenDAOIntegracionTest() { super(Orden.class, OrdenDAO::new); }

    /** Comprueba que eliminar una orden también elimina sus líneas. */
    @org.junit.jupiter.api.Test
    void eliminarOrdenConSusProductos() {
        var linea = registroValido(com.ppi115.cafefe.entity.OrdenProducto.class);
        dao(new OrdenProductoDAO()).crear(linea);
        var dao = dao(new OrdenDAO());
        dao.eliminar(orden.getIdOrden());
        em.flush();
        em.clear();
        org.junit.jupiter.api.Assertions.assertNull(dao.findById(orden.getIdOrden()));
        org.junit.jupiter.api.Assertions.assertNull(em.find(com.ppi115.cafefe.entity.OrdenProducto.class, linea.getIdOrdenProducto()));
    }

    /** Comprueba la consulta de empleados con el rol Camarero. */
    @org.junit.jupiter.api.Test
    void consultarCamarerosActivos() {
        rol.setNombre("Camarero");
        dao(new RolDAO()).modificar(rol);
        var dao = dao(new OrdenDAO());
        org.junit.jupiter.api.Assertions.assertTrue(dao.findCamareros().contains(empleadoRol));
        empleadoRol.setActivo(false);
        dao(new EmpleadoRolDAO()).modificar(empleadoRol);
        org.junit.jupiter.api.Assertions.assertFalse(dao.findCamareros().contains(empleadoRol));
    }
}
