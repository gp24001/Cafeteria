package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.TipoProducto;

/** Prueba CRUD, consultas y validación de TipoProductoDAO contra PostgreSQL real. */
public class TipoProductoDAOIntegracionTest extends DAOIntegracionBase<TipoProducto> {
    /**
     * Inicializa TipoProductoDAOIntegracionTest con los datos recibidos.
     */
    public TipoProductoDAOIntegracionTest() { super(TipoProducto.class, TipoProductoDAO::new); }
}
