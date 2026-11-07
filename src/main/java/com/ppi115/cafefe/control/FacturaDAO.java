package com.ppi115.cafefe.control;
import com.ppi115.cafefe.entity.Factura;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@LocalBean 
public class FacturaDAO extends DefaultDAO<Factura> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public FacturaDAO() {
        super(Factura.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /*  rest
        Obtiene el historial de facturas emitidas en una caja específica
     */
    public List<Factura> findByCaja(UUID idCaja) {
        return em.createQuery(
                "SELECT f FROM Factura f WHERE f.caja.id = :idCaja ORDER BY f.fechaEmision DESC", 
                Factura.class)
                 .setParameter("idCaja", idCaja)
                 .getResultList();
    }
}