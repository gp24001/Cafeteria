package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.DescuentoService;
import com.ppi115.cafefe.control.OrdenDAO;
import com.ppi115.cafefe.control.OrdenProductoDAO;
import com.ppi115.cafefe.control.OrdenService;
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
import jakarta.inject.Inject;
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
    private OrdenService service;

    @Inject
    private DescuentoService descuentoService;

    // Líneas de la orden en edición (en memoria hasta pulsar Guardar)
    private List<OrdenProducto> lineas = new ArrayList<>();
    private final List<UUID> lineasEliminadas = new ArrayList<>();

    private Producto productoSeleccionado;

    // Descuentos
    private List<Descuento> descuentosAplicables = new ArrayList<>();
    private Descuento descuentoSeleccionado;

    // Pestaña activa (0 = Generalidades, 1 = Productos)
    private int tabActivo = 0;

    // Combos (se cargan una vez por vista)
    private List<EmpleadoRol> camareros;
    private List<Producto> productos;

    public OrdenModel() {
        setNombreBean("Orden");
    }

    // ========================================
    // Métodos obligatorios de AbstractModel
    // ========================================

    @Override
    public OrdenDAO getDao() {
        return dao;
    }

    @Override
    public Orden createRegistrer() {
        Orden o = new Orden(UUID.randomUUID());
        o.setFechaOrden(new Date());
        return o;
    }

    @Override
    public Object getRegistrerById(Object id) {
        return id == null ? null : dao.findById(id);
    }

    @Override
    public Object getIdByRegistrer(Orden registro) {
        return registro == null ? null : registro.getIdOrden();
    }

    // ========================================
    // Ciclo CRUD (se enganchan las líneas)
    // ========================================

    @Override
    public void nuevo() {
        super.nuevo();
        limpiarLineas();
    }

    @Override
    public void editar(Orden object) {
        super.editar(object);
        cargarLineas();
    }

    @Override
    public void seleccionar(SelectEvent<Orden> event) {
        super.seleccionar(event);
        cargarLineas();
    }

    @Override
    public void cancelar() {
        super.cancelar();
        limpiarLineas();
    }

    @Override
    public void eliminar(Orden object) {
        super.eliminar(object);
        limpiarLineas();
    }

    @Override
    public void guardar() {
        if (getRegistro() == null) {
            mensaje(FacesMessage.SEVERITY_WARN, "El registro no puede ser nulo");
            return;
        }
        try {
            boolean esNueva = getEstado() == ESTADO_CRUD.NUEVO;
            if (getRegistro().getFechaOrden() == null) {
                getRegistro().setFechaOrden(new Date());
            }
            service.guardar(getRegistro(), esNueva, lineas, lineasEliminadas);
            mensaje(FacesMessage.SEVERITY_INFO, esNueva ? "Se creó la orden" : "Se modificó la orden");

            setRegistro(createRegistrer());
            setEstado(ESTADO_CRUD.NINGUNO);
            setSeleccionado(null);
            limpiarLineas();
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

    public void agregarProducto(ActionEvent event) {
        if (productoSeleccionado == null) {
            mensaje(FacesMessage.SEVERITY_WARN, "Seleccione un producto");
            return;
        }
        OrdenProducto l = new OrdenProducto(UUID.randomUUID());
        l.setIdOrden(getRegistro());
        l.setIdProducto(productoSeleccionado);
        l.setPrecio(productoSeleccionado.getPrecioSugerido());
        lineas.add(l);
        productoSeleccionado = null;
        mensaje(FacesMessage.SEVERITY_INFO, "Producto agregado a la orden");
    }

    public void quitarLinea(OrdenProducto linea) {
        if (linea == null) {
            return;
        }
        lineas.remove(linea);
        // Solo se borra de la BD si ya estaba guardada
        if (getEstado() == ESTADO_CRUD.EDITAR) {
            lineasEliminadas.add(linea.getIdOrdenProducto());
        }
    }

    // ========================================
    // Descuentos
    // ========================================

    /** Botón "Descuentos aplicables": calcula la lista para el diálogo. */
    public void cargarDescuentos(ActionEvent event) {
        descuentoSeleccionado = null;

        Set<Producto> productosOrden = new LinkedHashSet<>();
        for (OrdenProducto l : lineas) {
            if (l.getIdProducto() != null) {
                productosOrden.add(l.getIdProducto());
            }
        }

        Map<UUID, Descuento> unicos = new LinkedHashMap<>();
        for (DescuentoProducto dp : descuentoService.findVigentes(productosOrden, getRegistro().getFechaOrden())) {
            unicos.putIfAbsent(dp.getIdDescuento().getIdDescuento(), dp.getIdDescuento());
        }
        descuentosAplicables = new ArrayList<>(unicos.values());
    }

    /** Botón "Aplicar" del diálogo. */
    public void aplicarDescuento(ActionEvent event) {
        if (descuentoSeleccionado == null) {
            mensaje(FacesMessage.SEVERITY_WARN, "Seleccione un descuento");
            return;
        }
        DescuentoService.Resultado r = descuentoService.aplicar(
                descuentoSeleccionado, lineas, getRegistro().getFechaOrden());

        if (r.aplicadas > 0) {
            mensaje(FacesMessage.SEVERITY_INFO,
                    "Descuento aplicado a " + r.aplicadas + " producto(s)");
        } else if (r.repetidas > 0) {
            mensaje(FacesMessage.SEVERITY_WARN, "Ese descuento ya estaba aplicado");
        } else {
            mensaje(FacesMessage.SEVERITY_WARN, "El descuento no aplica a ningún producto de la orden");
        }
        descuentoSeleccionado = null;
    }

    // ========================================
    // Internos
    // ========================================

    private void cargarLineas() {
        lineas = new ArrayList<>(ordenProductoDao.findByOrden(getRegistro().getIdOrden()));
        lineasEliminadas.clear();
        productoSeleccionado = null;
        descuentoSeleccionado = null;
        tabActivo = 0;
    }

    private void limpiarLineas() {
        lineas = new ArrayList<>();
        lineasEliminadas.clear();
        productoSeleccionado = null;
        descuentoSeleccionado = null;
        tabActivo = 0;
    }

    private void mensaje(FacesMessage.Severity sev, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(sev, texto, null));
    }

    // ========================================
    // Combos
    // ========================================

    public List<EmpleadoRol> getCamareros() {
        if (camareros == null) {
            camareros = dao.findCamareros();
        }
        return camareros;
    }

    public List<Producto> getProductos() {
        if (productos == null) {
            productos = new ArrayList<>();
            for (Producto p : productoDao.findAll()) {
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

    public List<OrdenProducto> getLineas() {
        return lineas;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(Producto productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }

    public List<Descuento> getDescuentosAplicables() {
        return descuentosAplicables;
    }

    public Descuento getDescuentoSeleccionado() {
        return descuentoSeleccionado;
    }

    public void setDescuentoSeleccionado(Descuento descuentoSeleccionado) {
        this.descuentoSeleccionado = descuentoSeleccionado;
    }

    public int getTabActivo() {
        return tabActivo;
    }

    public void setTabActivo(int tabActivo) {
        this.tabActivo = tabActivo;
    }
}