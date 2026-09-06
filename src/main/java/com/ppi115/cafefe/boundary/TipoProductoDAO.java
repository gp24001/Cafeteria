package com.ppi115.cafefe.boundary;

import com.ppi115.cafefe.boundary.DefaultDAO;
import com.ppi115.cafefe.entity.TipoProducto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@LocalBean
public class TipoProductoDAO extends DefaultDAO<TipoProducto> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public TipoProductoDAO() {
        super(TipoProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
