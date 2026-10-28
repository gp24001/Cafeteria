package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.OrdenProducto;

/** Prueba CRUD, consultas y validación de OrdenProductoDAO contra PostgreSQL real. */
public class OrdenProductoDAOIntegracionTest extends DAOIntegracionBase<OrdenProducto> {
    /**
     * Inicializa OrdenProductoDAOIntegracionTest con los datos recibidos.
     */
    public OrdenProductoDAOIntegracionTest() { super(OrdenProducto.class, OrdenProductoDAO::new); }

    /** Verifica que las líneas se consultan por orden con su producto disponible. */
    @org.junit.jupiter.api.Test
    void consultarLineasDeLaOrden() {
        var dao = dao(new OrdenProductoDAO());
        var linea = registroValido(OrdenProducto.class);
        dao.crear(linea);
        em.flush();
        em.clear();
        var lineas = dao.findByOrden(orden.getIdOrden());
        org.junit.jupiter.api.Assertions.assertEquals(1, lineas.size());
        org.junit.jupiter.api.Assertions.assertEquals(producto.getNombre(), lineas.get(0).getIdProducto().getNombre());
        org.junit.jupiter.api.Assertions.assertTrue(dao.findByOrden(java.util.UUID.randomUUID()).isEmpty());
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> dao.findByOrden(null));
    }
}
