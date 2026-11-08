package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.CajaDAO;
import com.ppi115.cafefe.entity.Caja;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;

/** Estado y acciones JSF de CAJA. Coordina la vista y delega el acceso a datos en los DAO. */
@Named("cajaModel")
@ViewScoped
public class CajaModel extends AbstractModel<Caja> {
    @Inject private CajaDAO dao;
    public CajaModel() { setNombreBean("Caja"); }
    @Override public CajaDAO getDao() { return dao; }
    @Override public Caja createRegistrer() {
        Caja caja = new Caja(UUID.randomUUID()); caja.setActivo(true); return caja;
    }
    @Override public Object getRegistrerById(Object id) {
        return id == null ? null : consultar(() -> dao.findById(id), null);
    }
    @Override public Object getIdByRegistrer(Caja caja) { return caja == null ? null : caja.getIdCaja(); }
}
