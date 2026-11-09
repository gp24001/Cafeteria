package com.ppi115.cafefe.control;
import com.ppi115.cafefe.entity.PagoDetalle;


import java.util.List;
import java.util.UUID;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@jakarta.enterprise.context.Dependent
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
        try {
            if (idPago == null) throw new DatosInvalidosException("El identificador es obligatorio");
            return em.createQuery(
                "SELECT pd FROM PagoDetalle pd WHERE pd.idPago.idPago = :idPago",
                PagoDetalle.class)
                 .setParameter("idPago", idPago)
                 .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /** Valida los datos y las referencias del módulo comercial. */
    @Override
    protected void validar(PagoDetalle registro) {
        super.validar(registro);
        ValidacionComercial.validar(em, registro);
    }
}

