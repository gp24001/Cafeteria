package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.EmpleadoDAO;
import com.ppi115.cafefe.control.EmpleadoRolDAO;
import com.ppi115.cafefe.control.RolDAO;
import com.ppi115.cafefe.entity.Empleado;
import com.ppi115.cafefe.entity.EmpleadoRol;
import com.ppi115.cafefe.entity.Rol;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;

@Named("empleadoRolModel")
@ViewScoped
public class EmpleadoRolModel extends AbstractModel<EmpleadoRol> implements Serializable {

    @Inject
    private EmpleadoRolDAO dao;

    @Inject
    private EmpleadoDAO empleadoDAO;

    @Inject
    private RolDAO rolDAO;

    private UUID idEmpleadoSeleccionado;
    private UUID idRolSeleccionado;

    private UUID idEmpleadoFiltro;
    private List<EmpleadoRol> rolesEmpleado = new ArrayList<>();

    @Override
    public EmpleadoRolDAO getDao() {
        return dao;
    }

    @Override
    public EmpleadoRol createRegistrer() {
        EmpleadoRol r = new EmpleadoRol();
        r.setIdEmpleadoRol(UUID.randomUUID());
        r.setActivo(true);

        idEmpleadoSeleccionado = null;
        idRolSeleccionado = null;

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
    public Object getIdByRegistrer(EmpleadoRol registro) {
        if (registro == null) {
            return null;
        }
        return registro.getIdEmpleadoRol();
    }

    @Override
    public String getNombreBean() {
        return "Empleado Rol";
    }

    @Override
    public void guardar() {
        if (idEmpleadoSeleccionado != null) {
            Empleado empleado = empleadoDAO.findById(idEmpleadoSeleccionado);
            getRegistro().setIdEmpleado(empleado);
        }

        if (idRolSeleccionado != null) {
            Rol rol = rolDAO.findById(idRolSeleccionado);
            getRegistro().setIdRol(rol);
        }

        super.guardar();
    }

    @Override
    public void editar(EmpleadoRol registro) {
        super.editar(registro);

        if (registro.getIdEmpleado() != null) {
            idEmpleadoSeleccionado = registro.getIdEmpleado().getIdEmpleado();
        }

        if (registro.getIdRol() != null) {
            idRolSeleccionado = registro.getIdRol().getIdRol();
        }
    }

    public void buscarRolesEmpleado() {
        if (idEmpleadoFiltro == null) {
            rolesEmpleado = new ArrayList<>();
            return;
        }

        rolesEmpleado = ((EmpleadoRolDAO) getDao()).findByIdEmpleado(idEmpleadoFiltro);
    }

    @Override
    public void seleccionar(SelectEvent<EmpleadoRol> event) {
        super.seleccionar(event);

        EmpleadoRol registro = event.getObject();

        if (registro != null && registro.getIdEmpleado() != null) {
            idEmpleadoSeleccionado = registro.getIdEmpleado().getIdEmpleado();
        } else {
            idEmpleadoSeleccionado = null;
        }

        if (registro != null && registro.getIdRol() != null) {
            idRolSeleccionado = registro.getIdRol().getIdRol();
        } else {
            idRolSeleccionado = null;
        }
    }

    public List<Empleado> getEmpleados() {
        return empleadoDAO.findAll();
    }

    public List<Rol> getRoles() {
        return rolDAO.findAll();
    }

    public UUID getIdEmpleadoSeleccionado() {
        return idEmpleadoSeleccionado;
    }

    public void setIdEmpleadoSeleccionado(UUID idEmpleadoSeleccionado) {
        this.idEmpleadoSeleccionado = idEmpleadoSeleccionado;
    }

    public UUID getIdRolSeleccionado() {
        return idRolSeleccionado;
    }

    public void setIdRolSeleccionado(UUID idRolSeleccionado) {
        this.idRolSeleccionado = idRolSeleccionado;
    }

    public UUID getIdEmpleadoFiltro() {
        return idEmpleadoFiltro;
    }

    public void setIdEmpleadoFiltro(UUID idEmpleadoFiltro) {
        this.idEmpleadoFiltro = idEmpleadoFiltro;
    }

    public List<EmpleadoRol> getRolesEmpleado() {
        return rolesEmpleado;
    }

    public void setRolesEmpleado(List<EmpleadoRol> rolesEmpleado) {
        this.rolesEmpleado = rolesEmpleado;
    }
}