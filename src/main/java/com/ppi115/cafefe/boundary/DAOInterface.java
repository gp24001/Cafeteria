/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ppi115.cafefe.boundary;

import java.util.List;

public interface DAOInterface<T> {
     void crear(T entity);

    void modificar(T entity);

    void eliminar(Object id);

    T findById(Object id);

    List<T> findAll();

    List<T> findRange(int first, int pageSize);

    int contar();

}

