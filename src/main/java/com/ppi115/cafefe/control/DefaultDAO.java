
package com.ppi115.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.List;


public abstract class DefaultDAO<T> implements DAOInterface<T>{
  
    private final Class<T> entityClass;

    protected abstract EntityManager getEntityManager();

    public DefaultDAO(Class<T> entityClass) {   
        this.entityClass = entityClass;
    }

    @Override
    public void crear(T entity) {
        getEntityManager().persist(entity);
    }

    @Override
    public void modificar(T entity) throws IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser null");
        }
        getEntityManager().merge(entity);
    }

    @Override
    public void eliminar(Object id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }

        T entity = findById(id);

        if (entity != null) {
            getEntityManager().remove(entity);
        }
    }

    @Override
    public T findById(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    @Override
    public List<T> findAll() {

        CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    @Override
    public List<T> findRange(int first, int pageSize) {
        CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).setFirstResult(first).setMaxResults(pageSize).getResultList();
    }

    @Override
    public int contar() {
        CriteriaQuery<Long> cq = getEntityManager().getCriteriaBuilder().createQuery(Long.class);
        cq.select(getEntityManager().getCriteriaBuilder().count(cq.from(entityClass)));
        return getEntityManager().createQuery(cq).getSingleResult().intValue();
    }
}
