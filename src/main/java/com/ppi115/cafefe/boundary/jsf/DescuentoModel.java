package com.ppi115.cafefe.boundary.jsf;

import jakarta.inject.Inject;

import com.ppi115.cafefe.control.DescuentoDAO;
import com.ppi115.cafefe.control.DescuentoProductoDAO;
import com.ppi115.cafefe.entity.DescuentoProducto;
import com.ppi115.cafefe.control.TipoDescuentoDAO;
import com.ppi115.cafefe.entity.Descuento;
import com.ppi115.cafefe.entity.TipoDescuento;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;

/** Estado y acciones JSF de descuento y su vigencia. Coordina la vista y delega el acceso a datos en los DAO. */
@Named("descuentoModel")
@ViewScoped
public class DescuentoModel extends AbstractModel<Descuento> implements Serializable {

    @Inject
    private DescuentoDAO dao;

    @Inject
    private TipoDescuentoDAO tipoDescuentoDAO;

    private UUID idTipoDescuentoSeleccionado;

    /**
     * Inicializa DescuentoModel con los datos recibidos.
     */
    public DescuentoModel() {
        setNombreBean("Descuento");
    }

    /**
     * Proporciona el DAO utilizado para leer y guardar los registros de esta vista.
     * @return dao actual
     */
    @Override
    public DescuentoDAO getDao() {
        return dao;
    }

    /**
     * Construye un registro nuevo con su UUID y los valores iniciales de esta vista.
     * @return registro nuevo con su identificador y valores iniciales
     */
    @Override
    public Descuento createRegistrer() {
        Descuento r = new Descuento();
        r.setIdDescuento(UUID.randomUUID());
        r.setFechaInicio(new Date());
        idTipoDescuentoSeleccionado = null;
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
    public Object getIdByRegistrer(Descuento registro) {
        if (registro == null) return null;
        return registro.getIdDescuento();
    }

    /**
     * Valida el formulario y delega la escritura en la capa de persistencia.
     */
    @Override
    public void guardar() {
        if (getRegistro() == null) {
            informarError(new IllegalArgumentException("Debe crear o seleccionar un registro"));
            return;
        }
        try {
            if (getRegistro().getFechaInicio() != null
                    && getRegistro().getFechaFin() != null
                    && getRegistro().getFechaFin().before(getRegistro().getFechaInicio())) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "La fecha fin no puede ser anterior a la fecha de inicio",
                        null));
                return;
            }

