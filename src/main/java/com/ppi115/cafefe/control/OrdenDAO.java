package com.ppi115.cafefe.control;

import java.util.UUID;

import com.ppi115.cafefe.entity.OrdenProducto;

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

/** Acceso a datos de orden de venta. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class OrdenDAO extends DefaultDAO<Orden> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    @jakarta.inject.Inject
    private OrdenProductoDAO ordenProductoDao;

    /**
     * Inicializa OrdenDAO con los datos recibidos.
     */
    public OrdenDAO() {
        super(Orden.class);
    }

    /**
     * Proporciona el contexto de persistencia administrado por Jakarta EE.
     * @return entity manager actual
     */
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /** Trae empleado y rol con JOIN FETCH para mostrarlos en la tabla. */
    @Override
    public List<Orden> findRange(int first, int max, Map<String, FilterMeta> filterBy) {
        try {
            validarRango(first, max);
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
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /** findById con empleado y rol ya cargados (para editar). */
    @Override
    public Orden findById(Object id) {
        try {
            if (id == null) throw new DatosInvalidosException("El ID de la orden es obligatorio");
            List<Orden> r = em.createQuery(
                    "SELECT o FROM Orden o "
                    + "LEFT JOIN FETCH o.idEmpleadoRol er "
                    + "LEFT JOIN FETCH er.idEmpleado "
                    + "LEFT JOIN FETCH er.idRol "
                    + "WHERE o.idOrden = :id", Orden.class)
                    .setParameter("id", id)
                    .getResultList();
            return r.isEmpty() ? null : r.get(0);
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /** Empleados activos con rol activo "Camarero". */
    public List<EmpleadoRol> findCamareros() {
        try {
            return em.createQuery(
                    "SELECT er FROM EmpleadoRol er "
                    + "JOIN FETCH er.idEmpleado e "
                    + "JOIN FETCH er.idRol r "
                    + "WHERE LOWER(r.nombre) = 'camarero' "
                    + "AND er.activo = true AND r.activo = true AND e.activo = true "
                    + "ORDER BY e.nombre, e.apellido",
                    EmpleadoRol.class)
                    .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /** Borra primero las líneas de la orden y luego la orden. */
    @Override
    public void eliminar(Object id) throws IllegalArgumentException {
        try {
            if (id == null) {
                throw new DatosInvalidosException("El ID no puede ser null");
            }
            em.createQuery("DELETE FROM OrdenProducto op WHERE op.idOrden.idOrden = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            super.eliminar(id);
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Comprueba las restricciones y las reglas de la entidad antes de escribir en la base de datos.
     * @param r registro que se desea validar o editar
     */
    @Override
    protected void validar(Orden r) {
        super.validar(r);
        // Las relaciones se eligen al crear; al editar se conservan las ya persistidas.
        Orden anterior = em.find(Orden.class, r.getIdOrden());
        if (anterior != null && !java.util.Objects.equals(
                anterior.getIdEmpleadoRol() == null ? null : anterior.getIdEmpleadoRol().getIdEmpleadoRol(),
                r.getIdEmpleadoRol() == null ? null : r.getIdEmpleadoRol().getIdEmpleadoRol()))
            throw new DatosInvalidosException("Las relaciones no pueden cambiarse durante la edición");

        if (r.getFechaOrden() == null) throw new DatosInvalidosException("La fecha de la orden es obligatoria");
        var empleadoRol = exigirReferencia(com.ppi115.cafefe.entity.EmpleadoRol.class,
                r.getIdEmpleadoRol() == null ? null : r.getIdEmpleadoRol().getIdEmpleadoRol(), "El empleado de la orden");
        if (empleadoRol.getIdEmpleado() == null || !Boolean.TRUE.equals(empleadoRol.getIdEmpleado().getActivo())
                || empleadoRol.getIdRol() == null || !Boolean.TRUE.equals(empleadoRol.getIdRol().getActivo())
                || empleadoRol.getIdRol().getNombre() == null
                || !"camarero".equalsIgnoreCase(empleadoRol.getIdRol().getNombre().trim()))
            throw new DatosInvalidosException("La orden solo puede asignarse a un camarero activo");
        if (!Boolean.TRUE.equals(empleadoRol.getActivo()))
            throw new DatosInvalidosException("La asignación del empleado está inactiva");
        r.setIdEmpleadoRol(empleadoRol);
    }

    /**
     * Guarda la cabecera y sus líneas en la misma transacción administrada por este EJB.
     * @param orden orden utilizado por la operación
     * @param esNueva indica si la orden aún no ha sido guardada
     * @param lineas líneas de producto de la orden
     * @param lineasEliminadas identificadores de las líneas retiradas de la orden
     */
    public void guardarConProductos(Orden orden, boolean esNueva,
            List<OrdenProducto> lineas, List<UUID> lineasEliminadas) {

        if (orden == null) {
            throw new DatosInvalidosException("La orden no puede ser null");
        }
        if (orden.getIdEmpleadoRol() == null) {
            throw new DatosInvalidosException("Debe seleccionar un empleado");
        }

        if (esNueva) {
            crear(orden);
        } else {
            modificar(orden);
        }

        Orden managed = findById(orden.getIdOrden());

        if (lineasEliminadas != null) {
            for (UUID id : lineasEliminadas) {
                OrdenProducto existente = ordenProductoDao.findById(id);
                if (existente == null || existente.getIdOrden() == null
                        || !existente.getIdOrden().getIdOrden().equals(orden.getIdOrden()))
                    throw new DefaultDAO.DatosInvalidosException("La línea que se quiere eliminar no pertenece a esta orden");
                ordenProductoDao.eliminar(id);
            }
        }
        if (lineas != null) {
            for (OrdenProducto l : lineas) {
                if (l == null || l.getIdOrdenProducto() == null)
                    throw new DefaultDAO.DatosInvalidosException("La línea de producto es obligatoria y debe tener identificador");
                OrdenProducto existente = ordenProductoDao.findById(l.getIdOrdenProducto());
                if (existente != null && (existente.getIdOrden() == null
                        || !existente.getIdOrden().getIdOrden().equals(orden.getIdOrden())))
                    throw new DefaultDAO.DatosInvalidosException("La línea de producto pertenece a otra orden");
                l.setIdOrden(managed);
                if (existente == null) {
                    ordenProductoDao.crear(l);
                } else {
                    ordenProductoDao.modificar(l);
                }
            }
        }
    }
}
