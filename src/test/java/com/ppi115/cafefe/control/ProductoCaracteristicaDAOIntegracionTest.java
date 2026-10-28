package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.ProductoCaracteristica;

/** Prueba CRUD, consultas y validación de ProductoCaracteristicaDAO contra PostgreSQL real. */
public class ProductoCaracteristicaDAOIntegracionTest extends DAOIntegracionBase<ProductoCaracteristica> {
    /**
     * Inicializa ProductoCaracteristicaDAOIntegracionTest con los datos recibidos.
     */
    public ProductoCaracteristicaDAOIntegracionTest() { super(ProductoCaracteristica.class, ProductoCaracteristicaDAO::new); }
}
