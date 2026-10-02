package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.RolDAO;
import com.ppi115.cafefe.entity.Rol;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;

@Named("rolModel")
@ViewScoped
public class RolModel extends AbstractModel<Rol> implements Serializable {

    @Inject
    private RolDAO dao;

    @Override
    public RolDAO getDao() {
        return dao;
    }

    @Override
    public Rol createRegistrer() {
        Rol r = new Rol();
        r.setIdRol(UUID.randomUUID());
        r.setActivo(true);
        return r;
    }

    @Override
    public Object getRegistrerById(Object id) {
        if (id == null) {
            return null;
        }
        return dao.findById(id);
    }

    @Override
    public Object getIdByRegistrer(Rol registro) {
        if (registro == null) {
            return null;
        }
        return registro.getIdRol();
    }

    public RolModel() {
        setNombreBean("Rol");
    }
}