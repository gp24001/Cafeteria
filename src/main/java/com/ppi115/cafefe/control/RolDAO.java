package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Rol;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/** Acceso a datos de rol de empleado. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class RolDAO extends DefaultDAO<Rol> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa RolDAO con los datos recibidos.
     */
    public RolDAO() {
        super(Rol.class);
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
    public List<Rol> findActivos() {
        try {
            return getEntityManager().createQuery(
                "SELECT r FROM Rol r WHERE r.activo = true ORDER BY r.nombre",Rol.class)
                    .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }
}
