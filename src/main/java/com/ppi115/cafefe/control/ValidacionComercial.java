package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import static com.ppi115.cafefe.control.DefaultDAO.DatosInvalidosException;

/** Reglas compartidas del módulo comercial. */
final class ValidacionComercial {
    private ValidacionComercial() { }

    static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new DatosInvalidosException(mensaje);
    }

    static void texto(String valor, String nombre) {
        exigir(valor != null && !valor.isBlank(), nombre + " es obligatorio");
    }

    static void dinero(BigDecimal valor, boolean admiteCero) {
        exigir(valor != null, "El importe es obligatorio");
        exigir(admiteCero ? valor.signum() >= 0 : valor.signum() > 0,
                admiteCero ? "El importe no puede ser negativo" : "El importe debe ser positivo");
        exigir(valor.stripTrailingZeros().scale() <= 2, "El importe admite como máximo dos decimales");
        exigir(valor.compareTo(new BigDecimal("999999.99")) <= 0, "El importe excede la capacidad del campo");
    }

    static <T> T referencia(EntityManager em, Class<T> tipo, Object id, String nombre) {
        exigir(id != null, nombre + " es obligatorio");
        T registro = em.find(tipo, id);
        exigir(registro != null, nombre + " no existe");
        return registro;
    }

    static void empleadoActivo(EmpleadoRol er) {
        exigir(Boolean.TRUE.equals(er.getActivo()) && er.getIdEmpleado() != null
                && Boolean.TRUE.equals(er.getIdEmpleado().getActivo()) && er.getIdRol() != null
                && Boolean.TRUE.equals(er.getIdRol().getActivo()),
                "La asignación, el empleado y el rol deben estar activos");
    }

    static void validar(EntityManager em, Caja r) {
        texto(r.getNombre(), "El nombre de la caja");
        exigir(r.getActivo() != null, "El estado de la caja es obligatorio");
    }

    static void validar(EntityManager em, Factura r) {
        exigir(r.getFechaFacturacion() != null, "La fecha de facturación es obligatoria");
        texto(r.getCliente(), "El cliente");
        texto(r.getEstado(), "El estado de la factura");
        Caja caja = referencia(em, Caja.class, r.getIdCaja() == null ? null : r.getIdCaja().getIdCaja(), "La caja");
        exigir(Boolean.TRUE.equals(caja.getActivo()), "La caja debe estar activa");
        EmpleadoRol er = referencia(em, EmpleadoRol.class,
                r.getIdEmpleadoRol() == null ? null : r.getIdEmpleadoRol().getIdEmpleadoRol(), "El empleado de facturación");
        empleadoActivo(er);
        exigir(r.getPagoList() == null || r.getPagoList().isEmpty(), "Registre los pagos mediante el servicio de facturación");
        r.setIdCaja(caja);
        r.setIdEmpleadoRol(er);
    }

    static void validar(EntityManager em, Pago r) {
        texto(r.getEstado(), "El estado del pago");
        Factura factura = referencia(em, Factura.class,
                r.getIdFactura() == null ? null : r.getIdFactura().getIdFactura(), "La factura");
        exigir(!"ANULADA".equals(factura.getEstado()), "No se puede pagar una factura anulada");
        exigir(r.getPagoDetalleList() == null || r.getPagoDetalleList().isEmpty(),
                "Registre los detalles mediante el servicio de facturación");
        r.setIdFactura(factura);
    }

    static void validar(EntityManager em, PagoDetalle r) {
        dinero(r.getMonto(), false);
        texto(r.getTipoPago(), "El tipo de pago");
        Pago pago = referencia(em, Pago.class,
                r.getIdPago() == null ? null : r.getIdPago().getIdPago(), "El pago");
        exigir(!"ANULADO".equals(pago.getEstado()), "No se pueden registrar detalles de un pago anulado");
        exigir(!"ANULADA".equals(pago.getIdFactura().getEstado()), "No se puede pagar una factura anulada");
        r.setIdPago(pago);
    }

    static void validar(EntityManager em, FacturaOrdenProducto r) {
        dinero(r.getPrecio(), true);
        Factura factura = referencia(em, Factura.class,
                r.getIdFactura() == null ? null : r.getIdFactura().getIdFactura(), "La factura");
        exigir(!"ANULADA".equals(factura.getEstado()), "La factura está anulada");
        OrdenProducto linea = referencia(em, OrdenProducto.class,
                r.getIdOrdenProducto() == null ? null : r.getIdOrdenProducto().getIdOrdenProducto(), "La línea de orden");
        long repetidas = em.createQuery("SELECT COUNT(f) FROM FacturaOrdenProducto f "
                + "WHERE f.idOrdenProducto.idOrdenProducto = :linea AND f.idFactura.estado <> 'ANULADA' "
                + "AND f.idFacturaOrdenProducto <> :id", Long.class)
                .setParameter("linea", linea.getIdOrdenProducto()).setParameter("id", r.getIdFacturaOrdenProducto())
                .getSingleResult();
        exigir(repetidas == 0, "La línea de orden ya está facturada");
        r.setIdFactura(factura);
        r.setIdOrdenProducto(linea);
    }
}
