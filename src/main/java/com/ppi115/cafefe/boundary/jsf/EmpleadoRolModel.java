package com.ppi115.cafefe.boundary.jsf;

import jakarta.inject.Inject;

import com.ppi115.cafefe.control.EmpleadoDAO;
import com.ppi115.cafefe.control.EmpleadoRolDAO;
import com.ppi115.cafefe.control.RolDAO;
import com.ppi115.cafefe.entity.Empleado;
import com.ppi115.cafefe.entity.EmpleadoRol;
import com.ppi115.cafefe.entity.Rol;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;

/** Estado y acciones JSF de asignación de rol a empleado. Coordina la vista y delega el acceso a datos en los DAO. */
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

    /**
     * Proporciona el DAO utilizado para leer y guardar los registros de esta vista.
     * @return dao actual
     */
    @Override
    public EmpleadoRolDAO getDao() {
        return dao;
    }

    /**
     * Construye un registro nuevo con su UUID y los valores iniciales de esta vista.
     * @return registro nuevo con su identificador y valores iniciales
     */
    @Override
    public EmpleadoRol createRegistrer() {
        EmpleadoRol r = new EmpleadoRol();
        r.setIdEmpleadoRol(UUID.randomUUID());
        r.setActivo(true);

        idEmpleadoSeleccionado = null;
        idRolSeleccionado = null;

        return r;
    }

    /**
     * Recupera el registro solicitado a través del DAO de la vista.
     * @param id identificador del registro solicitado
     * @return registro encontrado, o null si no existe
     */
    @Override
    public Object getRegistrerById(Object id) {
        if (id == null) {
            return null;
        }
        return consultar(() -> dao.findById(id), null);
    }

    /**
     * Obtiene el identificador usado por la selección y las tablas de JSF.
     * @param registro registro que se desea consultar o editar
     * @return identificador del registro, o null si no hay registro
     */
    @Override
    public Object getIdByRegistrer(EmpleadoRol registro) {
        if (registro == null) {
            return null;
        }
        return registro.getIdEmpleadoRol();
    }

    /**
     * Obtiene nombre bean para la vista o la operación actual.
     * @return nombre bean actual
     */
    @Override
    public String getNombreBean() {
        return "Empleado Rol";
    }

    /**
     * Valida el formulario y delega la escritura en la capa de persistencia.
     */
    @Override
    public void guardar() {
        try {
            if (getRegistro() == null || idEmpleadoSeleccionado == null || idRolSeleccionado == null)
                throw new IllegalArgumentException("Debe seleccionar un empleado y un rol");
            Empleado empleado = empleadoDAO.findById(idEmpleadoSeleccionado);
            Rol rol = rolDAO.findById(idRolSeleccionado);
            if (empleado == null || !Boolean.TRUE.equals(empleado.getActivo()))
                throw new IllegalArgumentException("El empleado no existe o está inactivo");
            if (rol == null || !Boolean.TRUE.equals(rol.getActivo()))
                throw new IllegalArgumentException("El rol no existe o está inactivo");
            getRegistro().setIdEmpleado(empleado);
            getRegistro().setIdRol(rol);
            super.guardar();
        } catch (RuntimeException e) {
            informarError(e);
        }
    }

    /**
     * Carga el registro indicado y activa el modo de edición.
     * @param registro registro que se desea consultar o editar
     */
    @Override
    public void editar(EmpleadoRol registro) {
        if (registro == null) { super.editar(null); return; }
        super.editar(registro);

        if (registro.getIdEmpleado() != null) {
            idEmpleadoSeleccionado = registro.getIdEmpleado().getIdEmpleado();
        }

        if (registro.getIdRol() != null) {
            idRolSeleccionado = registro.getIdRol().getIdRol();
        }
    }

    /**
     * Recarga las asignaciones del empleado utilizado como filtro.
     */
    public void buscarRolesEmpleado() {
        if (idEmpleadoFiltro == null) {
            rolesEmpleado = new ArrayList<>();
            return;
        }

        rolesEmpleado = consultar(() -> getDao().findByIdEmpleado(idEmpleadoFiltro), List.of());
    }

    /**
     * Carga la fila elegida para editarla sin cambiar su identificador.
     * @param event evento generado por la selección o acción de la vista
     */
    @Override
    public void seleccionar(SelectEvent<EmpleadoRol> event) {
        if (event == null || event.getObject() == null) return;
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

    /**
     * Obtiene roles activos para la vista o la operación actual.
     * @return roles activos actual
     */
    public List<Rol> getRolesActivos() {
    return consultar(() -> rolDAO.findActivos(), java.util.List.of());
}

/**
 * Obtiene empleados activos para la vista o la operación actual.
 * @return empleados activos actual
 */
public List<Empleado> getEmpleadosActivos() {
    return consultar(() -> empleadoDAO.findActivos(), java.util.List.of());
}

    /**
     * Obtiene empleados para la vista o la operación actual.
     * @return empleados actual
     */
    public List<Empleado> getEmpleados() {
        return consultar(() -> empleadoDAO.findAll(), java.util.List.of());
    }

    /**
     * Obtiene roles para la vista o la operación actual.
     * @return roles actual
     */
    public List<Rol> getRoles() {
        return consultar(() -> rolDAO.findAll(), java.util.List.of());
    }

    /**
     * Obtiene id empleado seleccionado para la vista o la operación actual.
     * @return id empleado seleccionado actual
     */
    public UUID getIdEmpleadoSeleccionado() {
        return idEmpleadoSeleccionado;
    }

    /**
     * Actualiza id empleado seleccionado con el valor recibido.
     * @param idEmpleadoSeleccionado id empleado seleccionado utilizado por la operación
     */
    public void setIdEmpleadoSeleccionado(UUID idEmpleadoSeleccionado) {
        this.idEmpleadoSeleccionado = idEmpleadoSeleccionado;
    }

    /**
     * Obtiene id rol seleccionado para la vista o la operación actual.
     * @return id rol seleccionado actual
     */
    public UUID getIdRolSeleccionado() {
        return idRolSeleccionado;
    }

    /**
     * Actualiza id rol seleccionado con el valor recibido.
     * @param idRolSeleccionado id rol seleccionado utilizado por la operación
     */
    public void setIdRolSeleccionado(UUID idRolSeleccionado) {
        this.idRolSeleccionado = idRolSeleccionado;
    }

    /**
     * Obtiene id empleado filtro para la vista o la operación actual.
     * @return id empleado filtro actual
     */
    public UUID getIdEmpleadoFiltro() {
        return idEmpleadoFiltro;
    }

    /**
     * Actualiza id empleado filtro con el valor recibido.
     * @param idEmpleadoFiltro id empleado filtro utilizado por la operación
     */
    public void setIdEmpleadoFiltro(UUID idEmpleadoFiltro) {
        this.idEmpleadoFiltro = idEmpleadoFiltro;
    }

    /**
     * Obtiene roles empleado para la vista o la operación actual.
     * @return roles empleado actual
     */
    public List<EmpleadoRol> getRolesEmpleado() {
        return rolesEmpleado;
    }

    /**
     * Actualiza roles empleado con el valor recibido.
     * @param rolesEmpleado roles empleado utilizado por la operación
     */
    public void setRolesEmpleado(List<EmpleadoRol> rolesEmpleado) {
        this.rolesEmpleado = rolesEmpleado;
    }
}
