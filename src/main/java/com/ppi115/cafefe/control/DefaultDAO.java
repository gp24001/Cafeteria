
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
    public List<T> findRange(int first, int max) {
        CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
    }
    
    public List<T> findRange(int first,int max,Map<String, FilterMeta> filterBy) {
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
}

    @Override
    public int contar() {
        CriteriaQuery<Long> cq = getEntityManager().getCriteriaBuilder().createQuery(Long.class);
        cq.select(getEntityManager().getCriteriaBuilder().count(cq.from(entityClass)));
        return getEntityManager().createQuery(cq).getSingleResult().intValue();
    }
    
    public int contar(Map<String, FilterMeta> filterBy) {

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
}
    
    
}
