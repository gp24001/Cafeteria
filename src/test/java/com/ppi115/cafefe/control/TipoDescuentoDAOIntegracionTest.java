package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.TipoDescuento;

/** Prueba CRUD, consultas y validación de TipoDescuentoDAO contra PostgreSQL real. */
public class TipoDescuentoDAOIntegracionTest extends DAOIntegracionBase<TipoDescuento> {
    /**
     * Inicializa TipoDescuentoDAOIntegracionTest con los datos recibidos.
     */
    public TipoDescuentoDAOIntegracionTest() { super(TipoDescuento.class, TipoDescuentoDAO::new); }
}
