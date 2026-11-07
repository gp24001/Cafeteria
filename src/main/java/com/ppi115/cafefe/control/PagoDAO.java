package com.ppi115.cafefe.control;
import com.ppi115.cafefe.entity.Pago;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@LocalBean 
public class PagoDAO extends DefaultDAO<Pago> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public PagoDAO() {
        super(Pago.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /*  rest
        Obtiene el expediente de pago asociado a una Factura específica
     */
    public List<Pago> findByFactura(UUID idFactura) {
        return em.createQuery(
                "SELECT p FROM Pago p WHERE p.factura.id = :idFactura", 
                Pago.class)
                 .setParameter("idFactura", idFactura)
                 .getResultList();
    }
}