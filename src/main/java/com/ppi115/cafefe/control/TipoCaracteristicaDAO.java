package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.TipoCaracteristica;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@LocalBean
public class TipoCaracteristicaDAO extends DefaultDAO<TipoCaracteristica> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public TipoCaracteristicaDAO() {
        super(TipoCaracteristica.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}