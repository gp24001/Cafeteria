package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.CaracteristicaDAO;
import com.ppi115.cafefe.control.DescuentoDAO;
import com.ppi115.cafefe.control.DescuentoProductoDAO;
import com.ppi115.cafefe.control.ProductoCaracteristicaDAO;
import com.ppi115.cafefe.control.ProductoDAO;
import com.ppi115.cafefe.control.ProductoTipoProductoDAO;
import com.ppi115.cafefe.control.TipoProductoDAO;
import com.ppi115.cafefe.entity.Caracteristica;
import com.ppi115.cafefe.entity.Descuento;
import com.ppi115.cafefe.entity.DescuentoProducto;
import com.ppi115.cafefe.entity.Producto;
import com.ppi115.cafefe.entity.ProductoCaracteristica;
import com.ppi115.cafefe.entity.ProductoTipoProducto;
import com.ppi115.cafefe.entity.TipoProducto;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.primefaces.event.SelectEvent;

@Named("productoModel")
@ViewScoped
public class ProductoModel extends AbstractModel<Producto> implements Serializable {

    @Inject
    private ProductoDAO dao;

    @Inject
    private ProductoTipoProductoDAO productoTipoProductoDAO;

    @Inject
    private TipoProductoDAO tipoProductoDAO;

    @Inject
    private ProductoCaracteristicaDAO productoCaracteristicaDAO;

    @Inject
    private CaracteristicaDAO caracteristicaDAO;

    @Inject
    private DescuentoProductoDAO descuentoProductoDAO;

    @Inject
    private DescuentoDAO descuentoDAO;

    /* TIPO PRODUCTO */
    private ProductoTipoProducto productoTipoProducto;
    private ProductoTipoProducto productoTipoProductoSeleccionado;
    private TipoProducto tipoProductoSeleccionado;
    private ESTADO_CRUD estadoTipoProducto = ESTADO_CRUD.NINGUNO;

    /* CARACTERISTICA */
    private ProductoCaracteristica productoCaracteristica;
    private ProductoCaracteristica productoCaracteristicaSeleccionada;
    private Caracteristica caracteristicaSeleccionada;
    private ESTADO_CRUD estadoCaracteristica = ESTADO_CRUD.NINGUNO;

    /* DESCUENTO */
    private DescuentoProducto descuentoProducto;
    private DescuentoProducto descuentoProductoSeleccionado;
    private Descuento descuentoSeleccionado;

    private int pestanaActiva = 0;

    public ProductoModel() {
        setNombreBean("Producto");
    }

    @Override
    public ProductoDAO getDao() {
        return dao;
    }

    @Override
    public Producto createRegistrer() {
        Producto r = new Producto();
        r.setIdProducto(UUID.randomUUID());
        r.setActivo(true);
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
    public Object getIdByRegistrer(Producto registro) {
        if (registro == null) {
            return null;
        }
        return registro.getIdProducto();
    }

    /* =========================
       PRODUCTO
       ========================= */

    @Override
    public void nuevo() {
        limpiarTipoProducto();
        limpiarCaracteristicaProducto();
        limpiarDescuentoProducto();
        pestanaActiva = 0;
        super.nuevo();
    }

    @Override
    public void guardar() {
        pestanaActiva = 0;
        super.guardar();

        if (getEstado() == ESTADO_CRUD.NINGUNO) {
            limpiarTipoProducto();
            limpiarCaracteristicaProducto();
            limpiarDescuentoProducto();
        }
    }

    @Override
    public void cancelar() {
        pestanaActiva = 0;
        limpiarTipoProducto();
        limpiarCaracteristicaProducto();
        limpiarDescuentoProducto();
        super.cancelar();
    }

    @Override
    public void seleccionar(SelectEvent<Producto> event) {
        pestanaActiva = 0;
        super.seleccionar(event);

        if (event == null || event.getObject() == null) {
            limpiarTipoProducto();
            limpiarCaracteristicaProducto();
            limpiarDescuentoProducto();
            return;
        }

        cargarTipoProducto(event.getObject());
        cargarCaracteristicaProducto(event.getObject());
        prepararDescuentoProducto(event.getObject());
    }

    /* =========================
       TIPO DEL PRODUCTO
       ========================= */

    private void cargarTipoProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            limpiarTipoProducto();
            return;
        }

