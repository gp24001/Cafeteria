package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Rol;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@LocalBean
public class RolDAO extends DefaultDAO<Rol> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public RolDAO() {
        super(Rol.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}