package com.ppi115.cafefe.boundary.jsf;

import jakarta.inject.Inject;

import com.ppi115.cafefe.control.EmpleadoDAO;
import com.ppi115.cafefe.control.EmpleadoRolDAO;
import com.ppi115.cafefe.control.RolDAO;
import com.ppi115.cafefe.entity.Empleado;
import com.ppi115.cafefe.entity.EmpleadoRol;
import com.ppi115.cafefe.entity.Rol;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;

/** Estado y acciones JSF de empleado. Coordina la vista y delega el acceso a datos en los DAO. */
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

    /**
     * Inicializa EmpleadoModel con los datos recibidos.
     */
    public EmpleadoModel() {
        setNombreBean("Empleado");
    }

    /**
     * Proporciona el DAO utilizado para leer y guardar los registros de esta vista.
     * @return dao actual
     */
    @Override
    public EmpleadoDAO getDao() {
        return dao;
    }

    /**
     * Construye un registro nuevo con su UUID y los valores iniciales de esta vista.
     * @return registro nuevo con su identificador y valores iniciales
     */
    @Override
    public Empleado createRegistrer() {
        Empleado r = new Empleado();
        r.setIdEmpleado(UUID.randomUUID());
        r.setActivo(true);
        return r;
    }

    /**
     * Recupera el registro solicitado a través del DAO de la vista.
     * @param id identificador del registro solicitado
     * @return registro encontrado, o null si no existe
     */
    @Override
    public Object getRegistrerById(Object id) {
        if (id == null) return null;
        return consultar(() -> dao.findById(id), null);
    }

    /**
     * Obtiene el identificador usado por la selección y las tablas de JSF.
     * @param registro registro que se desea consultar o editar
     * @return identificador del registro, o null si no hay registro
     */
    @Override
    public Object getIdByRegistrer(Empleado registro) {
        if (registro == null) return null;
        return registro.getIdEmpleado();
    }

    /**
     * Prepara el formulario para introducir un registro nuevo.
     */
    @Override
    public void nuevo() {
        pestanaActiva = 0;
        limpiarRol();
        super.nuevo();
    }

    /**
     * Valida el formulario y delega la escritura en la capa de persistencia.
     */
    @Override
    public void guardar() {
        if (getRegistro() == null || getRegistro().getNombre() == null || getRegistro().getNombre().isBlank()
                || getRegistro().getApellido() == null || getRegistro().getApellido().isBlank()) {
            informarError(new IllegalArgumentException("El nombre y el apellido son obligatorios"));
            return;
        }
        if (getEstado() != ESTADO_CRUD.NUEVO && getEstado() != ESTADO_CRUD.EDITAR) return;
        boolean nuevo = getEstado() == ESTADO_CRUD.NUEVO;
        Empleado guardado = getRegistro();
        super.guardar();
        if (getEstado() == ESTADO_CRUD.NINGUNO) {
            editar(guardado);
            if (nuevo) prepararRol(guardado);
        }
    }

    /**
     * Descarta la edición actual y restablece el estado del formulario correspondiente.
     */
    @Override
    public void cancelar() {
        pestanaActiva = 0;
        super.cancelar();
        limpiarRol();
    }

    /**
     * Carga la fila elegida para editarla sin cambiar su identificador.
     * @param event evento generado por la selección o acción de la vista
     */
    @Override
    public void seleccionar(SelectEvent<Empleado> event) {
        pestanaActiva = 0;
        super.seleccionar(event);

        if (event == null || event.getObject() == null) return;
        prepararRol(event.getObject());
    }

    /**
     * Carga rol para su edición en la vista.
     * @param event evento generado por la selección o acción de la vista
     */
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

    /**
     * Valida y guarda rol mediante su DAO.
     */
    public void guardarRol() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (empleadoRol == null) {
            context.addMessage(null,new FacesMessage( FacesMessage.SEVERITY_WARN,"No hay una asignación para guardar", null));
            return;
        }

        try {
            if (estadoRol != ESTADO_CRUD.NUEVO && estadoRol != ESTADO_CRUD.EDITAR)
                throw new IllegalArgumentException("Debe crear o seleccionar una asignación");
            var errores = getValidator().validate(empleadoRol);
            if (!errores.isEmpty()) throw new IllegalArgumentException(errores.iterator().next().getMessage());
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

            empleadoRolSeleccionado = empleadoRol;
            estadoRol = ESTADO_CRUD.EDITAR;
            pestanaActiva = 1;
        } catch (IllegalArgumentException e) {
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error al guardar: " + e.getMessage(),null));
        } catch (Exception e) {
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"Ocurrió un error al guardar la asignación",null));
        }
    }

    /**
     * Elimina rol y actualiza el estado de la vista.
     */
    public void eliminarRol() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (empleadoRol == null || empleadoRol.getIdEmpleadoRol() == null) {
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"Debe seleccionar una asignación",null));
            return;
        }
        try {
            empleadoRolDAO.eliminar(empleadoRol.getIdEmpleadoRol());
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO,"Se eliminó la asignación del rol",null));
            prepararRol(getSeleccionado());
            pestanaActiva = 1;
        } catch (IllegalArgumentException e) {
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error al eliminar: " + e.getMessage(),null));
        } catch (Exception e) {
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"No se pudo eliminar la asignación del rol",null));
        }
    }

    /**
     * Descarta la edición de rol.
     */
    public void cancelarRol() {
        prepararRol(getSeleccionado());
        pestanaActiva = 1;
    }

    /**
     * Restablece rol al estado inicial de la vista.
     */
    private void limpiarRol() {
        empleadoRol = null;
        empleadoRolSeleccionado = null;
        idRolSeleccionado = null;
        estadoRol = ESTADO_CRUD.NINGUNO;
    }

    /**
     * Obtiene roles para la vista o la operación actual.
     * @return roles actual
     */
    public List<Rol> getRoles() {
        return consultar(() -> rolDAO.findAll(), java.util.List.of());
    }

    /**
     * Obtiene empleado roles para la vista o la operación actual.
     * @return empleado roles actual
     */
    public List<EmpleadoRol> getEmpleadoRoles() {
        return getSeleccionado() == null ? List.of()
                : consultar(() -> empleadoRolDAO.findAll().stream()
                        .filter(r -> r.getIdEmpleado() != null && r.getIdEmpleado().getIdEmpleado()
                                .equals(getSeleccionado().getIdEmpleado())).toList(), List.of());
    }

    /**
     * Obtiene empleado rol para la vista o la operación actual.
     * @return empleado rol actual
     */
    public EmpleadoRol getEmpleadoRol() {
        return empleadoRol;
    }

    /**
     * Actualiza empleado rol con el valor recibido.
     * @param empleadoRol empleado rol utilizado por la operación
     */
    public void setEmpleadoRol(EmpleadoRol empleadoRol) {
        this.empleadoRol = empleadoRol;
    }

    /**
     * Obtiene empleado rol seleccionado para la vista o la operación actual.
     * @return empleado rol seleccionado actual
     */
    public EmpleadoRol getEmpleadoRolSeleccionado() {
        return empleadoRolSeleccionado;
    }

    /**
     * Actualiza empleado rol seleccionado con el valor recibido.
     * @param empleadoRolSeleccionado empleado rol seleccionado utilizado por la operación
     */
    public void setEmpleadoRolSeleccionado(EmpleadoRol empleadoRolSeleccionado) {
        this.empleadoRolSeleccionado = empleadoRolSeleccionado;
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
     * Obtiene estado rol para la vista o la operación actual.
     * @return estado rol actual
     */
    public ESTADO_CRUD getEstadoRol() {
        return estadoRol;
    }

    /**
     * Actualiza estado rol con el valor recibido.
     * @param estadoRol estado rol utilizado por la operación
     */
    public void setEstadoRol(ESTADO_CRUD estadoRol) {
        this.estadoRol = estadoRol;
    }

    /**
     * Obtiene pestana activa para la vista o la operación actual.
     * @return pestana activa actual
     */
    public int getPestanaActiva() {
        return pestanaActiva;
    }

    /**
     * Actualiza pestana activa con el valor recibido.
     * @param pestanaActiva pestana activa utilizado por la operación
     */
    public void setPestanaActiva(int pestanaActiva) {
        this.pestanaActiva = pestanaActiva;
    }

    /** Prepara una asignación sin borrar el empleado seleccionado.
     * @param empleado propietario de la asignación
     */
    private void prepararRol(Empleado empleado) {
        if (empleado == null) { limpiarRol(); return; }
        empleadoRol = new EmpleadoRol();
        empleadoRol.setIdEmpleadoRol(UUID.randomUUID());
        empleadoRol.setIdEmpleado(empleado);
        empleadoRol.setActivo(true);
        empleadoRolSeleccionado = null;
        idRolSeleccionado = null;
        estadoRol = ESTADO_CRUD.NUEVO;
    }

    /** Inicia una asignación adicional para el empleado actual. */
    public void nuevoRol() { prepararRol(getSeleccionado()); pestanaActiva = 1; }
}
