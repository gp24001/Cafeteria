package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.DescuentoProducto;

/** Prueba CRUD, consultas y validación de DescuentoProductoDAO contra PostgreSQL real. */
public class DescuentoProductoDAOIntegracionTest extends DAOIntegracionBase<DescuentoProducto> {
    /**
     * Inicializa DescuentoProductoDAOIntegracionTest con los datos recibidos.
     */
    public DescuentoProductoDAOIntegracionTest() { super(DescuentoProducto.class, DescuentoProductoDAO::new); }

    /** Comprueba la lista ofrecida por órdenes según producto, fechas y estado del tipo. */
    @org.junit.jupiter.api.Test
    void listaDisponibleRespetaProductoFechasYTipoActivo() {
        var consulta = dao(new DescuentoProductoDAO());
        var asignacion = registroValido(DescuentoProducto.class);
        var inicio = java.sql.Timestamp.valueOf("2026-10-10 12:00:00");
        var fin = java.sql.Timestamp.valueOf("2026-10-20 12:00:00");
        descuento.setFechaInicio(inicio);
        descuento.setFechaFin(fin);
        asignacion.setFechaInicio(inicio);
        asignacion.setFechaFin(fin);
        consulta.crear(asignacion);
        em.flush();
        var productos = java.util.List.of(producto);
        org.junit.jupiter.api.Assertions.assertTrue(consulta.findVigentes(productos,
                new java.util.Date(inicio.getTime() - 1000)).isEmpty());
        org.junit.jupiter.api.Assertions.assertEquals(1, consulta.findVigentes(productos, inicio).size());
        org.junit.jupiter.api.Assertions.assertEquals(1, consulta.findVigentes(productos, fin).size());
        org.junit.jupiter.api.Assertions.assertTrue(consulta.findVigentes(productos,
                new java.util.Date(fin.getTime() + 1000)).isEmpty());
        descuento.setFechaFin(null);
        asignacion.setFechaFin(null);
        em.flush();
        org.junit.jupiter.api.Assertions.assertEquals(1, consulta.findVigentes(productos,
                new java.util.Date(fin.getTime() + 1000)).size());
        descuento.getIdTipoDescuento().setActivo(false);
        em.flush();
        org.junit.jupiter.api.Assertions.assertTrue(consulta.findVigentes(productos, inicio).isEmpty());
    }
}
