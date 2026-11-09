package com.ppi115.cafefe.control;
import com.ppi115.cafefe.entity.FacturaOrdenProducto;

import java.util.List;
import java.util.UUID;


import jakarta.ejb.Stateless;
import jakarta.ejb.LocalBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@jakarta.enterprise.context.Dependent
@Stateless
@LocalBean
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

    /*
        Obtiene las líneas de ítems cobrados vinculados a una Factura
     */
    public List<FacturaOrdenProducto> findByFactura(UUID idFactura) {
        try {
            if (idFactura == null) throw new DatosInvalidosException("El identificador es obligatorio");
            return em.createQuery(
                "SELECT fop FROM FacturaOrdenProducto fop JOIN FETCH fop.idOrdenProducto op "
                + "JOIN FETCH op.idProducto WHERE fop.idFactura.idFactura = :idFactura",
                FacturaOrdenProducto.class)
                 .setParameter("idFactura", idFactura)
                 .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /** Valida los datos y las referencias del módulo comercial. */
    @Override
    protected void validar(FacturaOrdenProducto registro) {
        super.validar(registro);
        ValidacionComercial.validar(em, registro);
    }
}
