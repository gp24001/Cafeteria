package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Descuento;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@LocalBean
public class DescuentoDAO extends DefaultDAO<Descuento> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public DescuentoDAO() {
        super(Descuento.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}