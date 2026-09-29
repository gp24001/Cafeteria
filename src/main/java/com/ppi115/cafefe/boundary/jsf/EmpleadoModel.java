package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.EmpleadoDAO;
import com.ppi115.cafefe.entity.Empleado;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;

@Named("empleadoModel")
@ViewScoped
public class EmpleadoModel extends AbstractModel<Empleado> implements Serializable {

    @Inject
    private EmpleadoDAO dao;

    @Override
    public EmpleadoDAO getDao() {
        return dao;
    }

    @Override
    public Empleado createRegistrer() {
        Empleado r = new Empleado();
        r.setIdEmpleado(UUID.randomUUID());
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
    public Object getIdByRegistrer(Empleado registro) {
        if (registro == null) {
            return null;
        }
        return registro.getIdEmpleado();
    }

    @Override
    public String getNombreBean() {
        return "Empleado";
    }
}