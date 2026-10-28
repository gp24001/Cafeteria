package com.ppi115.cafefe.boundary.jsf;

import jakarta.inject.Inject;

import com.ppi115.cafefe.control.DescuentoProductoDAO;
import com.ppi115.cafefe.control.OrdenDAO;
import com.ppi115.cafefe.control.OrdenProductoDAO;
import com.ppi115.cafefe.control.ProductoDAO;
import com.ppi115.cafefe.entity.Descuento;
import com.ppi115.cafefe.entity.DescuentoProducto;
import com.ppi115.cafefe.entity.EmpleadoRol;
import com.ppi115.cafefe.entity.Orden;
import com.ppi115.cafefe.entity.OrdenProducto;
import com.ppi115.cafefe.entity.Producto;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.primefaces.event.SelectEvent;

/** Estado y acciones JSF de orden de venta. Coordina la vista y delega el acceso a datos en los DAO. */
@Named("ordenModel")
@ViewScoped
public class OrdenModel extends AbstractModel<Orden> implements Serializable {

    @Inject
    private OrdenDAO dao;

    @Inject
    private OrdenProductoDAO ordenProductoDao;

    @Inject
    private ProductoDAO productoDao;

    @Inject
    private DescuentoProductoDAO descuentoProductoDAO;

    // Líneas de la orden en edición (en memoria hasta pulsar Guardar)
    private List<OrdenProducto> lineas = new ArrayList<>();
    private final List<UUID> lineasEliminadas = new ArrayList<>();

    private Producto productoSeleccionado;
    private OrdenProducto lineaSeleccionada;
    private Date fechaDescuentos;
    private final Map<UUID, java.math.BigDecimal> preciosSinDescuento = new LinkedHashMap<>();
    private final Map<UUID, String> observacionesSinDescuento = new LinkedHashMap<>();

    // Descuentos
    private List<Descuento> descuentosAplicables = new ArrayList<>();
    private Descuento descuentoSeleccionado;

    // Pestaña activa (0 = Generalidades, 1 = Productos)
    private int tabActivo = 0;

    // Combos (se cargan una vez por vista)
    private List<EmpleadoRol> camareros;
    private List<Producto> productos;

    /**
     * Inicializa OrdenModel con los datos recibidos.
     */
    public OrdenModel() {
        setNombreBean("Orden");
    }

    // ========================================
    // Métodos obligatorios de AbstractModel
    // ========================================

    /**
     * Proporciona el DAO utilizado para leer y guardar los registros de esta vista.
     * @return dao actual
     */
    @Override
    public OrdenDAO getDao() {
        return dao;
    }

    /**
     * Construye un registro nuevo con su UUID y los valores iniciales de esta vista.
     * @return registro nuevo con su identificador y valores iniciales
     */
    @Override
    public Orden createRegistrer() {
        Orden o = new Orden(UUID.randomUUID());
        o.setFechaOrden(new Date());
        return o;
    }

    /**
     * Recupera el registro solicitado a través del DAO de la vista.
     * @param id identificador del registro solicitado
     * @return registro encontrado, o null si no existe
     */
    @Override
    public Object getRegistrerById(Object id) {
        return id == null ? null : consultar(() -> dao.findById(id), null);
    }

    /**
     * Obtiene el identificador usado por la selección y las tablas de JSF.
     * @param registro registro que se desea consultar o editar
     * @return identificador del registro, o null si no hay registro
     */
    @Override
    public Object getIdByRegistrer(Orden registro) {
        return registro == null ? null : registro.getIdOrden();
    }

    // ========================================
    // Ciclo CRUD (se enganchan las líneas)
    // ========================================

    /**
     * Prepara el formulario para introducir un registro nuevo.
     */
    @Override
    public void nuevo() {
        super.nuevo();
        limpiarLineas();
    }

    /**
     * Carga el registro indicado y activa el modo de edición.
     * @param object object utilizado por la operación
     */
    @Override
    public void editar(Orden object) {
        super.editar(object);
        cargarLineas();
    }

