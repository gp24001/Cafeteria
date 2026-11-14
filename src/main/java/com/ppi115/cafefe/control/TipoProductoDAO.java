package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.TipoProducto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Acceso a datos de categoría de productos. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class TipoProductoDAO extends DefaultDAO<TipoProducto> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa TipoProductoDAO con los datos recibidos.
     */
    public TipoProductoDAO() {
        super(TipoProducto.class);
    }

    /**
     * Proporciona el contexto de persistencia administrado por Jakarta EE.
     * @return entity manager actual
     */
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
