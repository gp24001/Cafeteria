package com.ppi115.cafefe.control;
import com.ppi115.cafefe.entity.Caja;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless 
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
        return em.createQuery("SELECT c FROM Caja c WHERE c.activo = true", Caja.class)
                 .getResultList();
    }

    
}
