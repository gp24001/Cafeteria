package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Empleado;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.*;
import static com.ppi115.cafefe.control.ValidacionComercial.*;

/** Acceso por nombre, usando solo empleados y roles existentes. */
@Stateless
@LocalBean
public class AccesoService {
    @PersistenceContext(unitName = "Cafefe-PU") private EntityManager em;

    public record Acceso(UUID idEmpleado, String nombre, Set<String> roles) implements Serializable {
        public Acceso { roles = Set.copyOf(roles); }
        public boolean administrador() { return roles.contains("administrador"); }
        public boolean comercial() { return administrador() || roles.contains("cajero"); }
        public boolean ordenes() { return administrador() || roles.contains("camarero"); }
        public boolean permite(String pagina) {
            if ("Inicio".equals(pagina)) return true;
            if ("Caja".equals(pagina)) return administrador();
            if (Set.of("Factura", "Pago").contains(pagina)) return comercial();
            if ("Orden".equals(pagina)) return ordenes();
            return administrador() && Set.of("Empleado", "Rol", "Caracteristica", "Producto", "Descuento",
                    "TipoCaracteristica", "TipoProducto", "TipoDescuento").contains(pagina);
        }
    }

    public Acceso ingresar(String nombre) {
        texto(nombre, "El nombre del empleado");
        List<Empleado> candidatos = em.createQuery("SELECT e FROM Empleado e "
                + "WHERE e.activo = true AND LOWER(TRIM(e.nombre)) = :nombre", Empleado.class)
                .setParameter("nombre", nombre.trim().toLowerCase(Locale.ROOT)).getResultList();
        exigir(candidatos.size() == 1, candidatos.isEmpty() ? "No se encontró un empleado activo con ese nombre"
                : "Hay varios empleados con ese nombre; el administrador debe diferenciarlos");
        return consultar(candidatos.getFirst().getIdEmpleado());
    }

    /** Relee los permisos en cada petición para respetar desactivaciones y cambios de rol. */
    public Acceso consultar(UUID idEmpleado) {
        Empleado empleado = referencia(em, Empleado.class, idEmpleado, "El empleado");
        em.refresh(empleado);
        exigir(Boolean.TRUE.equals(empleado.getActivo()), "El empleado está inactivo");
        Set<String> roles = new HashSet<>();
        for (String rol : em.createQuery("SELECT r.nombre FROM EmpleadoRol er JOIN er.idRol r "
                + "WHERE er.idEmpleado.idEmpleado = :id AND er.activo = true AND r.activo = true", String.class)
                .setParameter("id", idEmpleado).getResultList()) {
            String normal = Normalizer.normalize(rol == null ? "" : rol, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "").trim().toLowerCase(Locale.ROOT);
            if ("admin".equals(normal)) normal = "administrador";
            roles.add(normal);
        }
        exigir(!roles.isEmpty(), "El empleado no tiene una asignación de rol activa");
        return new Acceso(empleado.getIdEmpleado(), empleado.getNombre() + " "
                + (empleado.getApellido() == null ? "" : empleado.getApellido()), roles);
    }
}
