package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.TipoDescuento;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@Stateless
@LocalBean
public class TipoDescuentoDAO extends DefaultDAO<TipoDescuento> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public TipoDescuentoDAO() {
        super(TipoDescuento.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
