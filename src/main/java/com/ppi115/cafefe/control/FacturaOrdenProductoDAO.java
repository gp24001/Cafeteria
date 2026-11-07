package com.ppi115.cafefe.control;
import com.ppi115.cafefe.entity.FacturaOrdenProducto;

import java.util.List;
import java.util.UUID;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class FacturaOrdenProductoDAO extends DefaultDAO<FacturaOrdenProducto> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public FacturaOrdenProductoDAO() {
        super(FacturaOrdenProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /*  rest
        Obtiene las líneas de ítems cobrados vinculados a una Factura
     */
    public List<FacturaOrdenProducto> findByFactura(UUID idFactura) {
        return em.createQuery(
                "SELECT fop FROM FacturaOrdenProducto fop WHERE fop.factura.id = :idFactura", 
                FacturaOrdenProducto.class)
                 .setParameter("idFactura", idFactura)
                 .getResultList();
    }
}