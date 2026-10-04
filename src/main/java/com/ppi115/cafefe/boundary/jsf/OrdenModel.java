package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.EmpleadoRolDAO;
import com.ppi115.cafefe.control.OrdenDAO;
import com.ppi115.cafefe.entity.EmpleadoRol;
import com.ppi115.cafefe.entity.Orden;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Named("ordenModel")
@ViewScoped
public class OrdenModel extends AbstractModel<Orden> implements Serializable {

    @Inject
    private OrdenDAO dao;

    @Inject
    private EmpleadoRolDAO empleadoRolDAO;

    private List<EmpleadoRol> listaEmpleadoRol;

    @Override
    public OrdenDAO getDao() {
        return dao;
    }

    @Override
    public Orden createRegistrer() {
        Orden r = new Orden();
        r.setIdOrden(UUID.randomUUID());
        r.setFechaOrden(new Date());
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
    public Object getIdByRegistrer(Orden registro) {
        if (registro == null) {
            return null;
        }
        return registro.getIdOrden();
    }

    @Override
    public String getNombreBean() {
        return "Orden";
    }

    public List<EmpleadoRol> getListaEmpleadoRol() {
        if (listaEmpleadoRol == null && empleadoRolDAO != null) {
            listaEmpleadoRol = empleadoRolDAO.findAll();
        }
        return listaEmpleadoRol;
    }
}