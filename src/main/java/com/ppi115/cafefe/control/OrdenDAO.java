package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Orden;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@LocalBean
public class OrdenDAO extends DefaultDAO<Orden> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public OrdenDAO() {
        super(Orden.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}