        List<ProductoTipoProducto> asignaciones
                = productoTipoProductoDAO.findByIdProducto(
                        producto.getIdProducto());

        if (asignaciones != null && !asignaciones.isEmpty()) {
            productoTipoProducto = asignaciones.get(0);
            productoTipoProductoSeleccionado = productoTipoProducto;
            tipoProductoSeleccionado
                    = productoTipoProducto.getIdTipoProducto();
            estadoTipoProducto = ESTADO_CRUD.EDITAR;
        } else {
            prepararTipoProducto(producto);
        }
    }

    private void prepararTipoProducto(Producto producto) {
        productoTipoProducto = new ProductoTipoProducto();
        productoTipoProducto.setIdProductoTipoProducto(UUID.randomUUID());
        productoTipoProducto.setIdProducto(producto);
        productoTipoProducto.setActivo(true);
        productoTipoProducto.setFechaCreacion(new Date());

        productoTipoProductoSeleccionado = null;
        tipoProductoSeleccionado = null;
        estadoTipoProducto = ESTADO_CRUD.NUEVO;
    }

    public void seleccionarTipoDisponible(TipoProducto tipo) {
        if (tipo == null) {
            return;
        }

        if (!Boolean.TRUE.equals(tipo.getActivo())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "No se puede asignar un tipo de producto inactivo",
                            null));
            return;
        }

        tipoProductoSeleccionado = tipo;

        if (productoTipoProducto != null) {
            productoTipoProducto.setIdTipoProducto(tipo);
        }
    }

    public void seleccionarTipoProducto(
            SelectEvent<ProductoTipoProducto> event) {

        if (event == null || event.getObject() == null) {
            return;
        }

        limpiarCaracteristicaProducto();

        productoTipoProductoSeleccionado = event.getObject();
        productoTipoProducto = event.getObject();

        tipoProductoSeleccionado
                = productoTipoProducto.getIdTipoProducto();

        estadoTipoProducto = ESTADO_CRUD.EDITAR;
        pestanaActiva = 1;
    }

    public void guardarTipoProducto() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (productoTipoProducto == null
                || productoTipoProducto.getIdProducto() == null) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Debe seleccionar un producto",
                            null));
            return;
        }

        try {
            if (estadoTipoProducto == ESTADO_CRUD.NUEVO) {

                Producto producto = dao.findById(
                        productoTipoProducto.getIdProducto().getIdProducto());

                if (producto == null || !producto.getActivo()) {
                    context.addMessage(null,
                            new FacesMessage(
                                    FacesMessage.SEVERITY_WARN,
                                    "No se puede asignar un tipo a un producto inactivo",
                                    null));
                    return;
                }

                if (tipoProductoSeleccionado == null) {
                    context.addMessage(null,
                            new FacesMessage(
                                    FacesMessage.SEVERITY_WARN,
                                    "Debe seleccionar un tipo de producto",
                                    null));
                    return;
                }

                TipoProducto tipo = tipoProductoDAO.findById(
                        tipoProductoSeleccionado.getIdTipoProducto());

                if (tipo == null) {
                    context.addMessage(null,
                            new FacesMessage(
                                    FacesMessage.SEVERITY_WARN,
                                    "El tipo de producto no existe",
                                    null));
                    return;
                }

                if (!Boolean.TRUE.equals(tipo.getActivo())) {
                    context.addMessage(null,
                            new FacesMessage(
                                    FacesMessage.SEVERITY_WARN,
                                    "No se puede asignar un tipo de producto inactivo",
                                    null));
                    return;
                }

                productoTipoProducto.setIdTipoProducto(tipo);
                productoTipoProductoDAO.crear(productoTipoProducto);

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_INFO,
                                "Se asignó el tipo de producto",
                                null));

            } else if (estadoTipoProducto == ESTADO_CRUD.EDITAR) {

                productoTipoProductoDAO.modificar(productoTipoProducto);

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_INFO,
                                "Se modificó la asignación",
                                null));
            }

            super.cancelar();
            limpiarTipoProducto();
            limpiarCaracteristicaProducto();
            pestanaActiva = 1;

        } catch (IllegalArgumentException e) {
            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Error al guardar: " + e.getMessage(),
                            null));
        } catch (Exception e) {
            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Ocurrió un error al guardar la asignación",
                            null));
        }
    }

    public void cancelarTipoProducto() {
        super.cancelar();
        limpiarTipoProducto();
        limpiarCaracteristicaProducto();
        pestanaActiva = 1;
    }

    public void eliminarTipoProducto() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (productoTipoProducto == null
                || productoTipoProducto.getIdProductoTipoProducto() == null
                || estadoTipoProducto != ESTADO_CRUD.EDITAR) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Debe seleccionar una asignación",
                            null));
            return;
        }

        try {
            productoTipoProductoDAO.eliminar(
                    productoTipoProducto.getIdProductoTipoProducto());

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Se eliminó la asignación",
                            null));

            super.cancelar();
            limpiarTipoProducto();
            limpiarCaracteristicaProducto();
            pestanaActiva = 1;

        } catch (IllegalArgumentException e) {
            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Error al eliminar: " + e.getMessage(),
                            null));
        } catch (Exception e) {
            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Ocurrió un error al eliminar la asignación",
                            null));
        }
    }

    private void limpiarTipoProducto() {
        productoTipoProducto = null;
        productoTipoProductoSeleccionado = null;
        tipoProductoSeleccionado = null;
        estadoTipoProducto = ESTADO_CRUD.NINGUNO;
    }

    /* =========================
       CARACTERISTICA DEL PRODUCTO
       ========================= */

    private void cargarCaracteristicaProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            limpiarCaracteristicaProducto();
            return;
        }

        List<ProductoCaracteristica> asignaciones
                = productoCaracteristicaDAO.findByIdProducto(
                        producto.getIdProducto());

        if (asignaciones != null && !asignaciones.isEmpty()) {
            productoCaracteristica = asignaciones.get(0);
            productoCaracteristicaSeleccionada = productoCaracteristica;
            caracteristicaSeleccionada
                    = productoCaracteristica.getIdCaracteristica();

            estadoCaracteristica = ESTADO_CRUD.EDITAR;
        } else {
            prepararCaracteristicaProducto(producto);
        }
    }

    private void prepararCaracteristicaProducto(Producto producto) {
        productoCaracteristica = new ProductoCaracteristica();
        productoCaracteristica.setIdProductoCaracteristica(
                UUID.randomUUID());
        productoCaracteristica.setIdProducto(producto);

        productoCaracteristicaSeleccionada = null;
        caracteristicaSeleccionada = null;
        estadoCaracteristica = ESTADO_CRUD.NUEVO;
    }

    public void seleccionarCaracteristicaDisponible(
            Caracteristica caracteristica) {

        if (caracteristica == null) {
            return;
        }

        if (!Boolean.TRUE.equals(caracteristica.getActivo())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "No se puede asignar una característica inactiva",
                            null));
            return;
        }

        caracteristicaSeleccionada = caracteristica;

        if (productoCaracteristica != null) {
            productoCaracteristica.setIdCaracteristica(caracteristica);
        }
    }

    public void seleccionarProductoCaracteristica(
            SelectEvent<ProductoCaracteristica> event) {

        if (event == null || event.getObject() == null) {
            return;
        }

        limpiarTipoProducto();

        productoCaracteristicaSeleccionada = event.getObject();
        productoCaracteristica = event.getObject();

        caracteristicaSeleccionada
                = productoCaracteristica.getIdCaracteristica();

        estadoCaracteristica = ESTADO_CRUD.EDITAR;
        pestanaActiva = 2;
    }

    public void guardarProductoCaracteristica() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (productoCaracteristica == null
                || productoCaracteristica.getIdProducto() == null) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Debe seleccionar un producto",
                            null));
            return;
        }

        try {
            Producto producto = dao.findById(
                    productoCaracteristica.getIdProducto().getIdProducto());

            if (estadoCaracteristica == ESTADO_CRUD.NUEVO) {

                if (producto == null || !producto.getActivo()) {
                    context.addMessage(null,
                            new FacesMessage(
                                    FacesMessage.SEVERITY_WARN,
                                    "No se puede asignar una característica a un producto inactivo",
                                    null));
                    return;
                }

                if (caracteristicaSeleccionada == null) {
                    context.addMessage(null,
                            new FacesMessage(
                                    FacesMessage.SEVERITY_WARN,
                                    "Debe seleccionar una característica",
                                    null));
                    return;
                }

                Caracteristica caracteristica
                        = caracteristicaDAO.findById(
                                caracteristicaSeleccionada
                                        .getIdCaracteristica());

                if (caracteristica == null) {
                    context.addMessage(null,
                            new FacesMessage(
                                    FacesMessage.SEVERITY_WARN,
                                    "La característica no existe",
                                    null));
                    return;
                }

                if (!Boolean.TRUE.equals(caracteristica.getActivo())) {
                    context.addMessage(null,
                            new FacesMessage(
                                    FacesMessage.SEVERITY_WARN,
                                    "No se puede asignar una característica inactiva",
                                    null));
                    return;
                }

                productoCaracteristica.setIdCaracteristica(
                        caracteristica);

                caracteristicaSeleccionada = caracteristica;
            }

            if (productoCaracteristica.getValor() == null
                    || productoCaracteristica.getValor().isBlank()) {

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_WARN,
                                "El valor es obligatorio",
                                null));
                return;
            }

            if (!validarExpresionRegular()) {
                return;
            }

            if (estadoCaracteristica == ESTADO_CRUD.NUEVO) {

                productoCaracteristicaDAO.crear(
                        productoCaracteristica);

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_INFO,
                                "Se asignó la característica",
                                null));

            } else if (estadoCaracteristica == ESTADO_CRUD.EDITAR) {

                productoCaracteristicaDAO.modificar(
                        productoCaracteristica);

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_INFO,
                                "Se modificó la característica",
                                null));
            }

            super.cancelar();
            limpiarTipoProducto();
            limpiarCaracteristicaProducto();
            pestanaActiva = 2;

        } catch (IllegalArgumentException e) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Error al guardar: " + e.getMessage(),
                            null));

        } catch (Exception e) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Ocurrió un error al guardar la característica",
                            null));
        }
    }

    private boolean validarExpresionRegular() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (caracteristicaSeleccionada == null
                || caracteristicaSeleccionada
                        .getIdTipoCaracteristica() == null) {
            return true;
        }

        String expresion = caracteristicaSeleccionada
                .getIdTipoCaracteristica()
                .getExpresionRegular();

        if (expresion == null || expresion.isBlank()) {
            return true;
        }

        try {
            if (!Pattern.matches(
                    expresion,
                    productoCaracteristica.getValor())) {

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_WARN,
                                "El valor no cumple con la expresión regular de la característica",
                                null));

                return false;
            }

        } catch (PatternSyntaxException e) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "La expresión regular configurada no es válida",
                            null));

            return false;
        }

        return true;
    }

    public void cancelarProductoCaracteristica() {
        super.cancelar();
        limpiarTipoProducto();
        limpiarCaracteristicaProducto();
        pestanaActiva = 2;
    }

    public void eliminarProductoCaracteristica() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (productoCaracteristica == null
                || productoCaracteristica
                        .getIdProductoCaracteristica() == null
                || estadoCaracteristica != ESTADO_CRUD.EDITAR) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Debe seleccionar una asignación",
                            null));
            return;
        }

        try {
            productoCaracteristicaDAO.eliminar(
                    productoCaracteristica
                            .getIdProductoCaracteristica());

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Se eliminó la característica",
                            null));

            super.cancelar();
            limpiarTipoProducto();
            limpiarCaracteristicaProducto();
            pestanaActiva = 2;

        } catch (IllegalArgumentException e) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Error al eliminar: " + e.getMessage(),
                            null));

        } catch (Exception e) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Ocurrió un error al eliminar la característica",
                            null));
        }
    }

    private void limpiarCaracteristicaProducto() {
        productoCaracteristica = null;
        productoCaracteristicaSeleccionada = null;
        caracteristicaSeleccionada = null;
        estadoCaracteristica = ESTADO_CRUD.NINGUNO;
    }

    /* =========================
       DESCUENTO DEL PRODUCTO
       ========================= */

    private void prepararDescuentoProducto(Producto producto) {
        descuentoProducto = new DescuentoProducto();
        descuentoProducto.setIdDescuentoProducto(UUID.randomUUID());
        descuentoProducto.setIdProducto(producto);
        descuentoProducto.setFechaInicio(new Date());

        descuentoProductoSeleccionado = null;
        descuentoSeleccionado = null;
    }

    public void seleccionarDescuentoDisponible(Descuento descuento) {
        if (descuento == null) {
            return;
        }

        if (descuento.getIdTipoDescuento() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "El descuento no tiene un tipo de descuento",
                            null));
            return;
        }

        if (!Boolean.TRUE.equals(
                descuento.getIdTipoDescuento().getActivo())) {

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "No se puede asignar un descuento de un tipo inactivo",
                            null));
            return;
        }

        descuentoSeleccionado = descuento;

        if (descuentoProducto != null) {
            descuentoProducto.setIdDescuento(descuento);
        }
    }

    public void seleccionarDescuentoProducto(
            SelectEvent<DescuentoProducto> event) {

        if (event == null || event.getObject() == null) {
            return;
        }

        descuentoProductoSeleccionado = event.getObject();
        descuentoProducto = event.getObject();
        descuentoSeleccionado = descuentoProducto.getIdDescuento();

        setRegistro(descuentoProducto.getIdProducto());
        setSeleccionado(descuentoProducto.getIdProducto());
        setEstado(ESTADO_CRUD.EDITAR);

        pestanaActiva = 3;
    }

    public void guardarDescuentoProducto() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (descuentoProducto == null
                || descuentoProducto.getIdProducto() == null) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Debe seleccionar un producto",
                            null));
            return;
        }

        if (descuentoSeleccionado == null) {
            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Debe seleccionar un descuento",
                            null));
            return;
        }

        if (descuentoProducto.getValor() == null
                || descuentoProducto.getValor() <= 0) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "El valor del descuento debe ser mayor a 0",
                            null));
            return;
        }

        if (descuentoProducto.getFechaInicio() == null) {
            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "La fecha de inicio es obligatoria",
                            null));
            return;
        }

        if (descuentoProducto.getFechaFin() != null
                && descuentoProducto.getFechaFin()
                        .before(descuentoProducto.getFechaInicio())) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "La fecha fin no puede ser anterior a la fecha de inicio",
                            null));
            return;
        }

        try {
            Producto producto = dao.findById(
                    descuentoProducto.getIdProducto().getIdProducto());

            if (producto == null || !producto.getActivo()) {
                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_WARN,
                                "No se puede asignar un descuento a un producto inactivo",
                                null));
                return;
            }

            Descuento descuento = descuentoDAO.findById(
                    descuentoSeleccionado.getIdDescuento());

            if (descuento == null) {
                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_WARN,
                                "El descuento no existe",
                                null));
                return;
            }

            /*
             * Solo verificamos duplicado cuando estamos creando.
             * Al editar, la relación ya existe y no debe detectarse
             * a sí misma como duplicada.
             */
            if (descuentoProductoSeleccionado == null
                    && descuentoProductoDAO.existeProductoDescuento(
                            producto.getIdProducto(),
                            descuento.getIdDescuento())) {

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_WARN,
                                "Este descuento ya está asignado al producto",
                                null));
                return;
            }

            if (descuento.getIdTipoDescuento() == null
                    || !Boolean.TRUE.equals(
                            descuento.getIdTipoDescuento().getActivo())) {

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_WARN,
                                "El tipo de descuento está inactivo",
                                null));
                return;
            }

            Integer maximo
                    = descuento.getIdTipoDescuento().getDescuentoMaximo();

            if (maximo != null
                    && descuentoProducto.getValor() > maximo) {

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_WARN,
                                "El descuento no puede superar el máximo de "
                                + maximo + "%",
                                null));
                return;
            }

            if (descuento.getFechaInicio() != null
                    && descuentoProducto.getFechaInicio()
                            .before(descuento.getFechaInicio())) {

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_WARN,
                                "La fecha de inicio no puede ser anterior a la vigencia del descuento",
                                null));
                return;
            }

            if (descuento.getFechaFin() != null) {

                if (descuentoProducto.getFechaFin() == null) {
                    context.addMessage(null,
                            new FacesMessage(
                                    FacesMessage.SEVERITY_WARN,
                                    "Debe indicar una fecha fin",
                                    null));
                    return;
                }

                if (descuentoProducto.getFechaFin()
                        .after(descuento.getFechaFin())) {

                    context.addMessage(null,
                            new FacesMessage(
                                    FacesMessage.SEVERITY_WARN,
                                    "La fecha fin supera la vigencia del descuento",
                                    null));
                    return;
                }
            }

            descuentoProducto.setIdProducto(producto);
            descuentoProducto.setIdDescuento(descuento);

            if (descuentoProductoSeleccionado == null) {

                descuentoProductoDAO.crear(descuentoProducto);

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_INFO,
                                "Se asignó el descuento al producto",
                                null));

            } else {

                descuentoProductoDAO.modificar(descuentoProducto);

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_INFO,
                                "Se modificó el descuento del producto",
                                null));
            }

            super.cancelar();
            limpiarTipoProducto();
            limpiarCaracteristicaProducto();
            limpiarDescuentoProducto();
            pestanaActiva = 3;

        } catch (IllegalArgumentException e) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Error al guardar: " + e.getMessage(),
                            null));

        } catch (Exception e) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Ocurrió un error al guardar el descuento",
                            null));
        }
    }

    public void cancelarDescuentoProducto() {
        super.cancelar();
        limpiarTipoProducto();
        limpiarCaracteristicaProducto();
        limpiarDescuentoProducto();
        pestanaActiva = 3;
    }
    
    public void eliminarDescuentoProducto() {
    FacesContext context = FacesContext.getCurrentInstance();

    if (descuentoProductoSeleccionado == null
            || descuentoProductoSeleccionado.getIdDescuentoProducto() == null) {

        context.addMessage(null,
                new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Debe seleccionar un descuento del producto",
                        null));
        return;
    }

    try {
        descuentoProductoDAO.eliminar(
                descuentoProductoSeleccionado.getIdDescuentoProducto());

        context.addMessage(null,
                new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Se eliminó el descuento del producto",
                        null));

        super.cancelar();
        limpiarTipoProducto();
        limpiarCaracteristicaProducto();
        limpiarDescuentoProducto();
        pestanaActiva = 3;

    } catch (IllegalArgumentException e) {

        context.addMessage(null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Error al eliminar: " + e.getMessage(),
                        null));

    } catch (Exception e) {

        context.addMessage(null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Ocurrió un error al eliminar el descuento",
                        null));
    }
}

    private void limpiarDescuentoProducto() {
        descuentoProducto = null;
        descuentoProductoSeleccionado = null;
        descuentoSeleccionado = null;
    }

    /* =========================
       LISTAS
       ========================= */

    public List<ProductoTipoProducto> getProductosTipoProducto() {
        return productoTipoProductoDAO.findAll();
    }

    public List<TipoProducto> getTiposProductoDisponibles() {
        return tipoProductoDAO.findAll();
    }

    public List<ProductoCaracteristica> getProductosCaracteristica() {
        return productoCaracteristicaDAO.findAll();
    }

    public List<Caracteristica> getCaracteristicasDisponibles() {
        return caracteristicaDAO.findAll();
    }

    public List<DescuentoProducto> getDescuentosProducto() {
        return descuentoProductoDAO.findAll();
    }

    public List<Descuento> getDescuentosDisponibles() {
        return descuentoDAO.findAll();
    }

    /* =========================
       GETTERS Y SETTERS
       ========================= */

    public ProductoTipoProducto getProductoTipoProducto() {
        return productoTipoProducto;
    }

    public void setProductoTipoProducto(
            ProductoTipoProducto productoTipoProducto) {
        this.productoTipoProducto = productoTipoProducto;
    }

    public ProductoTipoProducto getProductoTipoProductoSeleccionado() {
        return productoTipoProductoSeleccionado;
    }

    public void setProductoTipoProductoSeleccionado(
            ProductoTipoProducto productoTipoProductoSeleccionado) {
        this.productoTipoProductoSeleccionado
                = productoTipoProductoSeleccionado;
    }

    public TipoProducto getTipoProductoSeleccionado() {
        return tipoProductoSeleccionado;
    }

    public void setTipoProductoSeleccionado(
            TipoProducto tipoProductoSeleccionado) {
        this.tipoProductoSeleccionado = tipoProductoSeleccionado;
    }

    public ESTADO_CRUD getEstadoTipoProducto() {
        return estadoTipoProducto;
    }

    public void setEstadoTipoProducto(
            ESTADO_CRUD estadoTipoProducto) {
        this.estadoTipoProducto = estadoTipoProducto;
    }

    public ProductoCaracteristica getProductoCaracteristica() {
        return productoCaracteristica;
    }

    public void setProductoCaracteristica(
            ProductoCaracteristica productoCaracteristica) {
        this.productoCaracteristica = productoCaracteristica;
    }

    public ProductoCaracteristica getProductoCaracteristicaSeleccionada() {
        return productoCaracteristicaSeleccionada;
    }

    public void setProductoCaracteristicaSeleccionada(
            ProductoCaracteristica productoCaracteristicaSeleccionada) {
        this.productoCaracteristicaSeleccionada
                = productoCaracteristicaSeleccionada;
    }

    public Caracteristica getCaracteristicaSeleccionada() {
        return caracteristicaSeleccionada;
    }

    public void setCaracteristicaSeleccionada(
            Caracteristica caracteristicaSeleccionada) {
        this.caracteristicaSeleccionada = caracteristicaSeleccionada;
    }

    public ESTADO_CRUD getEstadoCaracteristica() {
        return estadoCaracteristica;
    }

    public void setEstadoCaracteristica(
            ESTADO_CRUD estadoCaracteristica) {
        this.estadoCaracteristica = estadoCaracteristica;
    }

    public DescuentoProducto getDescuentoProducto() {
        return descuentoProducto;
    }

    public void setDescuentoProducto(
            DescuentoProducto descuentoProducto) {
        this.descuentoProducto = descuentoProducto;
    }

    public DescuentoProducto getDescuentoProductoSeleccionado() {
        return descuentoProductoSeleccionado;
    }

    public void setDescuentoProductoSeleccionado(
            DescuentoProducto descuentoProductoSeleccionado) {
        this.descuentoProductoSeleccionado
                = descuentoProductoSeleccionado;
    }

    public Descuento getDescuentoSeleccionado() {
        return descuentoSeleccionado;
    }

    public void setDescuentoSeleccionado(
            Descuento descuentoSeleccionado) {
        this.descuentoSeleccionado = descuentoSeleccionado;
    }

    public int getPestanaActiva() {
        return pestanaActiva;
    }

    public void setPestanaActiva(int pestanaActiva) {
        this.pestanaActiva = pestanaActiva;
    }
}