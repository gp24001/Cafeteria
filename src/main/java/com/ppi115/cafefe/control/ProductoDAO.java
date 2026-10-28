package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Producto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Acceso a datos de producto del catálogo. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class ProductoDAO extends DefaultDAO<Producto> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa ProductoDAO con los datos recibidos.
     */
    public ProductoDAO() {
        super(Producto.class);
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
     * Consulta un producto requerido por una asignación y comprueba su estado.
     * @param em contexto de persistencia de la operación
     * @param producto referencia que envió la capa superior
     * @return producto existente y activo
     * @throws IllegalArgumentException si falta el producto, no existe o está inactivo
     */
    static Producto exigirActivo(EntityManager em, Producto producto) {
        if (producto == null || producto.getIdProducto() == null)
            throw new DatosInvalidosException("El producto es obligatorio");
        Producto existente = em.find(Producto.class, producto.getIdProducto());
        if (existente == null) throw new DatosInvalidosException("El producto no existe");
        if (!existente.getActivo()) throw new DatosInvalidosException("El producto está inactivo");
        return existente;
    }
}
