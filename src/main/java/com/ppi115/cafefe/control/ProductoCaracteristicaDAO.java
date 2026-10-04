package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.ProductoCaracteristica;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class ProductoCaracteristicaDAO extends DefaultDAO<ProductoCaracteristica> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public ProductoCaracteristicaDAO() {
        super(ProductoCaracteristica.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<ProductoCaracteristica> findByIdProducto(UUID idProducto) {
        return em.createQuery(
                "SELECT pc FROM ProductoCaracteristica pc "
                + "WHERE pc.idProducto.idProducto = :idProducto",
                ProductoCaracteristica.class)
                .setParameter("idProducto", idProducto)
                .getResultList();
    }
}