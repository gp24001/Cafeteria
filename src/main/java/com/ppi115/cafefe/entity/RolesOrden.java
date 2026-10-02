package com.ppi115.cafefe.entity;

import java.util.Arrays;
import java.util.List;

public class RolesOrden {

    static List<String> NombreRolesOrden =Arrays.asList("Camarero","Cajero");

    public static List<String> getNombresRolesOrden() {
        return NombreRolesOrden;
    }

}