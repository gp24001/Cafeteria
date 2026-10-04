package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Caracteristica;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class CaracteristicaDAO extends DefaultDAO<Caracteristica> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public CaracteristicaDAO() {
        super(Caracteristica.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}