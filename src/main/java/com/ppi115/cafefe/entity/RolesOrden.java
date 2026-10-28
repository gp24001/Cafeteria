package com.ppi115.cafefe.entity;

import java.util.Arrays;
import java.util.List;
/** Nombres de los roles disponibles al asignar responsables a una orden. */

public class RolesOrden {

    static List<String> NombreRolesOrden =Arrays.asList("Camarero","Cajero");

    /**
     * Obtiene nombres roles orden para la vista o la operación actual.
     * @return nombres roles orden actual
     */
    public static List<String> getNombresRolesOrden() {
        return NombreRolesOrden;
    }

}