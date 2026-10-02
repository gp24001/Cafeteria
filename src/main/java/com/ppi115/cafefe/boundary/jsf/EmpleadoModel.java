package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.EmpleadoDAO;
import com.ppi115.cafefe.control.EmpleadoRolDAO;
import com.ppi115.cafefe.control.RolDAO;
import com.ppi115.cafefe.entity.Empleado;
import com.ppi115.cafefe.entity.EmpleadoRol;
import com.ppi115.cafefe.entity.Rol;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;

@Named("empleadoModel")
@ViewScoped
public class EmpleadoModel extends AbstractModel<Empleado> implements Serializable {

    @Inject
    private EmpleadoDAO dao;

    @Inject
    private RolDAO rolDAO;

    @Inject
    private EmpleadoRolDAO empleadoRolDAO;

    private EmpleadoRol empleadoRol;
    private EmpleadoRol empleadoRolSeleccionado;
    private UUID idRolSeleccionado;
    private ESTADO_CRUD estadoRol = ESTADO_CRUD.NINGUNO;
    private int pestanaActiva = 0;

    public EmpleadoModel() {
        setNombreBean("Empleado");
    }

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
        if (id == null) return null;
        return dao.findById(id);
    }

    @Override
    public Object getIdByRegistrer(Empleado registro) {
        if (registro == null) return null;
        return registro.getIdEmpleado();
    }

    @Override
    public void nuevo() {
        pestanaActiva = 0;
        limpiarRol();
        super.nuevo();
    }

    @Override
    public void guardar() {
        pestanaActiva = 0;
        super.guardar();
        if (getEstado() == ESTADO_CRUD.NINGUNO) limpiarRol();
    }

    @Override
    public void cancelar() {
        pestanaActiva = 0;
        super.cancelar();
        limpiarRol();
    }

    @Override
    public void seleccionar(SelectEvent<Empleado> event) {
        pestanaActiva = 0;
        super.seleccionar(event);

        if (event == null || event.getObject() == null) return;
        empleadoRol = new EmpleadoRol();
        empleadoRol.setIdEmpleadoRol(UUID.randomUUID());
        empleadoRol.setIdEmpleado(event.getObject());
        empleadoRol.setActivo(true);
        empleadoRolSeleccionado = null;
        idRolSeleccionado = null;
        estadoRol = ESTADO_CRUD.NUEVO;
    }

    public void seleccionarRol(SelectEvent<EmpleadoRol> event) {
        if (event == null || event.getObject() == null) return;
        empleadoRolSeleccionado = event.getObject();
        empleadoRol = event.getObject();
        estadoRol = ESTADO_CRUD.EDITAR;
        pestanaActiva = 1;

        if (empleadoRol.getIdRol() != null) {
            idRolSeleccionado = empleadoRol.getIdRol().getIdRol();
        }
    }

    public void guardarRol() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (empleadoRol == null) {
            context.addMessage(null,new FacesMessage( FacesMessage.SEVERITY_WARN,"No hay una asignación para guardar", null));
            return;
        }

        try {
            if (estadoRol == ESTADO_CRUD.NUEVO) {
                if (getSeleccionado() == null) {
                    context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"Debe seleccionar un empleado",null));
                    return;
                }

                if (!Boolean.TRUE.equals(getSeleccionado().getActivo())) {
                    context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"No se puede asignar un rol a un empleado inactivo",null));
                    return;
                }

                if (idRolSeleccionado == null) {
                    context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"Debe seleccionar un rol",null));
                    return;
                }

                Rol rol = rolDAO.findById(idRolSeleccionado);

                if (rol == null || !Boolean.TRUE.equals(rol.getActivo())) {
                    context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"No se puede asignar un rol inactivo",null));
                    return;
                }

                empleadoRol.setIdEmpleado(getSeleccionado());
                empleadoRol.setIdRol(rol);
                empleadoRolDAO.crear(empleadoRol);

                context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO,"Se asignó el rol al empleado",null));
            } else if (estadoRol == ESTADO_CRUD.EDITAR) {
                empleadoRolDAO.modificar(empleadoRol);

                context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO,"Se modificó la asignación del rol",null));
            }

            super.cancelar();
            limpiarRol();
            pestanaActiva = 1;
        } catch (IllegalArgumentException e) {
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error al guardar: " + e.getMessage(),null));
        } catch (Exception e) {
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"Ocurrió un error al guardar la asignación",null));
        }
    }

    public void eliminarRol() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (empleadoRol == null || empleadoRol.getIdEmpleadoRol() == null) {
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"Debe seleccionar una asignación",null));
            return;
        }
        try {
            empleadoRolDAO.eliminar(empleadoRol.getIdEmpleadoRol());
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO,"Se eliminó la asignación del rol",null));
            super.cancelar();
            limpiarRol();
            pestanaActiva = 1;
        } catch (IllegalArgumentException e) {
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error al eliminar: " + e.getMessage(),null));
        } catch (Exception e) {
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"No se pudo eliminar la asignación del rol",null));
        }
    }

    public void cancelarRol() {
        super.cancelar();
        limpiarRol();
        pestanaActiva = 1;
    }

    private void limpiarRol() {
        empleadoRol = null;
        empleadoRolSeleccionado = null;
        idRolSeleccionado = null;
        estadoRol = ESTADO_CRUD.NINGUNO;
    }

    public List<Rol> getRoles() {
        return rolDAO.findAll();
    }

    public List<EmpleadoRol> getEmpleadoRoles() {
        return empleadoRolDAO.findAll();
    }

    public EmpleadoRol getEmpleadoRol() {
        return empleadoRol;
    }

    public void setEmpleadoRol(EmpleadoRol empleadoRol) {
        this.empleadoRol = empleadoRol;
    }

    public EmpleadoRol getEmpleadoRolSeleccionado() {
        return empleadoRolSeleccionado;
    }

    public void setEmpleadoRolSeleccionado(EmpleadoRol empleadoRolSeleccionado) {
        this.empleadoRolSeleccionado = empleadoRolSeleccionado;
    }

    public UUID getIdRolSeleccionado() {
        return idRolSeleccionado;
    }

    public void setIdRolSeleccionado(UUID idRolSeleccionado) {
        this.idRolSeleccionado = idRolSeleccionado;
    }

    public ESTADO_CRUD getEstadoRol() {
        return estadoRol;
    }

    public void setEstadoRol(ESTADO_CRUD estadoRol) {
        this.estadoRol = estadoRol;
    }

    public int getPestanaActiva() {
        return pestanaActiva;
    }

    public void setPestanaActiva(int pestanaActiva) {
        this.pestanaActiva = pestanaActiva;
    }
}