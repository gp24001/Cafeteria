package com.ppi115.cafefe.boundary.jsf;

import jakarta.inject.Inject;

import com.ppi115.cafefe.control.CaracteristicaDAO;
import com.ppi115.cafefe.control.ProductoCaracteristicaDAO;
import com.ppi115.cafefe.entity.ProductoCaracteristica;
import com.ppi115.cafefe.control.TipoCaracteristicaDAO;
import com.ppi115.cafefe.entity.Caracteristica;
import com.ppi115.cafefe.entity.TipoCaracteristica;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import org.primefaces.event.SelectEvent;

/** Estado y acciones JSF de característica configurable. Coordina la vista y delega el acceso a datos en los DAO. */
@Named("caracteristicaModel")
@ViewScoped
public class CaracteristicaModel extends AbstractModel<Caracteristica> implements Serializable {

    @Inject
    private CaracteristicaDAO dao;

    @Inject
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    private UUID idTipoCaracteristicaSeleccionado;

    /**
     * Inicializa CaracteristicaModel con los datos recibidos.
     */
    public CaracteristicaModel() {
        setNombreBean("Característica");
    }

    /**
     * Proporciona el DAO utilizado para leer y guardar los registros de esta vista.
     * @return dao actual
     */
    @Override
    public CaracteristicaDAO getDao() {
        return dao;
    }

    /**
     * Construye un registro nuevo con su UUID y los valores iniciales de esta vista.
     * @return registro nuevo con su identificador y valores iniciales
     */
    @Override
    public Caracteristica createRegistrer() {
        Caracteristica r = new Caracteristica();
        r.setIdCaracteristica(UUID.randomUUID());
        r.setActivo(true);
        idTipoCaracteristicaSeleccionado = null;
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
    public Object getIdByRegistrer(Caracteristica registro) {
        if (registro == null) return null;
        return registro.getIdCaracteristica();
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
            if (getEstado() == ESTADO_CRUD.NUEVO) {
                if (idTipoCaracteristicaSeleccionado == null) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Debe seleccionar un tipo de característica",
                            null));
                    return;
                }

                TipoCaracteristica tipo =
                    tipoCaracteristicaDAO.findById(idTipoCaracteristicaSeleccionado);

