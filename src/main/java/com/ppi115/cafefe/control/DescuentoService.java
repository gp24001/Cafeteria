package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Descuento;
import com.ppi115.cafefe.entity.DescuentoProducto;
import com.ppi115.cafefe.entity.OrdenProducto;
import com.ppi115.cafefe.entity.Producto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TemporalType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Stateless
@LocalBean
public class DescuentoService {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /** Resultado de aplicar un descuento a las líneas de una orden. */
    public static class Resultado {
        public final int aplicadas;
        public final int repetidas;

        public Resultado(int aplicadas, int repetidas) {
            this.aplicadas = aplicadas;
            this.repetidas = repetidas;
        }
    }

    /**
     * Descuentos de los productos dados vigentes en la fecha de la orden.
     * Vigente = la fecha cae en el rango del descuento y del descuento_producto
     * (fecha_fin null = rango abierto).
     */
    public List<DescuentoProducto> findVigentes(Collection<Producto> productos, Date fecha) {
        if (productos == null || productos.isEmpty() || fecha == null) {
            return new ArrayList<>();
        }
        return em.createQuery(
                "SELECT dp FROM DescuentoProducto dp "
                + "JOIN FETCH dp.idDescuento d "
                + "JOIN FETCH dp.idProducto p "
                + "WHERE p IN :productos "
                + "AND dp.valor IS NOT NULL "
                + "AND dp.fechaInicio <= :fecha "
                + "AND (dp.fechaFin IS NULL OR dp.fechaFin >= :fecha) "
                + "AND d.fechaInicio <= :fecha "
                + "AND (d.fechaFin IS NULL OR d.fechaFin >= :fecha) "
                + "ORDER BY d.nombre", DescuentoProducto.class)
                .setParameter("productos", productos)
                .setParameter("fecha", fecha, TemporalType.TIMESTAMP)
                .getResultList();
    }

    /**
     * Aplica el descuento a las líneas cuyo producto lo tenga vigente.
     * Cambia en memoria el precio y las observaciones de cada línea.
     * El porcentaje se calcula sobre el precio ACTUAL de la línea.
     */
    public Resultado aplicar(Descuento descuento, List<OrdenProducto> lineas, Date fecha) {
        if (descuento == null || lineas == null || lineas.isEmpty()) {
            return new Resultado(0, 0);
        }

        List<Producto> productos = new ArrayList<>();
        for (OrdenProducto l : lineas) {
            if (l.getIdProducto() != null) {
                productos.add(l.getIdProducto());
            }
        }

        Map<UUID, Integer> porcentajes = new HashMap<>();
        Descuento desc = null;
        for (DescuentoProducto dp : findVigentes(productos, fecha)) {
            if (dp.getIdDescuento().getIdDescuento().equals(descuento.getIdDescuento())) {
                porcentajes.put(dp.getIdProducto().getIdProducto(), dp.getValor());
                desc = dp.getIdDescuento();
            }
        }
        if (desc == null) {
            return new Resultado(0, 0);
        }

        String marca = "Descuento " + desc.getNombre() + " (";
        int aplicadas = 0;
        int repetidas = 0;

        for (OrdenProducto l : lineas) {
            if (l.getIdProducto() == null || l.getPrecio() == null) {
                continue;
            }
            Integer pct = porcentajes.get(l.getIdProducto().getIdProducto());
            if (pct == null) {
                continue;
            }
            String obs = l.getObservaciones() == null ? "" : l.getObservaciones();
            if (obs.contains(marca)) {
                repetidas++;
                continue;
            }

            int p = Math.max(0, Math.min(100, pct));
            BigDecimal anterior = l.getPrecio();
            BigDecimal nuevo = anterior.multiply(BigDecimal.valueOf(100 - p))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal monto = anterior.subtract(nuevo);

            String comentario = marca + p + "%): de " + anterior.setScale(2, RoundingMode.HALF_UP)
                    + " a " + nuevo + " (-" + monto + ")";

            l.setPrecio(nuevo);
            l.setObservaciones(obs.isBlank() ? comentario : obs + " | " + comentario);
            aplicadas++;
        }
        return new Resultado(aplicadas, repetidas);
    }
}