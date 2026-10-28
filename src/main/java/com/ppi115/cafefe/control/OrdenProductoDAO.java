package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.OrdenProducto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/** Acceso a datos de línea de producto de una orden. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
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
        super.validar(r);
        if (r.getPrecio() == null || r.getPrecio().signum() < 0)
            throw new DatosInvalidosException("El precio es obligatorio y no puede ser negativo");
        var orden = exigirReferencia(com.ppi115.cafefe.entity.Orden.class,
                r.getIdOrden() == null ? null : r.getIdOrden().getIdOrden(), "La orden");
        r.setIdOrden(orden);
        r.setIdProducto(ProductoDAO.exigirActivo(getEntityManager(), r.getIdProducto()));
    }
}
