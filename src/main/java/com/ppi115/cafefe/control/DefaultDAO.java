
package com.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.List;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Map;
import org.primefaces.model.FilterMeta;
/** Operaciones comunes de los DAO: valida restricciones, consulta referencias con EntityManager y traduce errores de persistencia. */

public abstract class DefaultDAO<T> implements DAOInterface<T>{

    @jakarta.inject.Inject
    private jakarta.validation.Validator validator;

    private final Class<T> entityClass;

    /**
     * Comprueba las restricciones y las reglas de la entidad antes de escribir en la base de datos.
     * @param entity entidad que se desea persistir
     */
    protected void validar(T entity) {
        if (entity == null) {
            throw new DatosInvalidosException("La entidad no puede ser nula");
        }
        var errores = validator.validate(entity);
        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores.stream()
                    .map(e -> e.getPropertyPath() + ": " + e.getMessage())
                    .sorted().collect(java.util.stream.Collectors.joining("; ")));
        }
    }

    /**
     * Consulta con EntityManager una referencia obligatoria y comprueba que exista.
     * @param tipo clase de la referencia que se consulta
     * @param id identificador del registro solicitado
     * @param nombre nombre usado para identificar el dato en los mensajes
     * @return referencia existente recuperada del contexto de persistencia
     */
    protected <R> R exigirReferencia(Class<R> tipo, Object id, String nombre) {
        if (id == null) throw new DatosInvalidosException(nombre + " es obligatorio");
        R registro = getEntityManager().find(tipo, id);
        if (registro == null) throw new DatosInvalidosException(nombre + " no existe");
        return registro;
    }

    /**
     * Rechaza desplazamientos negativos y tamaños de página no positivos.
     * @param first posición inicial de la página, comenzando en cero
     * @param max cantidad máxima de registros que se devolverán
     */
    protected void validarRango(int first, int max) {
        if (first < 0 || max <= 0) {
            throw new DatosInvalidosException("El inicio debe ser mayor o igual a cero y el máximo positivo");
        }
    }

    /**
     * Proporciona el contexto de persistencia administrado por Jakarta EE.
     * @return entity manager actual
     */
    protected abstract EntityManager getEntityManager();

    /**
     * Inicializa DefaultDAO con los datos recibidos.
     * @param entityClass entity class utilizado por la operación

     */
    public DefaultDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Valida y persiste un registro nuevo dentro de la transacción actual.
     * @param entity entidad que se desea persistir
     */
    @Override
    public void crear(T entity) {
        try {
            validar(entity);
            getEntityManager().persist(entity);
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo crear el registro. Revise sus datos y relaciones", e);
        }
    }

    /**
     * Valida y actualiza un registro dentro de la transacción actual.
     * @param entity entidad que se desea persistir
     */
    @Override
    public void modificar(T entity) throws IllegalArgumentException {
        if (entity == null) {
            throw new DatosInvalidosException("La entidad no puede ser null");
        }
        try {
            validar(entity);
            getEntityManager().merge(entity);
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo modificar el registro. Revise sus datos y relaciones", e);
        }
    }

    /**
     * Elimina el registro indicado y comunica los errores al nivel que invoca la operación.
     * @param id identificador del registro solicitado
     */
    @Override
    public void eliminar(Object id) throws IllegalArgumentException {
        if (id == null) {
            throw new DatosInvalidosException("El ID no puede ser null");
        }

        T entity = findById(id);

        if (entity != null) {
            try {
                getEntityManager().remove(entity);
            } catch (jakarta.persistence.PersistenceException e) {
                throw new DatosInvalidosException("No se pudo eliminar el registro. Puede tener datos relacionados", e);
            }
        }
    }

    /**
     * Busca un registro por su identificador; devuelve null cuando no existe.
     * @param id identificador del registro solicitado
     * @return registro encontrado, o null si no existe
     */
    @Override
    public T findById(Object id) {
        try {
        if (id == null) {
            throw new DatosInvalidosException("El ID es obligatorio");
        }
        return getEntityManager().find(entityClass, id);

        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Consulta todos los registros del catálogo en la base de datos.
     * @return lista de registros que cumplen la consulta
     */
    @Override
    public List<T> findAll() {
        try {
            CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
            cq.select(cq.from(entityClass));
            return getEntityManager().createQuery(cq).getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Consulta una página de registros y aplica los filtros recibidos.
     * @param first posición inicial de la página, comenzando en cero
     * @param max cantidad máxima de registros que se devolverán
     * @return lista de registros que cumplen la consulta
     */
    @Override
    public List<T> findRange(int first, int max) {
        try {
            validarRango(first, max);
            CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
            cq.select(cq.from(entityClass));
            return getEntityManager().createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Consulta una página de registros y aplica los filtros recibidos.
     * @param first posición inicial de la página, comenzando en cero
     * @param max cantidad máxima de registros que se devolverán
     * @param filterBy filtros activos de la tabla
     * @return lista de registros que cumplen la consulta
     */
    public List<T> findRange(int first,int max,Map<String, FilterMeta> filterBy) {
        try {
            validarRango(first, max);
            CriteriaBuilder cb =getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> cq =cb.createQuery(entityClass);
            Root<T> root =cq.from(entityClass);
            cq.select(root);

            if (filterBy != null && !filterBy.isEmpty()) {
                ArrayList<Predicate> predicates = new ArrayList<>();

                for (FilterMeta filtro : filterBy.values()) {
                    if (filtro == null) {
                        continue;
                    }
                    String campo =filtro.getField();
                    Object valor =filtro.getFilterValue();
                    if (campo == null|| campo.isBlank()|| valor == null|| valor.toString().isBlank()) {
                        continue;
                    }
                    String texto =valor.toString().trim().toLowerCase();
                    predicates.add(cb.like(cb.lower(root.get(campo).as(String.class) ),"%" + texto + "%" ));
                }
                if (!predicates.isEmpty()) {
                    cq.where(predicates.toArray(new Predicate[0]));
                }
            }
            TypedQuery<T> query =getEntityManager().createQuery(cq);
            query.setFirstResult(first);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Cuenta los registros que cumplen los filtros, si se proporcionan.
     * @return cantidad de registros que cumplen la consulta
     */
    @Override
    public int contar() {
        try {
            CriteriaQuery<Long> cq = getEntityManager().getCriteriaBuilder().createQuery(Long.class);
            cq.select(getEntityManager().getCriteriaBuilder().count(cq.from(entityClass)));
            return getEntityManager().createQuery(cq).getSingleResult().intValue();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Cuenta los registros que cumplen los filtros, si se proporcionan.
     * @param filterBy filtros activos de la tabla
     * @return cantidad de registros que cumplen la consulta
     */
    public int contar(Map<String, FilterMeta> filterBy) {
        try {
            CriteriaBuilder cb =getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Long> cq =cb.createQuery(Long.class);
            Root<T> root =cq.from(entityClass);
            cq.select(cb.count(root));

            if (filterBy != null && !filterBy.isEmpty()) {
                ArrayList<Predicate> predicates =new ArrayList<>();
                for (FilterMeta filtro : filterBy.values()) {
                    if (filtro == null) {
                        continue;
                    }

                    String campo =filtro.getField();
                    Object valor =filtro.getFilterValue();

                    if (campo == null|| campo.isBlank()|| valor == null|| valor.toString().isBlank()) {
                        continue;
                    }
                    String texto =valor.toString().trim().toLowerCase();
                    predicates.add(cb.like(cb.lower(root.get(campo).as(String.class)),"%" + texto + "%"));
                }
                if (!predicates.isEmpty()) {
                    cq.where(predicates.toArray(new Predicate[0]));
                }
            }
            return getEntityManager().createQuery(cq).getSingleResult().intValue();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /** Error de validación que conserva su mensaje al atravesar un EJB. */
    @jakarta.ejb.ApplicationException(rollback = true)
    public static class DatosInvalidosException extends IllegalArgumentException {
        /**
         * Inicializa DatosInvalidosException con los datos recibidos.
         * @param mensaje mensaje utilizado por la operación

         */
        public DatosInvalidosException(String mensaje) { super(mensaje); }
        /**
         * Inicializa DatosInvalidosException con los datos recibidos.
         * @param mensaje mensaje utilizado por la operación
         * @param causa causa utilizado por la operación

         */
        public DatosInvalidosException(String mensaje, Throwable causa) { super(mensaje, causa); }
    }
}
