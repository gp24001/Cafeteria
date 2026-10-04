package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Producto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@LocalBean
public class ProductoDAO extends DefaultDAO<Producto> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public ProductoDAO() {
        super(Producto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