    /**
     * Carga la fila elegida para editarla sin cambiar su identificador.
     * @param event evento generado por la selección o acción de la vista
     */
    @Override
    public void seleccionar(SelectEvent<Orden> event) {
        super.seleccionar(event);
        cargarLineas();
    }

    /**
     * Descarta la edición actual y restablece el estado del formulario correspondiente.
     */
    @Override
    public void cancelar() {
        super.cancelar();
        limpiarLineas();
    }

    /**
     * Elimina el registro indicado y comunica los errores al nivel que invoca la operación.
     * @param object object utilizado por la operación

     */
    @Override
    public void eliminar(Orden object) {
        super.eliminar(object);
        limpiarLineas();
    }

    /**
     * Valida el formulario y delega la escritura en la capa de persistencia.
     */
    @Override
    public void guardar() {
        if (getRegistro() == null) {
            mensaje(FacesMessage.SEVERITY_WARN, "El registro no puede ser nulo");
            return;
        }
        try {
            boolean esNueva = getEstado() == ESTADO_CRUD.NUEVO;
            if (fechaDescuentos != null && !fechaDescuentos.equals(getRegistro().getFechaOrden())) {
                cambiarFecha();
                informarError(new IllegalArgumentException("Cambió la fecha: revise y aplique nuevamente los descuentos antes de guardar"));
                return;
            }
            if (getRegistro().getFechaOrden() == null) {
                getRegistro().setFechaOrden(new Date());
            }
            if (getEstado() != ESTADO_CRUD.NUEVO && getEstado() != ESTADO_CRUD.EDITAR)
                throw new IllegalArgumentException("Debe crear o seleccionar una orden");
            if (getRegistro().getIdEmpleadoRol() == null
                    || !Boolean.TRUE.equals(getRegistro().getIdEmpleadoRol().getActivo()))
                throw new IllegalArgumentException("Debe seleccionar una asignación de empleado activa");
            var errores = getValidator().validate(getRegistro());
            if (!errores.isEmpty()) throw new IllegalArgumentException(errores.iterator().next().getMessage());
            for (OrdenProducto linea : lineas) {
                if (linea == null || linea.getIdProducto() == null || linea.getPrecio() == null
                        || linea.getPrecio().signum() < 0)
                    throw new IllegalArgumentException("Cada línea debe tener producto y precio no negativo");
            }
            dao.guardarConProductos(getRegistro(), esNueva, lineas, lineasEliminadas);
            mensaje(FacesMessage.SEVERITY_INFO, esNueva ? "Se creó la orden" : "Se modificó la orden");

            Orden guardada = getRegistro();
            UUID idLinea = lineaSeleccionada == null ? null : lineaSeleccionada.getIdOrdenProducto();
            setRegistro(guardada);
            setEstado(ESTADO_CRUD.EDITAR);
            setSeleccionado(guardada);
            cargarLineas();
            lineaSeleccionada = lineas.stream().filter(l -> l.getIdOrdenProducto().equals(idLinea)).findFirst().orElse(null);
            tabActivo = 1;
            if (getModel() != null) {
                getModel().setRowCount(dao.contar());
            }
        } catch (IllegalArgumentException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Error al guardar: " + e.getMessage());
        } catch (Exception e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Ocurrió un error inesperado al guardar");
        }
    }

    // ========================================
    // Productos de la orden
    // ========================================

    /**
     * Añade el producto elegido a las líneas de la orden en edición.
     * @param event evento generado por la selección o acción de la vista
     */
    public void agregarProducto(ActionEvent event) {
        if (getEstado() != ESTADO_CRUD.NUEVO || getRegistro() == null || productoSeleccionado == null || !productoSeleccionado.getActivo()) {
            mensaje(FacesMessage.SEVERITY_WARN, "Seleccione un producto");
            return;
        }
        OrdenProducto l = new OrdenProducto(UUID.randomUUID());
        l.setIdOrden(getRegistro());
        l.setIdProducto(productoSeleccionado);
        l.setPrecio(productoSeleccionado.getPrecioSugerido());
        lineas.add(l);
        lineaSeleccionada = l;
        productoSeleccionado = null;
        tabActivo = 1;
        cargarDescuentos(null);
        mensaje(FacesMessage.SEVERITY_INFO, "Producto agregado a la orden");
    }

