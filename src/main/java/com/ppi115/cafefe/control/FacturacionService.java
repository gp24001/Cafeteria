package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.*;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.*;
import static com.ppi115.cafefe.control.ValidacionComercial.*;

/** Operaciones de facturación y cobro. */
@Stateless
@LocalBean
public class FacturacionService {
    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;
    @Inject private FacturaDAO facturaDAO;
    @Inject private FacturaOrdenProductoDAO lineaDAO;
    @Inject private PagoDAO pagoDAO;
    @Inject private PagoDetalleDAO detalleDAO;

    /** Emite una factura copiando los importes ya guardados en las líneas de orden. */
    public Factura emitirFactura(UUID idCaja, UUID idEmpleadoRol, String cliente,
            List<UUID> idsLineas, String observaciones) {
        exigir(idsLineas != null && !idsLineas.isEmpty(), "Seleccione al menos una línea de orden");
        exigir(idsLineas.stream().noneMatch(Objects::isNull), "La línea de orden es obligatoria");
        exigir(new HashSet<>(idsLineas).size() == idsLineas.size(), "No repita líneas de orden");
        OrdenProducto primera = referencia(em, OrdenProducto.class, idsLineas.getFirst(), "La línea de orden");
        exigir(primera.getIdOrden() != null, "La línea no pertenece a una orden");
        UUID idOrden = primera.getIdOrden().getIdOrden();
        em.find(Orden.class, idOrden, LockModeType.PESSIMISTIC_WRITE);
        List<UUID> completas = em.createQuery("SELECT op.idOrdenProducto FROM OrdenProducto op "
                + "WHERE op.idOrden.idOrden = :id", UUID.class).setParameter("id", idOrden).getResultList();
        exigir(new HashSet<>(completas).equals(new HashSet<>(idsLineas)),
                "Debe facturar la orden completa, sin omitir productos ni mezclar órdenes");
        Factura factura = new Factura(UUID.randomUUID());
        factura.setIdCaja(new Caja(idCaja));
        factura.setIdEmpleadoRol(new EmpleadoRol(idEmpleadoRol));
        factura.setCliente(cliente);
        factura.setFechaFacturacion(new Date());
        factura.setEstado("EMITIDA");
        factura.setObservaciones(observaciones);
        // Bloqueo en orden estable: dos cajas no pueden facturar simultáneamente la misma línea.
        List<OrdenProducto> lineas = new ArrayList<>();
        for (UUID id : idsLineas.stream().sorted().toList()) {
            OrdenProducto linea = em.find(OrdenProducto.class, id, LockModeType.PESSIMISTIC_WRITE);
            exigir(linea != null, "La línea de orden no existe");
            dinero(linea.getPrecio(), true);
            exigir(linea.getIdOrden() != null && linea.getIdProducto() != null, "La línea de orden está incompleta");
            long anteriores = em.createQuery("SELECT COUNT(f) FROM FacturaOrdenProducto f "
                    + "WHERE f.idOrdenProducto.idOrdenProducto = :id AND f.idFactura.estado <> 'ANULADA'", Long.class)
                    .setParameter("id", id).getSingleResult();
            exigir(anteriores == 0, "La línea de orden ya está facturada");
            lineas.add(linea);
        }
        facturaDAO.crear(factura);
        for (OrdenProducto linea : lineas) {
            FacturaOrdenProducto detalle = new FacturaOrdenProducto(UUID.randomUUID());
            detalle.setIdFactura(factura);
            detalle.setIdOrdenProducto(linea);
            detalle.setPrecio(linea.getPrecio());
            detalle.setObservaciones(linea.getObservaciones());
            lineaDAO.crear(detalle);
        }
        if (lineas.stream().map(OrdenProducto::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add).signum() == 0)
            factura.setEstado("PAGADA");
        em.flush();
        return factura;
    }

    /** Acepta pagos parciales y varios detalles; nunca permite cobrar más que el saldo. */
    public Pago registrarPago(UUID idFactura, List<PagoDetalle> detalles, String observaciones) {
        Factura factura = bloquearFactura(idFactura);
        exigir(!"ANULADA".equals(factura.getEstado()), "La factura está anulada");
        exigir(detalles != null && !detalles.isEmpty(), "Registre al menos un detalle de pago");
        BigDecimal importe = BigDecimal.ZERO;
        for (PagoDetalle detalle : detalles) {
            exigir(detalle != null, "El detalle de pago es obligatorio");
            dinero(detalle.getMonto(), false);
            texto(detalle.getTipoPago(), "El tipo de pago");
            exigir(detalle.getIdPago() == null, "El detalle debe ser nuevo y no pertenecer a otro pago");
            exigir(detalle.getIdPagoDetalle() == null || em.find(PagoDetalle.class, detalle.getIdPagoDetalle()) == null,
                    "El detalle de pago ya existe");
            importe = importe.add(detalle.getMonto());
        }
        exigir(importe.compareTo(saldo(idFactura)) <= 0, "El pago supera el saldo de la factura");
        Pago pago = new Pago(UUID.randomUUID());
        pago.setIdFactura(factura);
        pago.setEstado("CONFIRMADO");
        pago.setObservaciones(observaciones);
        pagoDAO.crear(pago);
        // Se copian los detalles: no se cambia el formulario del llamador si la transacción falla.
        for (PagoDetalle entrada : detalles) {
            PagoDetalle detalle = new PagoDetalle(UUID.randomUUID());
            detalle.setIdPago(pago);
            detalle.setMonto(entrada.getMonto());
            detalle.setObservaciones(entrada.getObservaciones());
            detalle.setReferenciaExterna(entrada.getReferenciaExterna());
            detalle.setTipoPago(entrada.getTipoPago());
            detalleDAO.crear(detalle);
        }
        em.flush();
        factura.setEstado(saldo(idFactura).signum() == 0 ? "PAGADA" : "PARCIAL");
        return pago;
    }

