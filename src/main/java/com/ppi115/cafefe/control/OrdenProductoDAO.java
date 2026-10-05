package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.OrdenProducto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
@LocalBean
public class OrdenProductoDAO extends DefaultDAO<OrdenProducto> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public OrdenProductoDAO() {
        super(OrdenProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /** Líneas de una orden con el producto ya cargado. */
    public List<OrdenProducto> findByOrden(Object idOrden) {
        return em.createQuery(
                "SELECT op FROM OrdenProducto op "
                + "LEFT JOIN FETCH op.idProducto "
                + "WHERE op.idOrden.idOrden = :id", OrdenProducto.class)
                .setParameter("id", idOrden)
                .getResultList();
    }
}