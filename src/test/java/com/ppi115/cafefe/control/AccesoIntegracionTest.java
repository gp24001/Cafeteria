package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccesoIntegracionTest extends IntegracionDAOBase {
    private AccesoService servicio() {
        AccesoService s = new AccesoService();
        inyectar(s, AccesoService.class, "em", em);
        return s;
    }
    private String nombreUnico() {
        String nombre = "Acceso " + UUID.randomUUID().toString().replace("-", "").replaceAll("[0-9]", "a");
        empleado.setNombre(nombre); em.flush(); return nombre;
    }

    @Test void camareroSoloAccedeAOrdenes() {
        String nombre = nombreUnico();
        var acceso = servicio().ingresar(" " + nombre.toUpperCase(Locale.ROOT) + " ");
        assertEquals(empleado.getIdEmpleado(), acceso.idEmpleado());
        assertTrue(acceso.permite("Orden"));
        assertTrue(acceso.permite("Inicio"));
        assertFalse(acceso.permite("Factura"));
        assertFalse(acceso.permite("Empleado"));
        assertFalse(acceso.permite("PaginaDesconocida"));
    }

    @Test void cajeroAccedeAlModuloComercial() {
        String nombre = nombreUnico(); rol.setNombre("Cajero"); em.flush();
        var acceso = servicio().ingresar(nombre);
        for (String pagina : List.of("Factura", "Pago")) assertTrue(acceso.permite(pagina));
        assertFalse(acceso.permite("Caja"));
        assertFalse(acceso.permite("Orden"));
        assertFalse(acceso.permite("Producto"));
    }

    @Test void administradorTieneTodosLosModulos() {
        String nombre = nombreUnico(); rol.setNombre("Administrador"); em.flush();
        var acceso = servicio().ingresar(nombre);
        for (String pagina : List.of("Caja", "Factura", "Pago", "Orden", "Empleado", "Rol", "Producto",
                "Caracteristica", "Descuento", "TipoProducto", "TipoCaracteristica", "TipoDescuento"))
            assertTrue(acceso.permite(pagina), pagina);
        assertFalse(acceso.permite("PaginaDesconocida"));
    }

    @Test void accesoSeRealizaSoloPorNombre() {
        String nombre = nombreUnico();
        assertEquals(empleado.getIdEmpleado(), servicio().ingresar(nombre).idEmpleado());
        assertThrows(IllegalArgumentException.class,
                () -> servicio().ingresar(nombre + " " + empleado.getApellido()));
    }

    @Test void baristaIngresaSinRecibirPermisosDeOtrosRoles() {
        String nombre = nombreUnico(); rol.setNombre("Barista"); em.flush();
        var acceso = servicio().ingresar(nombre);
        assertTrue(acceso.permite("Inicio"));
        assertFalse(acceso.permite("Orden"));
        assertFalse(acceso.permite("Factura"));
        assertFalse(acceso.permite("Empleado"));
    }

    @Test void rechazaNombresInexistentesOAmbiguos() {
        String nombre = nombreUnico();
        assertThrows(IllegalArgumentException.class, () -> servicio().ingresar(" "));
        assertThrows(IllegalArgumentException.class, () -> servicio().ingresar(UUID.randomUUID().toString()));
        Empleado duplicado = new Empleado(UUID.randomUUID());
        duplicado.setNombre(nombre); duplicado.setApellido("Duplicado"); duplicado.setActivo(true);
        dao(new EmpleadoDAO()).crear(duplicado); em.flush();
        assertThrows(IllegalArgumentException.class, () -> servicio().ingresar(nombre));
    }

    @Test void revocaAccesoAlDesactivarEmpleadoRolOAsignacion() {
        String nombre = nombreUnico();
        servicio().ingresar(nombre);
        empleadoRol.setActivo(false); em.flush();
        assertThrows(IllegalArgumentException.class, () -> servicio().consultar(empleado.getIdEmpleado()));
        empleadoRol.setActivo(true); rol.setActivo(false); em.flush();
        assertThrows(IllegalArgumentException.class, () -> servicio().ingresar(nombre));
        rol.setActivo(true); empleado.setActivo(false); em.flush();
        assertThrows(IllegalArgumentException.class, () -> servicio().ingresar(nombre));
    }
}
