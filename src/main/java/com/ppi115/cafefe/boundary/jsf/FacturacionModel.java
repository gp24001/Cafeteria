package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.*;
import com.ppi115.cafefe.entity.*;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/** Formularios de emisión y cobro*/
@Named("facturacionModel")
@ViewScoped
public class FacturacionModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Inject private FacturacionService servicio;
    @Inject private CajaDAO cajaDAO;
    @Inject private FacturaOrdenProductoDAO lineaDAO;
    private List<Caja> cajas = List.of();
    private List<OrdenProducto> pendientes = List.of();
    private List<OrdenProducto> seleccionadas = new ArrayList<>();
    private List<Orden> ordenesPendientes = List.of();
    private Orden ordenSeleccionada;
    private List<OrdenProducto> detalleOrden = List.of();
    private List<Factura> facturas = List.of();
    private List<FacturaOrdenProducto> lineasFactura = List.of();
    private List<PagoDetalle> historial = List.of();
    private List<PagoDetalle> detalles = new ArrayList<>();
    private UUID idCaja;
    private UUID idEmpleadoRol;
    private List<EmpleadoRol> facturadores = List.of();
    private String cliente;
    private String observaciones;
    private String motivo;
    private int tabActivo;
    private Factura factura;
    private PagoDetalle detalle = new PagoDetalle();
    private BigDecimal total = BigDecimal.ZERO, pagado = BigDecimal.ZERO, saldo = BigDecimal.ZERO;

    @PostConstruct public void cargar() {
        try {
            cajas = cajaDAO.findActivas();
            facturadores = servicio.findFacturadores();
            pendientes = servicio.findLineasPendientes();
            ordenesPendientes = servicio.findOrdenesPendientes();
            facturas = servicio.findFacturas();
        } catch (RuntimeException ex) { error(ex); }
    }

    public void emitir() {
        try {
            if (ordenSeleccionada == null) throw new IllegalArgumentException("Seleccione una orden completa");
            Factura nueva = servicio.emitirOrden(idCaja, idEmpleadoRol, cliente,
                    ordenSeleccionada.getIdOrden(), observaciones);
            seleccionadas = new ArrayList<>(); cliente = null; observaciones = null;
            ordenSeleccionada = null; detalleOrden = List.of();
            cargar(); seleccionar(nueva);
            tabActivo = 1;
            mensaje(FacesMessage.SEVERITY_INFO, "Factura emitida");
        } catch (RuntimeException ex) { error(ex); }
    }

    public void seleccionar(Factura seleccionada) {
        try {
            factura = seleccionada;
            detalles = new ArrayList<>(); detalle = new PagoDetalle(); motivo = null;
            actualizarDetalle();
        } catch (RuntimeException ex) { error(ex); }
    }

    private void actualizarDetalle() {
        if (factura == null) return;
        UUID id = factura.getIdFactura();
        total = servicio.total(id); pagado = servicio.pagado(id); saldo = servicio.saldo(id);
        lineasFactura = lineaDAO.findByFactura(id);
        historial = servicio.findDetallesPago(id);
        factura = facturas.stream().filter(f -> id.equals(f.getIdFactura())).findFirst().orElse(factura);
    }

    public void agregarDetalle() {
        try {
            if (detalle.getMonto() == null || detalle.getMonto().signum() <= 0
                    || detalle.getMonto().stripTrailingZeros().scale() > 2)
                throw new IllegalArgumentException("Escriba un importe positivo con hasta dos decimales");
            if (detalle.getTipoPago() == null || detalle.getTipoPago().isBlank())
                throw new IllegalArgumentException("Seleccione un medio de pago");
            detalle.setIdPagoDetalle(UUID.randomUUID());
            detalles.add(detalle); detalle = new PagoDetalle();
        } catch (RuntimeException ex) { error(ex); }
    }

    public void quitarDetalle(PagoDetalle quitar) { detalles.remove(quitar); }

    public void cobrar() {
        try {
            if (factura == null) throw new IllegalArgumentException("Seleccione una factura");
            servicio.registrarPago(factura.getIdFactura(), detalles, null);
            detalles = new ArrayList<>(); detalle = new PagoDetalle();
            cargar(); actualizarDetalle();
            mensaje(FacesMessage.SEVERITY_INFO, "Pago registrado");
        } catch (RuntimeException ex) { error(ex); }
    }

    public void anular() {
        try {
            if (factura == null) throw new IllegalArgumentException("Seleccione una factura");
            servicio.anularFactura(factura.getIdFactura(), motivo);
            cargar(); actualizarDetalle(); motivo = null;
            mensaje(FacesMessage.SEVERITY_INFO, "Factura anulada; se conserva su historial");
        } catch (RuntimeException ex) { error(ex); }
    }

    private void error(RuntimeException ex) {
        Throwable causa = ex;
        while (!(causa instanceof IllegalArgumentException) && causa.getCause() != null) causa = causa.getCause();
        if (causa instanceof IllegalArgumentException)
            mensaje(FacesMessage.SEVERITY_WARN, causa.getMessage());
        else {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,
                    "Error en facturación", ex);
            mensaje(FacesMessage.SEVERITY_ERROR, "No se pudo completar la operación");
        }
    }

    private void mensaje(FacesMessage.Severity gravedad, String texto) {
        FacesContext contexto = getFacesContext();
        if (contexto != null) contexto.addMessage(null, new FacesMessage(gravedad, texto, null));
    }

    protected FacesContext getFacesContext() { return FacesContext.getCurrentInstance(); }

    public List<Caja> getCajas() { return cajas; }
    public List<OrdenProducto> getPendientes() { return pendientes; }
    public List<OrdenProducto> getSeleccionadas() { return seleccionadas; }
    public void setSeleccionadas(List<OrdenProducto> seleccionadas) { this.seleccionadas = seleccionadas; }
    public List<Factura> getFacturas() { return facturas; }
    public List<FacturaOrdenProducto> getLineasFactura() { return lineasFactura; }
    public List<PagoDetalle> getHistorial() { return historial; }
    public List<PagoDetalle> getDetalles() { return detalles; }
    public PagoDetalle getDetalle() { return detalle; }
    public Factura getFactura() { return factura; }
    public UUID getIdCaja() { return idCaja; }
    public void setIdCaja(UUID idCaja) { this.idCaja = idCaja; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getPagado() { return pagado; }
    public BigDecimal getSaldo() { return saldo; }
    public BigDecimal getTotalDetalles() {
        return detalles.stream().map(PagoDetalle::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public int getTabActivo() { return tabActivo; }
    public void setTabActivo(int tabActivo) { this.tabActivo = tabActivo == 1 ? 1 : 0; }
    public String abrirCobros() { return "/Factura?faces-redirect=true&tab=1"; }
    public List<Orden> getOrdenesPendientes() { return ordenesPendientes; }
    public Orden getOrdenSeleccionada() { return ordenSeleccionada; }
    public List<OrdenProducto> getDetalleOrden() { return detalleOrden; }
    public void elegirOrden(Orden orden) {
        try {
            ordenSeleccionada = orden;
            detalleOrden = servicio.findDetalleOrden(orden.getIdOrden());
        } catch (RuntimeException ex) { error(ex); }
    }
    public BigDecimal getTotalOrden() {
        return detalleOrden.stream().map(OrdenProducto::getPrecio).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public List<EmpleadoRol> getFacturadores() { return facturadores; }
    public UUID getIdEmpleadoRol() { return idEmpleadoRol; }
    public void setIdEmpleadoRol(UUID idEmpleadoRol) { this.idEmpleadoRol = idEmpleadoRol; }
}
