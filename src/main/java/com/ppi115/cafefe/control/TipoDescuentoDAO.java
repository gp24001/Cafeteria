package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.TipoDescuento;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Acceso a datos de tipo y límite de descuento. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class TipoDescuentoDAO extends DefaultDAO<TipoDescuento> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa TipoDescuentoDAO con los datos recibidos.
     */
    public TipoDescuentoDAO() {
        super(TipoDescuento.class);
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