    /** Anula únicamente facturas sin cobros; no simula devoluciones que el esquema no representa. */
    public void anularFactura(UUID idFactura, String motivo) {
        texto(motivo, "El motivo de anulación");
        Factura factura = bloquearFactura(idFactura);
        exigir(!"ANULADA".equals(factura.getEstado()), "La factura ya está anulada");
        exigir(pagado(idFactura).signum() == 0, "No se puede anular una factura con pagos registrados");
        factura.setEstado("ANULADA");
        factura.setObservaciones((factura.getObservaciones() == null ? "" : factura.getObservaciones() + "\n")
                + "Anulación: " + motivo.trim());
    }

    public BigDecimal total(UUID idFactura) {
        referencia(em, Factura.class, idFactura, "La factura");
        BigDecimal total = em.createQuery("SELECT SUM(f.precio) FROM FacturaOrdenProducto f "
                + "WHERE f.idFactura.idFactura = :id", BigDecimal.class)
                .setParameter("id", idFactura).getSingleResult();
        return total == null ? BigDecimal.ZERO : total;
    }

    public BigDecimal pagado(UUID idFactura) {
        referencia(em, Factura.class, idFactura, "La factura");
        BigDecimal pagado = em.createQuery("SELECT SUM(d.monto) FROM PagoDetalle d "
                + "WHERE d.idPago.idFactura.idFactura = :id AND d.idPago.estado = 'CONFIRMADO'", BigDecimal.class)
                .setParameter("id", idFactura).getSingleResult();
        return pagado == null ? BigDecimal.ZERO : pagado;
    }

    public BigDecimal saldo(UUID idFactura) {
        return total(idFactura).subtract(pagado(idFactura));
    }

    private Factura bloquearFactura(UUID id) {
        exigir(id != null, "La factura es obligatoria");
        em.flush();
        Factura factura = em.find(Factura.class, id, LockModeType.PESSIMISTIC_WRITE);
        exigir(factura != null, "La factura no existe");
        em.refresh(factura, LockModeType.PESSIMISTIC_WRITE);
        return factura;
    }

    public List<OrdenProducto> findLineasPendientes() {
        return em.createQuery("SELECT op FROM OrdenProducto op JOIN FETCH op.idProducto "
                + "JOIN FETCH op.idOrden o WHERE NOT EXISTS (SELECT f.idFacturaOrdenProducto "
                + "FROM FacturaOrdenProducto f WHERE f.idOrdenProducto = op AND f.idFactura.estado <> 'ANULADA') "
                + "ORDER BY o.fechaOrden, op.idOrdenProducto", OrdenProducto.class).getResultList();
    }

    public Factura emitirOrden(UUID idCaja, UUID idEmpleadoRol, String cliente, UUID idOrden, String observaciones) {
        referencia(em, Orden.class, idOrden, "La orden");
        List<UUID> ids = em.createQuery("SELECT op.idOrdenProducto FROM OrdenProducto op "
                + "WHERE op.idOrden.idOrden = :id", UUID.class).setParameter("id", idOrden).getResultList();
        return emitirFactura(idCaja, idEmpleadoRol, cliente, ids, observaciones);
    }

    public List<Orden> findOrdenesPendientes() {
        return em.createQuery("SELECT o FROM Orden o JOIN FETCH o.idEmpleadoRol er "
                + "JOIN FETCH er.idEmpleado WHERE EXISTS (SELECT op.idOrdenProducto FROM OrdenProducto op "
                + "WHERE op.idOrden = o) AND NOT EXISTS (SELECT f.idFacturaOrdenProducto FROM FacturaOrdenProducto f "
                + "WHERE f.idOrdenProducto.idOrden = o AND f.idFactura.estado <> 'ANULADA') "
                + "ORDER BY o.fechaOrden DESC", Orden.class).getResultList();
    }

    public List<OrdenProducto> findDetalleOrden(UUID idOrden) {
        referencia(em, Orden.class, idOrden, "La orden");
        return em.createQuery("SELECT op FROM OrdenProducto op JOIN FETCH op.idProducto "
                + "WHERE op.idOrden.idOrden = :id ORDER BY op.idOrdenProducto", OrdenProducto.class)
                .setParameter("id", idOrden).getResultList();
    }

    public List<EmpleadoRol> findFacturadores() {
        return em.createQuery("SELECT er FROM EmpleadoRol er JOIN FETCH er.idEmpleado e "
                + "JOIN FETCH er.idRol r WHERE er.activo = true AND e.activo = true AND r.activo = true "
                + "ORDER BY e.nombre, e.apellido", EmpleadoRol.class).getResultList();
    }

    public List<Factura> findFacturas() {
        return em.createQuery("SELECT f FROM Factura f LEFT JOIN FETCH f.idCaja "
                + "ORDER BY f.fechaFacturacion DESC, f.idFactura", Factura.class).getResultList();
    }

    public List<PagoDetalle> findDetallesPago(UUID idFactura) {
        exigir(idFactura != null, "La factura es obligatoria");
        return em.createQuery("SELECT d FROM PagoDetalle d JOIN FETCH d.idPago p WHERE p.idFactura.idFactura = :id", PagoDetalle.class)
                .setParameter("id", idFactura).getResultList();
    }
}
