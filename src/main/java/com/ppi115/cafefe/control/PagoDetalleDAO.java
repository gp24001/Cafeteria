package com.ppi115.cafefe.control;
import com.ppi115.cafefe.entity.PagoDetalle;


import java.util.List;
import java.util.UUID;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@LocalBean 
public class PagoDetalleDAO extends DefaultDAO<PagoDetalle> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public PagoDetalleDAO() {
        super(PagoDetalle.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /*
        Obtiene las formas de pago y montos parciales aplicados a un Pago
     */
    public List<PagoDetalle> findByPago(UUID idPago) {
        return em.createQuery(
                "SELECT pd FROM PagoDetalle pd WHERE pd.pago.id = :idPago", 
                PagoDetalle.class)
                 .setParameter("idPago", idPago)
                 .getResultList();
    }
}

