package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Descuento;

/** Prueba CRUD, consultas y validación de DescuentoDAO contra PostgreSQL real. */
public class DescuentoDAOIntegracionTest extends DAOIntegracionBase<Descuento> {
    /**
     * Inicializa DescuentoDAOIntegracionTest con los datos recibidos.
     */
    public DescuentoDAOIntegracionTest() { super(Descuento.class, DescuentoDAO::new); }
}
