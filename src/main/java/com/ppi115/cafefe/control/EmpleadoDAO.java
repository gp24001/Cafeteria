package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Empleado;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/** Acceso a datos de empleado. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class EmpleadoDAO extends DefaultDAO<Empleado> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa EmpleadoDAO con los datos recibidos.
     */
    public EmpleadoDAO() {
        super(Empleado.class);
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
     * Consulta únicamente los registros marcados como activos.
     * @return lista de registros que cumplen la consulta
     */
    public List<Empleado> findActivos() {
        try {
            return getEntityManager().createQuery(
                "SELECT e FROM Empleado e WHERE e.activo = true ORDER BY e.nombre",Empleado.class)
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
    protected void validar(Empleado r) {
        super.validar(r);
        if (r.getNombre() == null || r.getNombre().isBlank()
                || r.getApellido() == null || r.getApellido().isBlank())
            throw new DatosInvalidosException("El nombre y el apellido son obligatorios");
    }
}
