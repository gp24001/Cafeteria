package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Empleado;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
@LocalBean
public class EmpleadoDAO extends DefaultDAO<Empleado> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public EmpleadoDAO() {
        super(Empleado.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public List<Empleado> findActivos() {
    return getEntityManager().createQuery(
        "SELECT e FROM Empleado e WHERE e.activo = true ORDER BY e.nombre",Empleado.class)
            .getResultList();
}
}