package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Producto;

/** Prueba CRUD, consultas y validación de ProductoDAO contra PostgreSQL real. */
public class ProductoDAOIntegracionTest extends DAOIntegracionBase<Producto> {
    /**
     * Inicializa ProductoDAOIntegracionTest con los datos recibidos.
     */
    public ProductoDAOIntegracionTest() { super(Producto.class, ProductoDAO::new); }
}
