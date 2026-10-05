package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.EmpleadoRol;
import com.ppi115.cafefe.entity.Orden;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.FilterMeta;

@Stateless
@LocalBean
public class OrdenDAO extends DefaultDAO<Orden> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    public OrdenDAO() {
        super(Orden.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /** Trae empleado y rol con JOIN FETCH para mostrarlos en la tabla. */
    @Override
    public List<Orden> findRange(int first, int max, Map<String, FilterMeta> filterBy) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Orden> cq = cb.createQuery(Orden.class);
        Root<Orden> root = cq.from(Orden.class);

        Fetch<Orden, ?> er = root.fetch("idEmpleadoRol", JoinType.LEFT);
        er.fetch("idEmpleado", JoinType.LEFT);
        er.fetch("idRol", JoinType.LEFT);

        cq.select(root);

        if (filterBy != null && !filterBy.isEmpty()) {
            List<Predicate> predicates = new ArrayList<>();
            for (FilterMeta filtro : filterBy.values()) {
                if (filtro == null) {
                    continue;
                }
                String campo = filtro.getField();
                Object valor = filtro.getFilterValue();
                if (campo == null || campo.isBlank() || valor == null || valor.toString().isBlank()) {
                    continue;
                }
                String texto = valor.toString().trim().toLowerCase();
                predicates.add(cb.like(cb.lower(root.get(campo).as(String.class)), "%" + texto + "%"));
            }
            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[0]));
            }
        }

        cq.orderBy(cb.desc(root.get("fechaOrden")));

        TypedQuery<Orden> query = em.createQuery(cq);
        query.setFirstResult(first);
        query.setMaxResults(max);
        return query.getResultList();
    }

    /** findById con empleado y rol ya cargados (para editar). */
    @Override
    public Orden findById(Object id) {
        List<Orden> r = em.createQuery(
                "SELECT o FROM Orden o "
                + "LEFT JOIN FETCH o.idEmpleadoRol er "
                + "LEFT JOIN FETCH er.idEmpleado "
                + "LEFT JOIN FETCH er.idRol "
                + "WHERE o.idOrden = :id", Orden.class)
                .setParameter("id", id)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }

    /** Empleados activos con rol activo "Camarero". */
    public List<EmpleadoRol> findCamareros() {
        return em.createQuery(
                "SELECT er FROM EmpleadoRol er "
                + "JOIN FETCH er.idEmpleado e "
                + "JOIN FETCH er.idRol r "
                + "WHERE LOWER(r.nombre) = 'camarero' "
                + "AND er.activo = true AND r.activo = true AND e.activo = true "
                + "ORDER BY e.nombre, e.apellido",
                EmpleadoRol.class)
                .getResultList();
    }

    /** Borra primero las líneas de la orden y luego la orden. */
    @Override
    public void eliminar(Object id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        em.createQuery("DELETE FROM OrdenProducto op WHERE op.idOrden.idOrden = :id")
                .setParameter("id", id)
                .executeUpdate();
        super.eliminar(id);
    }
}