    /**
     * Retira una línea de la edición y registra su eliminación si ya estaba guardada.
     * @param linea linea utilizado por la operación
     */
    public void quitarLinea(OrdenProducto linea) {
        if (getEstado() != ESTADO_CRUD.NUEVO || linea == null) {
            return;
        }
        lineas.remove(linea);
        preciosSinDescuento.remove(linea.getIdOrdenProducto());
        observacionesSinDescuento.remove(linea.getIdOrdenProducto());
        if (linea.equals(lineaSeleccionada)) {
            lineaSeleccionada = null;
            descuentosAplicables.clear();
            descuentoSeleccionado = null;
        }
        // Solo se borra de la BD si ya estaba guardada
        if (getEstado() == ESTADO_CRUD.EDITAR) {
            lineasEliminadas.add(linea.getIdOrdenProducto());
        }
    }

    // ========================================
    // Descuentos
    // ========================================

    /** Lista únicamente los descuentos del producto vigentes en la fecha de la orden. */
    public void cargarDescuentos(ActionEvent event) {
        descuentoSeleccionado = null;
        descuentosAplicables = new ArrayList<>();
        if (getEstado() != ESTADO_CRUD.NUEVO || lineaSeleccionada == null || !lineas.contains(lineaSeleccionada)) return;
        try {
            Map<UUID, Descuento> unicos = new LinkedHashMap<>();
            for (DescuentoProducto dp : descuentoProductoDAO.findVigentes(java.util.List.of(lineaSeleccionada.getIdProducto()), getRegistro().getFechaOrden()))
                unicos.putIfAbsent(dp.getIdDescuento().getIdDescuento(), dp.getIdDescuento());
            descuentosAplicables = new ArrayList<>(unicos.values());
        } catch (RuntimeException e) {
            informarError(e);
        }
    }

    /** Botón "Aplicar" del diálogo. */
    public void aplicarDescuento(ActionEvent event) {
        if (getEstado() != ESTADO_CRUD.NUEVO || getRegistro() == null || lineaSeleccionada == null
                || !lineas.contains(lineaSeleccionada) || descuentoSeleccionado == null
                || !descuentosAplicables.contains(descuentoSeleccionado)) {
            informarError(new IllegalArgumentException("Seleccione una línea de producto y uno de sus descuentos al crear la orden"));
            return;
        }
        try {
            if (fechaDescuentos != null && !fechaDescuentos.equals(getRegistro().getFechaOrden())) {
                Descuento solicitado = descuentoSeleccionado;
                cambiarFecha();
                descuentoSeleccionado = solicitado;
            }
            if (getRegistro().getFechaOrden() == null)
                throw new IllegalArgumentException("La fecha de la orden es obligatoria");
            fechaDescuentos = new Date(getRegistro().getFechaOrden().getTime());
            UUID idLinea = lineaSeleccionada.getIdOrdenProducto();
            preciosSinDescuento.putIfAbsent(idLinea, lineaSeleccionada.getPrecio());
            if (!observacionesSinDescuento.containsKey(idLinea))
                observacionesSinDescuento.put(idLinea, lineaSeleccionada.getObservaciones());
            DescuentoProductoDAO.Resultado r = descuentoProductoDAO.aplicar(descuentoSeleccionado,
                    lineaSeleccionada, getRegistro().getFechaOrden());
            if (r.aplicadas > 0) mensaje(FacesMessage.SEVERITY_INFO, "Descuento aplicado al producto seleccionado");
            else if (r.repetidas > 0) mensaje(FacesMessage.SEVERITY_WARN, "Ese descuento ya está aplicado a este producto");
            else mensaje(FacesMessage.SEVERITY_WARN, "El descuento no aplica a este producto en la fecha de la orden");
        } catch (RuntimeException e) {
            informarError(e);
        }
    }

    // ========================================
    // Internos
    // ========================================

