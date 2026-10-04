package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.DescuentoProducto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class DescuentoProductoDAO extends DefaultDAO<DescuentoProducto> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public DescuentoProductoDAO() {
        super(DescuentoProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<DescuentoProducto> findByIdProducto(UUID idProducto) {
        return em.createQuery(
                "SELECT dp FROM DescuentoProducto dp "
                + "WHERE dp.idProducto.idProducto = :idProducto",
                DescuentoProducto.class)
                .setParameter("idProducto", idProducto)
                .getResultList();
    }

    public boolean existeProductoDescuento(UUID idProducto, UUID idDescuento) {
        Long cantidad = em.createQuery(
                "SELECT COUNT(dp) FROM DescuentoProducto dp "
                + "WHERE dp.idProducto.idProducto = :idProducto "
                + "AND dp.idDescuento.idDescuento = :idDescuento",
                Long.class)
                .setParameter("idProducto", idProducto)
                .setParameter("idDescuento", idDescuento)
                .getSingleResult();

        return cantidad > 0;
    }
}