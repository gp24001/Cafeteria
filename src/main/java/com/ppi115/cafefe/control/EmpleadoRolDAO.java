package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.EmpleadoRol;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class EmpleadoRolDAO extends DefaultDAO<EmpleadoRol> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public EmpleadoRolDAO() {
        super(EmpleadoRol.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<EmpleadoRol> findByIdEmpleado(UUID idEmpleado) {
        return em.createQuery(
                "SELECT er FROM EmpleadoRol er "
                + "WHERE er.idEmpleado.idEmpleado = :idEmpleado "
                + "AND er.activo = true",
                EmpleadoRol.class)
                .setParameter("idEmpleado", idEmpleado)
                .getResultList();
    }
}