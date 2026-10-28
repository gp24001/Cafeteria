package com.ppi115.cafefe.control;

import java.util.Map;

import java.util.HashMap;

import java.util.Date;

import java.util.Collection;

import java.util.ArrayList;

import java.math.RoundingMode;

import java.math.BigDecimal;

import jakarta.persistence.TemporalType;

import com.ppi115.cafefe.entity.Producto;

import com.ppi115.cafefe.entity.OrdenProducto;

import com.ppi115.cafefe.entity.Descuento;

import com.ppi115.cafefe.entity.DescuentoProducto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

/** Acceso a datos de descuento asignado a un producto. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class DescuentoProductoDAO extends DefaultDAO<DescuentoProducto> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa DescuentoProductoDAO con los datos recibidos.
     */
    public DescuentoProductoDAO() {
        super(DescuentoProducto.class);
    }

    /**
     * Proporciona el contexto de persistencia administrado por Jakarta EE.
     * @return entity manager actual
     */
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Comprueba las restricciones y las reglas de la entidad antes de escribir en la base de datos.
     * @param r registro que se desea validar o editar
     */
    @Override
    protected void validar(DescuentoProducto r) {
        super.validar(r);
        // Las relaciones se eligen al crear; al editar se conservan las ya persistidas.
        DescuentoProducto anterior = em.find(DescuentoProducto.class, r.getIdDescuentoProducto());
        if (anterior != null && !java.util.Objects.equals(
                anterior.getIdProducto() == null ? null : anterior.getIdProducto().getIdProducto(),
                r.getIdProducto() == null ? null : r.getIdProducto().getIdProducto()))
            throw new DatosInvalidosException("Las relaciones no pueden cambiarse durante la edición");
        if (anterior != null && !java.util.Objects.equals(
                anterior.getIdDescuento() == null ? null : anterior.getIdDescuento().getIdDescuento(),
                r.getIdDescuento() == null ? null : r.getIdDescuento().getIdDescuento()))
            throw new DatosInvalidosException("Las relaciones no pueden cambiarse durante la edición");


        var producto = ProductoDAO.exigirActivo(getEntityManager(), r.getIdProducto());
        var descuento = exigirReferencia(com.ppi115.cafefe.entity.Descuento.class,
                r.getIdDescuento() == null ? null : r.getIdDescuento().getIdDescuento(), "El descuento");
        var tipo = descuento.getIdTipoDescuento();
        if (tipo == null || !Boolean.TRUE.equals(tipo.getActivo()))
            throw new DatosInvalidosException("El tipo de descuento está inactivo");
        if (r.getValor() == null || r.getValor() <= 0 || r.getValor() > 100)
            throw new DatosInvalidosException("El descuento debe ser mayor a cero y no superar 100%");
        if (tipo.getDescuentoMaximo() != null && r.getValor() > tipo.getDescuentoMaximo())
            throw new DatosInvalidosException("El descuento supera el máximo permitido");
        if (r.getFechaInicio() == null) throw new DatosInvalidosException("La fecha de inicio es obligatoria");
        if (r.getFechaFin() != null && r.getFechaFin().before(r.getFechaInicio()))
            throw new DatosInvalidosException("La fecha fin no puede ser anterior al inicio");
        if (descuento.getFechaInicio() != null && r.getFechaInicio().before(descuento.getFechaInicio()))
            throw new DatosInvalidosException("El inicio es anterior a la vigencia del descuento");
        if (descuento.getFechaFin() != null && (r.getFechaFin() == null
                || r.getFechaFin().after(descuento.getFechaFin())))
            throw new DatosInvalidosException("La fecha fin debe estar dentro de la vigencia del descuento");
        if (findByIdProducto(producto.getIdProducto()).stream().anyMatch(a ->
                a.getIdDescuento().getIdDescuento().equals(descuento.getIdDescuento())
                && !a.getIdDescuentoProducto().equals(r.getIdDescuentoProducto())))
            throw new DatosInvalidosException("Este descuento ya está asignado al producto");
        r.setIdProducto(producto);
        r.setIdDescuento(descuento);
    }

    /**
     * Consulta únicamente las asignaciones pertenecientes al producto indicado.
     * @param idProducto identificador del producto
     * @return lista de registros que cumplen la consulta
     */
    public List<DescuentoProducto> findByIdProducto(UUID idProducto) {
        try {
            if (idProducto == null) throw new DatosInvalidosException("El ID del producto es obligatorio");
            return em.createQuery(
                    "SELECT dp FROM DescuentoProducto dp JOIN FETCH dp.idProducto JOIN FETCH dp.idDescuento d LEFT JOIN FETCH d.idTipoDescuento "
                    + "WHERE dp.idProducto.idProducto = :idProducto",
                    DescuentoProducto.class)
                    .setParameter("idProducto", idProducto)
                    .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Comprueba en la base de datos si el producto ya tiene asignado el descuento.
     * @param idProducto identificador del producto
     * @param idDescuento id descuento utilizado por la operación
     * @return true cuando se cumple la condición comprobada
     */
    public boolean existeProductoDescuento(UUID idProducto, UUID idDescuento) {
        try {
            if (idProducto == null || idDescuento == null)
                throw new DatosInvalidosException("El producto y el descuento son obligatorios");
            Long cantidad = em.createQuery(
                    "SELECT COUNT(dp) FROM DescuentoProducto dp "
                    + "WHERE dp.idProducto.idProducto = :idProducto "
                    + "AND dp.idDescuento.idDescuento = :idDescuento",
                    Long.class)
                    .setParameter("idProducto", idProducto)
                    .setParameter("idDescuento", idDescuento)
                    .getSingleResult();

            return cantidad > 0;
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Muestra los descuentos asignados al producto, incluso fuera de vigencia.
     * La fecha se verifica de nuevo al aplicar, para explicar por qué no corresponde.
     * @param producto producto de la línea seleccionada
     * @return asignaciones del producto con su tipo de descuento cargado
     */
    public List<DescuentoProducto> findAsignados(Producto producto) {
        if (producto == null || producto.getIdProducto() == null)
            throw new DefaultDAO.DatosInvalidosException("Debe seleccionar un producto");
        try {
            return em.createQuery("SELECT dp FROM DescuentoProducto dp JOIN FETCH dp.idDescuento d "
                    + "JOIN FETCH d.idTipoDescuento t JOIN FETCH dp.idProducto p "
                    + "WHERE p.idProducto = :id AND p.activo = true AND t.activo = true ORDER BY d.nombre",
                    DescuentoProducto.class).setParameter("id", producto.getIdProducto()).getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DefaultDAO.DatosInvalidosException("No se pudieron consultar los descuentos del producto", e);
        }
    }

    /** Resultado de aplicar un descuento a las líneas de una orden. */
    public static class Resultado {
        public final int aplicadas;
        public final int repetidas;

        /**
         * Inicializa Resultado con los datos recibidos.
         * @param aplicadas aplicadas utilizado por la operación
         * @param repetidas repetidas utilizado por la operación

         */
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
        try {
        return em.createQuery(
                "SELECT dp FROM DescuentoProducto dp "
                + "JOIN FETCH dp.idDescuento d "
                + "JOIN FETCH dp.idProducto p "
                + "JOIN FETCH d.idTipoDescuento t "
                + "WHERE p IN :productos "
                + "AND p.activo = true AND t.activo = true "
                + "AND dp.valor > 0 AND dp.valor <= 100 AND dp.valor <= t.descuentoMaximo "
                + "AND dp.fechaInicio <= :fecha "
                + "AND (dp.fechaFin IS NULL OR dp.fechaFin >= :fecha) "
                + "AND d.fechaInicio <= :fecha "
                + "AND (d.fechaFin IS NULL OR d.fechaFin >= :fecha) "
                + "ORDER BY d.nombre", DescuentoProducto.class)
                .setParameter("productos", productos)
                .setParameter("fecha", fecha, TemporalType.TIMESTAMP)
                .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DefaultDAO.DatosInvalidosException("No se pudieron comprobar los descuentos vigentes", e);
        }
    }

    /**
     * Aplica un descuento exclusivamente a la línea elegida por el usuario.
     * @param descuento descuento asignado al producto
     * @param linea línea seleccionada en la orden
     * @param fecha fecha de la orden usada para comprobar la vigencia
     * @return resultado de la aplicación en esa línea
     */
    public Resultado aplicar(Descuento descuento, OrdenProducto linea, Date fecha) {
        if (linea == null || fecha == null)
            throw new DefaultDAO.DatosInvalidosException("Debe seleccionar una línea y la fecha de la orden");
        return aplicar(descuento, List.of(linea), fecha);
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
