package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.ProductoTipoProducto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class ProductoTipoProductoDAO extends DefaultDAO<ProductoTipoProducto> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public ProductoTipoProductoDAO() {
        super(ProductoTipoProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<ProductoTipoProducto> findByIdProducto(UUID idProducto) {
        return em.createQuery(
                "SELECT ptp FROM ProductoTipoProducto ptp "
                + "WHERE ptp.idProducto.idProducto = :idProducto "
                + "AND ptp.activo = true",
                ProductoTipoProducto.class)
                .setParameter("idProducto", idProducto)
                .getResultList();
    }
}