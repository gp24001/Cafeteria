package com.ppi115.cafefe.control;
import com.ppi115.cafefe.entity.Factura;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@jakarta.enterprise.context.Dependent
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

    /* 
        Obtiene el historial de facturas emitidas en una caja específica
     */
    public List<Factura> findByCaja(UUID idCaja) {
        try {
            if (idCaja == null) throw new DatosInvalidosException("El identificador es obligatorio");
            return em.createQuery(
                "SELECT f FROM Factura f WHERE f.idCaja.idCaja = :idCaja ORDER BY f.fechaFacturacion DESC",
                Factura.class)
                 .setParameter("idCaja", idCaja)
                 .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /** Valida los datos y las referencias del módulo comercial. */
    @Override
    protected void validar(Factura registro) {
        super.validar(registro);
        ValidacionComercial.validar(em, registro);
    }

    /** Conserva el historial: los registros relacionados deben resolverse antes de eliminar. */
    @Override
    public void eliminar(Object id) {
        if (id == null) throw new DatosInvalidosException("El identificador es obligatorio");
        long relacionados = em.createQuery("SELECT COUNT(f) FROM FacturaOrdenProducto f WHERE f.idFactura.idFactura = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (relacionados > 0) throw new DatosInvalidosException("El registro tiene datos relacionados y no se puede eliminar");
        if (em.createQuery("SELECT COUNT(p) FROM Pago p WHERE p.idFactura.idFactura = :id", Long.class)
                .setParameter("id", id).getSingleResult() > 0)
            throw new DatosInvalidosException("La factura tiene pagos y no se puede eliminar");
        super.eliminar(id);
    }
}
