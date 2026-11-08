package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;

abstract class ComercialDAOIntegracionBase<T> extends DAOIntegracionBase<T> {
    protected Caja caja;
    protected Factura factura;
    protected Pago pago;
    protected OrdenProducto linea;

    protected ComercialDAOIntegracionBase(Class<T> tipo, Supplier<? extends DefaultDAO<T>> fabrica) {
        super(tipo, fabrica);
    }

    @BeforeEach
    void prepararComercial() {
        caja = new Caja(UUID.randomUUID());
        caja.setNombre("Integración caja");
        caja.setActivo(true);
        dao(new CajaDAO()).crear(caja);
        factura = nuevaFactura();
        dao(new FacturaDAO()).crear(factura);
        pago = new Pago(UUID.randomUUID());
        pago.setIdFactura(factura);
        pago.setEstado("CONFIRMADO");
        dao(new PagoDAO()).crear(pago);
        linea = super.registroValido(OrdenProducto.class);
        linea.setPrecio(new BigDecimal("2.50"));
        dao(new OrdenProductoDAO()).crear(linea);
        em.flush();
    }

    protected Factura nuevaFactura() {
        Factura r = new Factura(UUID.randomUUID());
        r.setCliente("Integración cliente");
        r.setFechaFacturacion(new Date());
        r.setEstado("EMITIDA");
        r.setIdCaja(caja);
        r.setIdEmpleadoRol(empleadoRol);
        return r;
    }

    @Override
    protected <R> R registroValido(Class<R> tipo) {
        Object r;
        if (tipo == Caja.class) {
            Caja c = new Caja(UUID.randomUUID()); c.setNombre("Integración caja nueva"); c.setActivo(true); r = c;
        } else if (tipo == Factura.class) r = nuevaFactura();
        else if (tipo == Pago.class) {
            Pago p = new Pago(UUID.randomUUID()); p.setIdFactura(factura); p.setEstado("CONFIRMADO"); r = p;
        } else if (tipo == PagoDetalle.class) {
            PagoDetalle d = new PagoDetalle(UUID.randomUUID()); d.setIdPago(pago); d.setMonto(BigDecimal.ONE);
            d.setTipoPago("EFECTIVO"); r = d;
        } else if (tipo == FacturaOrdenProducto.class) {
            FacturaOrdenProducto f = new FacturaOrdenProducto(UUID.randomUUID());
            f.setIdFactura(factura); f.setIdOrdenProducto(linea); f.setPrecio(linea.getPrecio()); r = f;
        } else return super.registroValido(tipo);
        return tipo.cast(r);
    }

    protected FacturacionService servicio() {
        FacturacionService s = new FacturacionService();
        inyectar(s, FacturacionService.class, "em", em);
        inyectar(s, FacturacionService.class, "facturaDAO", dao(new FacturaDAO()));
        inyectar(s, FacturacionService.class, "lineaDAO", dao(new FacturaOrdenProductoDAO()));
        inyectar(s, FacturacionService.class, "pagoDAO", dao(new PagoDAO()));
        inyectar(s, FacturacionService.class, "detalleDAO", dao(new PagoDetalleDAO()));
        return s;
    }
}
