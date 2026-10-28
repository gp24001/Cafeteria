package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.EmpleadoRol;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

/** Acceso a datos de asignación de rol a empleado. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class EmpleadoRolDAO extends DefaultDAO<EmpleadoRol> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa EmpleadoRolDAO con los datos recibidos.
     */
    public EmpleadoRolDAO() {
        super(EmpleadoRol.class);
    }

    /**
     * Proporciona el contexto de persistencia administrado por Jakarta EE.
     * @return entity manager actual
     */
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Consulta las asignaciones activas del empleado indicado.
     * @param idEmpleado identificador del empleado
     * @return lista de registros que cumplen la consulta
     */
    public List<EmpleadoRol> findByIdEmpleado(UUID idEmpleado) {
        try {
            if (idEmpleado == null) throw new DatosInvalidosException("El ID del empleado es obligatorio");
            return em.createQuery(
                    "SELECT er FROM EmpleadoRol er "
                    + "WHERE er.idEmpleado.idEmpleado = :idEmpleado "
                    + "AND er.activo = true",
                    EmpleadoRol.class)
                    .setParameter("idEmpleado", idEmpleado)
                    .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Comprueba las restricciones y las reglas de la entidad antes de escribir en la base de datos.
     * @param r registro que se desea validar o editar
     */
    @Override
    protected void validar(EmpleadoRol r) {
        super.validar(r);
        // Las relaciones se eligen al crear; al editar se conservan las ya persistidas.
        EmpleadoRol anterior = em.find(EmpleadoRol.class, r.getIdEmpleadoRol());
        if (anterior != null && !java.util.Objects.equals(
                anterior.getIdEmpleado() == null ? null : anterior.getIdEmpleado().getIdEmpleado(),
                r.getIdEmpleado() == null ? null : r.getIdEmpleado().getIdEmpleado()))
            throw new DatosInvalidosException("Las relaciones no pueden cambiarse durante la edición");
        if (anterior != null && !java.util.Objects.equals(
                anterior.getIdRol() == null ? null : anterior.getIdRol().getIdRol(),
                r.getIdRol() == null ? null : r.getIdRol().getIdRol()))
            throw new DatosInvalidosException("Las relaciones no pueden cambiarse durante la edición");

        var empleado = exigirReferencia(com.ppi115.cafefe.entity.Empleado.class,
                r.getIdEmpleado() == null ? null : r.getIdEmpleado().getIdEmpleado(), "El empleado");
        var rol = exigirReferencia(com.ppi115.cafefe.entity.Rol.class,
                r.getIdRol() == null ? null : r.getIdRol().getIdRol(), "El rol");
        if (!Boolean.TRUE.equals(empleado.getActivo()) || !Boolean.TRUE.equals(rol.getActivo()))
            throw new DatosInvalidosException("El empleado y el rol deben estar activos");
        r.setIdEmpleado(empleado);
        r.setIdRol(rol);
    }
}
