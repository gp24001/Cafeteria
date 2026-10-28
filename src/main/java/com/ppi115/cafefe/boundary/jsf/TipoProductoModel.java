package com.ppi115.cafefe.boundary.jsf;

import jakarta.inject.Inject;

import com.ppi115.cafefe.control.TipoProductoDAO;
import com.ppi115.cafefe.control.ProductoTipoProductoDAO;
import com.ppi115.cafefe.entity.ProductoTipoProducto;
import com.ppi115.cafefe.entity.TipoProducto;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import java.util.List;
import java.util.Date;

/** Estado y acciones JSF de categoría de productos. Coordina la vista y delega el acceso a datos en los DAO. */
@Named("tipoProductoModel")
@ViewScoped
public class TipoProductoModel extends AbstractModel<TipoProducto> implements Serializable {

    @Inject
    private TipoProductoDAO dao;

    /**
     * Proporciona el DAO utilizado para leer y guardar los registros de esta vista.
     * @return dao actual
     */
    @Override
    public TipoProductoDAO getDao() {
        return dao;
    }

    /**
     * Construye un registro nuevo con su UUID y los valores iniciales de esta vista.
     * @return registro nuevo con su identificador y valores iniciales
     */
    @Override
    public TipoProducto createRegistrer() {
        TipoProducto r = new TipoProducto();
        r.setIdTipoProducto(UUID.randomUUID());
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
    public Object getIdByRegistrer(TipoProducto registro) {
        if (registro == null) {
            return null;
        }
        return registro.getIdTipoProducto();
    }

    /**
     * Obtiene nombre bean para la vista o la operación actual.
     * @return nombre bean actual
     */
    @Override
    public String getNombreBean() {
        return "Tipo de Producto";
    }

    @Inject private ProductoTipoProductoDAO asignacionDAO;
    private final AsignacionesProducto asignacionesProducto = new AsignacionesProducto();

    /**
     * Obtiene asignaciones producto para la vista o la operación actual.
     * @return asignaciones producto actual
     */
    public AsignacionesProducto getAsignacionesProducto() { return asignacionesProducto; }

    /** CRUD de las asignaciones; reutiliza AbstractModel y conserva el producto del formulario principal. */
    public class AsignacionesProducto extends AbstractModel<ProductoTipoProducto> {
        private com.ppi115.cafefe.entity.Producto producto;
        private TipoProducto catalogoSeleccionado;
        private List<ProductoTipoProducto> asignaciones = List.of();

        /** @return validador del bean administrado por CDI */
        @Override
        protected jakarta.validation.Validator getValidator() { return TipoProductoModel.this.getValidator(); }

        /** @return DAO de las asignaciones */
        @Override
        public ProductoTipoProductoDAO getDao() { return asignacionDAO; }

        /**
         * Prepara una asignación del producto actual.
         * @return registro nuevo, o null si no se eligió un producto guardado
         */
        @Override
        public ProductoTipoProducto createRegistrer() {
            if (producto == null) return null;
            ProductoTipoProducto r = new ProductoTipoProducto();
            r.setIdProductoTipoProducto(UUID.randomUUID());
            r.setIdProducto(producto);
            r.setActivo(true); r.setFechaCreacion(new Date());
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
        public Object getIdByRegistrer(ProductoTipoProducto r) { return r == null ? null : r.getIdProductoTipoProducto(); }

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
        public void seleccionar(org.primefaces.event.SelectEvent<ProductoTipoProducto> event) {
            if (event == null || event.getObject() == null || !asignaciones.contains(event.getObject())) return;
            super.seleccionar(event);
            catalogoSeleccionado = getRegistro().getIdTipoProducto();
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
            if (getRegistro().getActivo() == null || getRegistro().getFechaCreacion() == null) {
                informarError(new IllegalArgumentException("El estado y la fecha de creación son obligatorios"));
                return;
            }
            ProductoTipoProducto guardado = getRegistro();
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
        public void seleccionarDisponible(TipoProducto valor) {
            if (getEstado() != ESTADO_CRUD.NUEVO || getRegistro() == null || valor == null || !Boolean.TRUE.equals(valor.getActivo())) {
                informarError(new IllegalArgumentException("Debe elegir una opción activa para el producto"));
                return;
            }
            catalogoSeleccionado = valor;
            getRegistro().setIdTipoProducto(valor);
        }

        /** @return opción actualmente elegida del catálogo */
        public TipoProducto getCatalogoSeleccionado() { return catalogoSeleccionado; }
        /** @return asignaciones del producto seleccionado */
        public List<ProductoTipoProducto> getAsignaciones() { return asignaciones; }
        /** @return opciones del catálogo disponibles para el selector */
        public List<TipoProducto> getDisponibles() {
            return consultar(() -> TipoProductoModel.this.getDao().findAll(), List.of());
        }

        /** @return identificador del catálogo presentado por el selector compacto */
        public UUID getIdCatalogoSeleccionado() {
            return catalogoSeleccionado == null ? null : catalogoSeleccionado.getIdTipoProducto();
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
                if (getRegistro() != null) getRegistro().setIdTipoProducto(null);
                return;
            }
            TipoProducto elegido = consultar(() -> TipoProductoModel.this.getDao().findById(id), null);
            seleccionarDisponible(elegido);
        }
    }
}
