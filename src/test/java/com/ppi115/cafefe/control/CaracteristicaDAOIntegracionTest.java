package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Caracteristica;

/** Prueba CRUD, consultas y validación de CaracteristicaDAO contra PostgreSQL real. */
public class CaracteristicaDAOIntegracionTest extends DAOIntegracionBase<Caracteristica> {
    /**
     * Inicializa CaracteristicaDAOIntegracionTest con los datos recibidos.
     */
    public CaracteristicaDAOIntegracionTest() { super(Caracteristica.class, CaracteristicaDAO::new); }
}
