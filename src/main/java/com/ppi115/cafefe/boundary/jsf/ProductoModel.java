package com.ppi115.cafefe.boundary.jsf;

import jakarta.inject.Inject;

import com.ppi115.cafefe.control.ProductoDAO;
import com.ppi115.cafefe.entity.Producto;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.util.UUID;
import org.primefaces.event.SelectEvent;

/** Estado y acciones JSF de producto del catálogo. Coordina la vista y delega el acceso a datos en los DAO. */
@Named("productoModel")
@ViewScoped
public class ProductoModel extends AbstractModel<Producto> {
    @Inject private ProductoDAO dao;
    @Inject private TipoProductoModel tipos;
    @Inject private CaracteristicaModel caracteristicas;
    @Inject private DescuentoModel descuentos;
    private int pestanaActiva;

    /**
     * Inicializa ProductoModel con los datos recibidos.
     */
    public ProductoModel() { setNombreBean("Producto"); }
    /**
     * Proporciona el DAO utilizado para leer y guardar los registros de esta vista.
     * @return dao actual
     */
    @Override
    public ProductoDAO getDao() { return dao; }
    /**
     * Construye un registro nuevo con su UUID y los valores iniciales de esta vista.
     * @return registro nuevo con su identificador y valores iniciales
     */
    @Override
    public Producto createRegistrer() {
        Producto p = new Producto(UUID.randomUUID());
        p.setActivo(true);
        return p;
    }
    /**
     * Recupera el registro solicitado a través del DAO de la vista.
     * @param id identificador del registro solicitado
     * @return registro encontrado, o null si no existe
     */
    @Override
    public Object getRegistrerById(Object id) { return id == null ? null : consultar(() -> dao.findById(id), null); }
    /**
     * Obtiene el identificador usado por la selección y las tablas de JSF.
     * @param p p utilizado por la operación
     * @return identificador del registro, o null si no hay registro
     */
    @Override
    public Object getIdByRegistrer(Producto p) { return p == null ? null : p.getIdProducto(); }

    /**
     * Entrega el producto seleccionado a las secciones de categorías, características y descuentos.
     * @param producto producto al que pertenece la información de esta sección
     */
    private void cargarAsignaciones(Producto producto) {
        tipos.getAsignacionesProducto().cargar(producto);
        caracteristicas.getAsignacionesProducto().cargar(producto);
        descuentos.getAsignacionesProducto().cargar(producto);
    }
    /**
     * Carga la fila elegida para editarla sin cambiar su identificador.
     * @param event evento generado por la selección o acción de la vista
     */
    @Override
    public void seleccionar(SelectEvent<Producto> event) {
        if (event == null || event.getObject() == null) return;
        super.seleccionar(event);
        cargarAsignaciones(getSeleccionado());
    }
    /**
     * Valida el formulario y delega la escritura en la capa de persistencia.
     */
    @Override
    public void guardar() {
        if (getRegistro() == null || (getEstado() != ESTADO_CRUD.NUEVO && getEstado() != ESTADO_CRUD.EDITAR)) {
            informarError(new IllegalArgumentException("Debe crear o seleccionar un producto"));
            return;
        }
        boolean nuevo = getEstado() == ESTADO_CRUD.NUEVO;
        Producto producto = getRegistro();
        super.guardar();
        if (getEstado() == ESTADO_CRUD.NINGUNO) {
            editar(producto);
            if (nuevo) cargarAsignaciones(producto);
        }
    }
    /**
     * Prepara el formulario para introducir un registro nuevo.
     */
    @Override
    public void nuevo() {
        super.nuevo();
        setSeleccionado(null);
        cargarAsignaciones(null);
        pestanaActiva = 0;
    }
    /**
     * Descarta la edición actual y restablece el estado del formulario correspondiente.
     */
    @Override
    public void cancelar() {
        super.cancelar();
        cargarAsignaciones(null);
        pestanaActiva = 0;
    }
    /**
     * Elimina el registro indicado y comunica los errores al nivel que invoca la operación.
     * @param producto producto al que pertenece la información de esta sección

     */
    @Override
    public void eliminar(Producto producto) {
        super.eliminar(producto);
        if (getEstado() == ESTADO_CRUD.NINGUNO) {
            cargarAsignaciones(null);
            pestanaActiva = 0;
        }
    }
    /**
     * Obtiene pestana activa para la vista o la operación actual.
     * @return pestana activa actual
     */
    public int getPestanaActiva() { return pestanaActiva; }
    /**
     * Actualiza pestana activa con el valor recibido.
     * @param valor nuevo valor que se asignará
     */
    public void setPestanaActiva(int valor) { pestanaActiva = valor; }
}
