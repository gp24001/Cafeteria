
package com.ppi115.cafefe.control;

import java.util.List;

public interface DAOInterface<T> {
     void crear(T entity);

    void modificar(T entity);

    void eliminar(Object id);

    T findById(Object id);

    List<T> findAll();

    List<T> findRange(int first, int max);

    int contar();

}