    /**
     * Recupera las líneas guardadas de la orden seleccionada.
     */
    private void cargarLineas() {
        if (getRegistro() == null || getRegistro().getIdOrden() == null) return;
        lineas = new ArrayList<>(consultar(() -> ordenProductoDao.findByOrden(getRegistro().getIdOrden()), List.of()));
        lineasEliminadas.clear();
        lineaSeleccionada = null;
        descuentosAplicables.clear();
        preciosSinDescuento.clear();
        fechaDescuentos = null;
        observacionesSinDescuento.clear();
        productoSeleccionado = null;
        descuentoSeleccionado = null;
        tabActivo = 0;
    }

    /**
     * Vacía las líneas y las selecciones temporales de la orden.
     */
    private void limpiarLineas() {
        lineas = new ArrayList<>();
        lineasEliminadas.clear();
        lineaSeleccionada = null;
        descuentosAplicables.clear();
        preciosSinDescuento.clear();
        fechaDescuentos = null;
        observacionesSinDescuento.clear();
        productoSeleccionado = null;
        descuentoSeleccionado = null;
        tabActivo = 0;
    }

    /**
     * Añade un mensaje a FacesContext para informar del resultado de una acción.
     * @param sev sev utilizado por la operación
     * @param texto texto utilizado por la operación
     */
    private void mensaje(FacesMessage.Severity sev, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(sev, texto, null));
    }

    // ========================================
    // Combos
    // ========================================

    /**
     * Obtiene camareros para la vista o la operación actual.
     * @return camareros actual
     */
    public List<EmpleadoRol> getCamareros() {
        if (camareros == null) {
            camareros = consultar(() -> dao.findCamareros(), List.of());
        }
        return camareros;
    }

    /**
     * Obtiene productos para la vista o la operación actual.
     * @return productos actual
     */
    public List<Producto> getProductos() {
        if (productos == null) {
            productos = new ArrayList<>();
            for (Producto p : consultar(() -> productoDao.findAll(), List.<Producto>of())) {
                if (p.getActivo()) {
                    productos.add(p);
                }
            }
        }
        return productos;
    }

    // ========================================
    // Getters / setters
    // ========================================

    /**
     * Obtiene lineas para la vista o la operación actual.
     * @return lineas actual
     */
    public List<OrdenProducto> getLineas() {
        return lineas;
    }

    /**
     * Obtiene producto seleccionado para la vista o la operación actual.
     * @return producto seleccionado actual
     */
    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    /**
     * Actualiza producto seleccionado con el valor recibido.
     * @param productoSeleccionado producto seleccionado utilizado por la operación
     */
    public void setProductoSeleccionado(Producto productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }

    /**
     * Obtiene descuentos aplicables para la vista o la operación actual.
     * @return descuentos aplicables actual
     */
    public List<Descuento> getDescuentosAplicables() {
        return descuentosAplicables;
    }

    /**
     * Obtiene descuento seleccionado para la vista o la operación actual.
     * @return descuento seleccionado actual
     */
    public Descuento getDescuentoSeleccionado() {
        return descuentoSeleccionado;
    }

    /**
     * Actualiza descuento seleccionado con el valor recibido.
     * @param descuentoSeleccionado descuento seleccionado utilizado por la operación
     */
    public void setDescuentoSeleccionado(Descuento descuentoSeleccionado) {
        this.descuentoSeleccionado = descuentoSeleccionado;
    }

    /**
     * Obtiene tab activo para la vista o la operación actual.
     * @return tab activo actual
     */
    public int getTabActivo() {
        return tabActivo;
    }

    /**
     * Actualiza tab activo con el valor recibido.
     * @param tabActivo tab activo utilizado por la operación
     */
    public void setTabActivo(int tabActivo) {
        this.tabActivo = tabActivo;
    }

    /**
     * Carga los descuentos de la única línea elegida en la tabla.
     * @param event fila seleccionada de la orden
     */
    public void seleccionarLinea(SelectEvent<OrdenProducto> event) {
        if (event == null || event.getObject() == null || !lineas.contains(event.getObject())) return;
        lineaSeleccionada = event.getObject();
        cargarDescuentos(null);
    }

    /** Recalcula el formulario al cambiar la fecha, retirando los descuentos de la fecha anterior. */
    public void cambiarFecha() {
        if (getEstado() != ESTADO_CRUD.NUEVO) return;
        fechaDescuentos = null;
        for (OrdenProducto linea : lineas) {
            UUID id = linea.getIdOrdenProducto();
            if (preciosSinDescuento.containsKey(id)) {
                linea.setPrecio(preciosSinDescuento.get(id));
                linea.setObservaciones(observacionesSinDescuento.get(id));
            }
        }
        cargarDescuentos(null);
    }

    /** @return línea que recibirá el descuento */
    public OrdenProducto getLineaSeleccionada() { return lineaSeleccionada; }

    /** @param linea fila seleccionada, que debe pertenecer a esta orden */
    public void setLineaSeleccionada(OrdenProducto linea) {
        lineaSeleccionada = linea != null && lineas.contains(linea) ? linea : null;
    }

    /** @return suma de los importes actuales de las líneas */
    public java.math.BigDecimal getTotal() {
        return lineas.stream().map(OrdenProducto::getPrecio).filter(java.util.Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    /** @return identificador del camarero elegido para la orden */
    public UUID getIdEmpleadoRolSeleccionado() {
        return getRegistro() == null || getRegistro().getIdEmpleadoRol() == null
                ? null : getRegistro().getIdEmpleadoRol().getIdEmpleadoRol();
    }

    /**
     * Resuelve el identificador únicamente dentro de los camareros permitidos.
     * @param id asignación de empleado elegida en el formulario
     */
    public void setIdEmpleadoRolSeleccionado(UUID id) {
        if (getRegistro() == null) return;
        if (getEstado() != ESTADO_CRUD.NUEVO) {
            if (!java.util.Objects.equals(id, getIdEmpleadoRolSeleccionado()))
                informarError(new IllegalArgumentException("El camarero no puede cambiarse durante la edición"));
            return;
        }
        if (id == null) { getRegistro().setIdEmpleadoRol(null); return; }
        EmpleadoRol elegido = getCamareros().stream()
                .filter(e -> id.equals(e.getIdEmpleadoRol())).findFirst().orElse(null);
        if (elegido == null) {
            informarError(new IllegalArgumentException("Debe seleccionar un camarero activo"));
            return;
        }
        getRegistro().setIdEmpleadoRol(elegido);
    }

    /** @return identificador del producto preparado para agregar a la orden */
    public UUID getIdProductoSeleccionado() {
        return productoSeleccionado == null ? null : productoSeleccionado.getIdProducto();
    }

    /** @param id producto activo elegido en el catálogo */
    public void setIdProductoSeleccionado(UUID id) {
        if (getEstado() != ESTADO_CRUD.NUEVO) {
            if (!java.util.Objects.equals(id, getIdProductoSeleccionado()))
                informarError(new IllegalArgumentException("Los productos solo se seleccionan al crear la orden"));
            return;
        }
        if (id == null) { productoSeleccionado = null; return; }
        Producto elegido = getProductos().stream().filter(p -> id.equals(p.getIdProducto())).findFirst().orElse(null);
        if (elegido == null) {
            informarError(new IllegalArgumentException("Debe seleccionar un producto activo"));
            return;
        }
        productoSeleccionado = elegido;
    }

    /** @return identificador del descuento elegido para la línea actual */
    public UUID getIdDescuentoSeleccionado() {
        return descuentoSeleccionado == null ? null : descuentoSeleccionado.getIdDescuento();
    }

    /** @param id descuento de los asignados al producto de la línea actual */
    public void setIdDescuentoSeleccionado(UUID id) {
        if (getEstado() != ESTADO_CRUD.NUEVO) {
            if (!java.util.Objects.equals(id, getIdDescuentoSeleccionado()))
                informarError(new IllegalArgumentException("Los descuentos solo se seleccionan al crear la orden"));
            return;
        }
        if (id == null) { descuentoSeleccionado = null; return; }
        Descuento elegido = descuentosAplicables.stream()
                .filter(d -> id.equals(d.getIdDescuento())).findFirst().orElse(null);
        if (elegido == null) {
            informarError(new IllegalArgumentException("El descuento no pertenece al producto seleccionado"));
            return;
        }
        descuentoSeleccionado = elegido;
    }
}
