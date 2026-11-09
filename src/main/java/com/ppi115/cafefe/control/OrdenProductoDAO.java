package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.OrdenProducto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

/** Acceso a datos de línea de producto de una orden. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@jakarta.enterprise.context.Dependent
@Stateless
@LocalBean
public class OrdenProductoDAO extends DefaultDAO<OrdenProducto> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa OrdenProductoDAO con los datos recibidos.
     */
    public OrdenProductoDAO() {
        super(OrdenProducto.class);
    }

    /**
     * Proporciona el contexto de persistencia administrado por Jakarta EE.
     * @return entity manager actual
     */
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /** Líneas de una orden con el producto ya cargado. */
    public List<OrdenProducto> findByOrden(Object idOrden) {
        try {
            if (idOrden == null) throw new DatosInvalidosException("El ID de la orden es obligatorio");
            return em.createQuery(
                    "SELECT op FROM OrdenProducto op "
                    + "LEFT JOIN FETCH op.idProducto "
                    + "WHERE op.idOrden.idOrden = :id", OrdenProducto.class)
                    .setParameter("id", idOrden)
                    .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Comprueba las restricciones y las reglas de la entidad antes de escribir en la base de datos.
     * @param r registro que se desea validar o editar
     */
    @Override
    protected void validar(OrdenProducto r) {
        OrdenProducto actual = r == null || r.getIdOrdenProducto() == null ? null
                : em.find(OrdenProducto.class, r.getIdOrdenProducto());
        if (actual != null) protegerOrden(actual);
        if (r != null) protegerOrden(r);
        super.validar(r);
        if (r.getPrecio() == null || r.getPrecio().signum() < 0)
            throw new DatosInvalidosException("El precio es obligatorio y no puede ser negativo");
        var orden = exigirReferencia(com.ppi115.cafefe.entity.Orden.class,
                r.getIdOrden() == null ? null : r.getIdOrden().getIdOrden(), "La orden");
        r.setIdOrden(orden);
        r.setIdProducto(ProductoDAO.exigirActivo(getEntityManager(), r.getIdProducto()));
    }
    /** Comprueba la facturación y elimina la línea dentro de la misma transacción. */
    @Override
    public void eliminar(Object id) {
        OrdenProducto actual = id == null ? null : em.find(OrdenProducto.class, id);
        if (actual != null) protegerOrden(actual);
        super.eliminar(id);
    }

    /** Impide cambiar una orden que ya tiene una factura vigente.
     * Bloquea la orden antes de comprobar sus productos facturados.
     */
    private void protegerOrden(OrdenProducto registro) {
        UUID id = (registro.getIdOrden() == null ? null : registro.getIdOrden().getIdOrden());
        if (id == null) return;
        // Bloquea la fila sin refrescar ni descartar los cambios del formulario JSF.
        if (em.createNativeQuery("SELECT id_orden FROM public.orden "
                + "WHERE id_orden = CAST(? AS uuid) FOR UPDATE")
                .setParameter(1, id.toString()).getResultList().isEmpty()) return;
        long facturadas = em.createQuery("SELECT COUNT(f) FROM FacturaOrdenProducto f "
                + "WHERE f.idOrdenProducto.idOrden.idOrden = :id AND f.idFactura.estado <> 'ANULADA'", Long.class)
                .setParameter("id", id).getSingleResult();
        if (facturadas > 0) throw new DefaultDAO.DatosInvalidosException("No se puede modificar una orden que ya está facturada");
    }
}