            if (getEstado() == ESTADO_CRUD.NUEVO) {
                if (idTipoDescuentoSeleccionado != null) {
                    TipoDescuento tipo = tipoDescuentoDAO.findById(idTipoDescuentoSeleccionado);

                    if (tipo != null && !Boolean.TRUE.equals(tipo.getActivo())) {
                        FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(
                                FacesMessage.SEVERITY_WARN,
                                "No se puede asignar un tipo de descuento inactivo",
                                null));
                        return;
                    }

                    getRegistro().setIdTipoDescuento(tipo);
                } else {
                    getRegistro().setIdTipoDescuento(null);
                }
            }

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
    public void editar(Descuento registro) {
        super.editar(registro);
        cargarTipo(registro);
    }

    /**
     * Carga la fila elegida para editarla sin cambiar su identificador.
     * @param event evento generado por la selección o acción de la vista
     */
    @Override
    public void seleccionar(SelectEvent<Descuento> event) {
        super.seleccionar(event);

        if (event != null) {
            cargarTipo(event.getObject());
        }
    }

    /**
     * Sincroniza el selector con el tipo asociado al registro actual.
     * @param registro registro que se desea consultar o editar
     */
    private void cargarTipo(Descuento registro) {
        if (registro != null && registro.getIdTipoDescuento() != null) {
            idTipoDescuentoSeleccionado =
                registro.getIdTipoDescuento().getIdTipoDescuento();
        } else {
            idTipoDescuentoSeleccionado = null;
        }
    }

    /**
     * Obtiene tipos descuento para la vista o la operación actual.
     * @return tipos descuento actual
     */
    public List<TipoDescuento> getTiposDescuento() {
        return consultar(() -> tipoDescuentoDAO.findAll(), java.util.List.of());
    }

    /**
     * Obtiene id tipo descuento seleccionado para la vista o la operación actual.
     * @return id tipo descuento seleccionado actual
     */
    public UUID getIdTipoDescuentoSeleccionado() {
        return idTipoDescuentoSeleccionado;
    }

    /**
     * Actualiza id tipo descuento seleccionado con el valor recibido.
     * @param idTipoDescuentoSeleccionado id tipo descuento seleccionado utilizado por la operación
     */
    public void setIdTipoDescuentoSeleccionado(UUID idTipoDescuentoSeleccionado) {
        this.idTipoDescuentoSeleccionado = idTipoDescuentoSeleccionado;
    }

    @Inject private DescuentoProductoDAO asignacionDAO;
    private final AsignacionesProducto asignacionesProducto = new AsignacionesProducto();

    /**
     * Obtiene asignaciones producto para la vista o la operación actual.
     * @return asignaciones producto actual
     */
    public AsignacionesProducto getAsignacionesProducto() { return asignacionesProducto; }

    /** CRUD de las asignaciones; reutiliza AbstractModel y conserva el producto del formulario principal. */
    public class AsignacionesProducto extends AbstractModel<DescuentoProducto> {
        private com.ppi115.cafefe.entity.Producto producto;
        private Descuento catalogoSeleccionado;
        private List<DescuentoProducto> asignaciones = List.of();

        /** @return validador del bean administrado por CDI */
        @Override
        protected jakarta.validation.Validator getValidator() { return DescuentoModel.this.getValidator(); }

        /** @return DAO de las asignaciones */
        @Override
        public DescuentoProductoDAO getDao() { return asignacionDAO; }

        /**
         * Prepara una asignación del producto actual.
         * @return registro nuevo, o null si no se eligió un producto guardado
         */
        @Override
        public DescuentoProducto createRegistrer() {
            if (producto == null) return null;
            DescuentoProducto r = new DescuentoProducto();
            r.setIdDescuentoProducto(UUID.randomUUID());
            r.setIdProducto(producto);
            r.setFechaInicio(new Date());
            return r;
        }

        /**
         * @param id identificador de asignación
         * @return registro encontrado o null
         */
        @Override
        public Object getRegistrerById(Object id) {
            return id == null ? null : consultar(() -> asignacionDAO.findById(id), null);
        }

        /**
         * @param r asignación que se consulta
         * @return identificador o null
         */
        @Override
        public Object getIdByRegistrer(DescuentoProducto r) { return r == null ? null : r.getIdDescuentoProducto(); }

        /**
         * Carga las asignaciones de un producto y prepara su formulario.
         * @param producto producto seleccionado en la vista principal
         */
        public void cargar(com.ppi115.cafefe.entity.Producto producto) {
            this.producto = producto;
            recargar();
            nuevo();
        }

        /** Recarga solo la lista de esta pestaña. */
        private void recargar() {
            asignaciones = producto == null ? List.of()
                    : consultar(() -> asignacionDAO.findByIdProducto(producto.getIdProducto()), List.of());
        }

        /** Inicia una nueva asignación sin borrar el producto. */
        @Override
        public void nuevo() {
            super.nuevo();
            setSeleccionado(null);
            catalogoSeleccionado = null;
            if (producto == null) setEstado(ESTADO_CRUD.NINGUNO);
        }

        /** @param event fila elegida dentro de las asignaciones del producto */
        @Override
        public void seleccionar(org.primefaces.event.SelectEvent<DescuentoProducto> event) {
            if (event == null || event.getObject() == null || !asignaciones.contains(event.getObject())) return;
            super.seleccionar(event);
            catalogoSeleccionado = getRegistro().getIdDescuento();
        }

        /** Guarda mediante el CRUD original y restaura la asignación guardada como selección. */
        @Override
        public void guardar() {
            if (producto == null || getRegistro() == null) {
                informarError(new IllegalArgumentException("Debe seleccionar un producto guardado"));
                return;
            }
            if (!producto.getActivo()) {
                informarError(new IllegalArgumentException("El producto está inactivo"));
                return;
            }
            var r = getRegistro();
            var tipo = catalogoSeleccionado == null ? null : catalogoSeleccionado.getIdTipoDescuento();
            if (tipo == null || r.getValor() == null || r.getValor() <= 0 || r.getValor() > 100
                    || (tipo.getDescuentoMaximo() != null && r.getValor() > tipo.getDescuentoMaximo())
                    || r.getFechaInicio() == null
                    || (r.getFechaFin() != null && r.getFechaFin().before(r.getFechaInicio()))) {
                informarError(new IllegalArgumentException("Revise el tipo, el porcentaje y las fechas del descuento"));
                return;
            }
            DescuentoProducto guardado = getRegistro();
            super.guardar();
            if (getEstado() == ESTADO_CRUD.NINGUNO) {
                editar(guardado);
                recargar();
            }
        }

        /** Descarta la edición de la asignación, conservando el producto y la pestaña. */
        @Override
        public void cancelar() { cargar(producto); }

        /** Elimina la asignación elegida mediante el CRUD original. */
        public void eliminar() {
            if (getSeleccionado() == null) {
                informarError(new IllegalArgumentException("Debe seleccionar una asignación"));
                return;
            }
            super.eliminar(getSeleccionado());
            if (getEstado() == ESTADO_CRUD.NINGUNO) cargar(producto);
        }

        /** @param valor opción activa del catálogo que se asignará al producto */
        public void seleccionarDisponible(Descuento valor) {
            if (getEstado() != ESTADO_CRUD.NUEVO || getRegistro() == null || valor == null || (valor.getIdTipoDescuento() == null || !Boolean.TRUE.equals(valor.getIdTipoDescuento().getActivo()))) {
                informarError(new IllegalArgumentException("Debe elegir una opción activa para el producto"));
                return;
            }
            catalogoSeleccionado = valor;
            getRegistro().setIdDescuento(valor);
        }

        /** @return opción actualmente elegida del catálogo */
        public Descuento getCatalogoSeleccionado() { return catalogoSeleccionado; }
        /** @return asignaciones del producto seleccionado */
        public List<DescuentoProducto> getAsignaciones() { return asignaciones; }
        /** @return opciones del catálogo disponibles para el selector */
        public List<Descuento> getDisponibles() {
            return consultar(() -> DescuentoModel.this.getDao().findAll(), List.of());
        }

        /** @return identificador del catálogo presentado por el selector compacto */
        public UUID getIdCatalogoSeleccionado() {
            return catalogoSeleccionado == null ? null : catalogoSeleccionado.getIdDescuento();
        }

        /**
         * Cambia la relación exclusivamente al crear una asignación.
         * @param id identificador elegido en el catálogo
         */
        public void setIdCatalogoSeleccionado(UUID id) {
            if (getEstado() != ESTADO_CRUD.NUEVO) {
                if (!java.util.Objects.equals(id, getIdCatalogoSeleccionado()))
                    informarError(new IllegalArgumentException("La relación no puede cambiarse durante la edición"));
                return;
            }
            if (id == null) {
                catalogoSeleccionado = null;
                if (getRegistro() != null) getRegistro().setIdDescuento(null);
                return;
            }
            Descuento elegido = consultar(() -> DescuentoModel.this.getDao().findById(id), null);
            seleccionarDisponible(elegido);
        }
    }
}
