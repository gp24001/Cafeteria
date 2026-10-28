package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.TipoCaracteristica;

/** Prueba CRUD, consultas y validación de TipoCaracteristicaDAO contra PostgreSQL real. */
public class TipoCaracteristicaDAOIntegracionTest extends DAOIntegracionBase<TipoCaracteristica> {
    /**
     * Inicializa TipoCaracteristicaDAOIntegracionTest con los datos recibidos.
     */
    public TipoCaracteristicaDAOIntegracionTest() { super(TipoCaracteristica.class, TipoCaracteristicaDAO::new); }

    /** Comprueba que un formato inválido no llega a la base de datos. */
    @org.junit.jupiter.api.Test
    void rechazarExpresionRegularInvalida() {
        var registro = registroValido(TipoCaracteristica.class);
        registro.setExpresionRegular("[");
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> dao(new TipoCaracteristicaDAO()).crear(registro));
    }
}
