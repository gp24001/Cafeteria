package com.ppi115.cafefe.control;
import com.ppi115.cafefe.entity.Pago;

import java.util.List;
import java.util.UUID;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@jakarta.enterprise.context.Dependent
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

    /*  
        Obtiene el expediente de pago asociado a una Factura específica
     */
    public List<Pago> findByFactura(UUID idFactura) {
        try {
            if (idFactura == null) throw new DatosInvalidosException("El identificador es obligatorio");
            return em.createQuery(
                "SELECT p FROM Pago p WHERE p.idFactura.idFactura = :idFactura",
                Pago.class)
                 .setParameter("idFactura", idFactura)
                 .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /** Valida los datos y las referencias del módulo comercial. */
    @Override
    protected void validar(Pago registro) {
        super.validar(registro);
        ValidacionComercial.validar(em, registro);
    }

    /** Conserva el historial: los registros relacionados deben resolverse antes de eliminar. */
    @Override
    public void eliminar(Object id) {
        if (id == null) throw new DatosInvalidosException("El identificador es obligatorio");
        long relacionados = em.createQuery("SELECT COUNT(d) FROM PagoDetalle d WHERE d.idPago.idPago = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (relacionados > 0) throw new DatosInvalidosException("El registro tiene datos relacionados y no se puede eliminar");
        super.eliminar(id);
    }
}
