package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.ProductoTipoProducto;

/** Prueba CRUD, consultas y validación de ProductoTipoProductoDAO contra PostgreSQL real. */
public class ProductoTipoProductoDAOIntegracionTest extends DAOIntegracionBase<ProductoTipoProducto> {
    /**
     * Inicializa ProductoTipoProductoDAOIntegracionTest con los datos recibidos.
     */
    public ProductoTipoProductoDAOIntegracionTest() { super(ProductoTipoProducto.class, ProductoTipoProductoDAO::new); }

    /** La fecha de creación conserva el día y la hora después de leer desde PostgreSQL. */
    @org.junit.jupiter.api.Test
    void fechaCreacionConservaDiaYHora() {
        var consulta = dao(new ProductoTipoProductoDAO());
        var asignacion = registroValido(ProductoTipoProducto.class);
        var fecha = java.sql.Timestamp.valueOf("2026-10-27 14:35:12");
        asignacion.setFechaCreacion(fecha);
        consulta.crear(asignacion);
        em.flush();
        em.clear();
        org.junit.jupiter.api.Assertions.assertEquals(fecha.getTime(),
                consulta.findById(asignacion.getIdProductoTipoProducto()).getFechaCreacion().getTime());
    }
}
