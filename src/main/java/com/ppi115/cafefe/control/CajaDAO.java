package com.ppi115.cafefe.control;
import com.ppi115.cafefe.entity.Caja;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.ejb.LocalBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@LocalBean
public class CajaDAO extends DefaultDAO<Caja>{

    @PersistenceContext (unitName = "Cafefe-PU") // cambia segun el nombre de persistencia
    private EntityManager em;

    public CajaDAO(){
        super(Caja.class);
    }

   @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /*
        Metodo para comprobar cajas activas para realizar un proceso de facturacion
     */
    public List<Caja> findActivas() {
        try {
            return em.createQuery("SELECT c FROM Caja c WHERE c.activo = true", Caja.class)
                 .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }



    /** Valida los datos y las referencias del módulo comercial. */
    @Override
    protected void validar(Caja registro) {
        super.validar(registro);
        ValidacionComercial.validar(em, registro);
    }

    /** Conserva el historial: los registros relacionados deben resolverse antes de eliminar. */
    @Override
    public void eliminar(Object id) {
        if (id == null) throw new DatosInvalidosException("El identificador es obligatorio");
        long relacionados = em.createQuery("SELECT COUNT(f) FROM Factura f WHERE f.idCaja.idCaja = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (relacionados > 0) throw new DatosInvalidosException("El registro tiene datos relacionados y no se puede eliminar");
        super.eliminar(id);
    }
}