                if (tipo == null) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "El tipo de característica no existe",
                            null));
                    return;
                }

                if (!Boolean.TRUE.equals(tipo.getActivo())) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "No se puede asignar un tipo de característica inactivo",
                            null));
                    return;
                }

                getRegistro().setIdTipoCaracteristica(tipo);
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
    public void editar(Caracteristica registro) {
        super.editar(registro);
        cargarTipo(registro);
    }

    /**
     * Carga la fila elegida para editarla sin cambiar su identificador.
     * @param event evento generado por la selección o acción de la vista
     */
    @Override
    public void seleccionar(SelectEvent<Caracteristica> event) {
        super.seleccionar(event);

        if (event != null) {
            cargarTipo(event.getObject());
        }
    }

    /**
     * Sincroniza el selector con el tipo asociado al registro actual.
     * @param registro registro que se desea consultar o editar
     */
    private void cargarTipo(Caracteristica registro) {
        if (registro != null && registro.getIdTipoCaracteristica() != null) {
            idTipoCaracteristicaSeleccionado =
                registro.getIdTipoCaracteristica().getIdTipoCaracteristica();
        } else {
            idTipoCaracteristicaSeleccionado = null;
        }
    }

    /**
     * Obtiene tipos caracteristica para la vista o la operación actual.
     * @return tipos caracteristica actual
     */
    public List<TipoCaracteristica> getTiposCaracteristica() {
        return consultar(() -> tipoCaracteristicaDAO.findAll(), java.util.List.of());
    }

    /**
     * Obtiene id tipo caracteristica seleccionado para la vista o la operación actual.
     * @return id tipo caracteristica seleccionado actual
     */
    public UUID getIdTipoCaracteristicaSeleccionado() {
        return idTipoCaracteristicaSeleccionado;
    }

    /**
     * Actualiza id tipo caracteristica seleccionado con el valor recibido.
     * @param idTipoCaracteristicaSeleccionado id tipo caracteristica seleccionado utilizado por la operación
     */
    public void setIdTipoCaracteristicaSeleccionado(UUID idTipoCaracteristicaSeleccionado) {
        this.idTipoCaracteristicaSeleccionado = idTipoCaracteristicaSeleccionado;
    }

    @Inject private ProductoCaracteristicaDAO asignacionDAO;
    private final AsignacionesProducto asignacionesProducto = new AsignacionesProducto();

    /**
     * Obtiene asignaciones producto para la vista o la operación actual.
     * @return asignaciones producto actual
     */
    public AsignacionesProducto getAsignacionesProducto() { return asignacionesProducto; }

    /** CRUD de las asignaciones; reutiliza AbstractModel y conserva el producto del formulario principal. */
    public class AsignacionesProducto extends AbstractModel<ProductoCaracteristica> {
        private com.ppi115.cafefe.entity.Producto producto;
        private Caracteristica catalogoSeleccionado;
        private List<ProductoCaracteristica> asignaciones = List.of();

        /** @return validador del bean administrado por CDI */
        @Override
        protected jakarta.validation.Validator getValidator() { return CaracteristicaModel.this.getValidator(); }

        /** @return DAO de las asignaciones */
        @Override
        public ProductoCaracteristicaDAO getDao() { return asignacionDAO; }

        /**
         * Prepara una asignación del producto actual.
         * @return registro nuevo, o null si no se eligió un producto guardado
         */
        @Override
        public ProductoCaracteristica createRegistrer() {
            if (producto == null) return null;
            ProductoCaracteristica r = new ProductoCaracteristica();
            r.setIdProductoCaracteristica(UUID.randomUUID());
            r.setIdProducto(producto);

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
        public Object getIdByRegistrer(ProductoCaracteristica r) { return r == null ? null : r.getIdProductoCaracteristica(); }

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
        public void seleccionar(org.primefaces.event.SelectEvent<ProductoCaracteristica> event) {
            if (event == null || event.getObject() == null || !asignaciones.contains(event.getObject())) return;
            super.seleccionar(event);
            catalogoSeleccionado = getRegistro().getIdCaracteristica();
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
            if (getRegistro().getIdCaracteristica() != null && getRegistro().getValor() != null) {
                var tipo = getRegistro().getIdCaracteristica().getIdTipoCaracteristica();
                if (tipo != null && tipo.getExpresionRegular() != null && !tipo.getExpresionRegular().isBlank()) {
                    try {
                        if (!java.util.regex.Pattern.matches(tipo.getExpresionRegular(), getRegistro().getValor())) {
                            informarError(new IllegalArgumentException("El valor no cumple el formato de la característica"));
                            return;
                        }
                    } catch (java.util.regex.PatternSyntaxException e) {
                        informarError(new IllegalArgumentException("La expresión regular configurada no es válida", e));
                        return;
                    }
                }
            }
            ProductoCaracteristica guardado = getRegistro();
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
        public void seleccionarDisponible(Caracteristica valor) {
            if (getEstado() != ESTADO_CRUD.NUEVO || getRegistro() == null || valor == null || !Boolean.TRUE.equals(valor.getActivo())) {
                informarError(new IllegalArgumentException("Debe elegir una opción activa para el producto"));
                return;
            }
            catalogoSeleccionado = valor;
            getRegistro().setIdCaracteristica(valor);
        }

        /** @return opción actualmente elegida del catálogo */
        public Caracteristica getCatalogoSeleccionado() { return catalogoSeleccionado; }
        /** @return asignaciones del producto seleccionado */
        public List<ProductoCaracteristica> getAsignaciones() { return asignaciones; }
        /** @return opciones del catálogo disponibles para el selector */
        public List<Caracteristica> getDisponibles() {
            return consultar(() -> CaracteristicaModel.this.getDao().findAll(), List.of());
        }

        /** @return identificador del catálogo presentado por el selector compacto */
        public UUID getIdCatalogoSeleccionado() {
            return catalogoSeleccionado == null ? null : catalogoSeleccionado.getIdCaracteristica();
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
                if (getRegistro() != null) getRegistro().setIdCaracteristica(null);
                return;
            }
            Caracteristica elegido = consultar(() -> CaracteristicaModel.this.getDao().findById(id), null);
            seleccionarDisponible(elegido);
        }
    }
}
