
package com.ppi115.cafefe.control;

import java.util.List;
/** Contrato de operaciones CRUD y consultas que implementan los DAO. */

public interface DAOInterface<T> {
     /**
      * Valida y persiste un registro nuevo dentro de la transacción actual.
      * @param entity entidad que se desea persistir
      */
     void crear(T entity);

    /**
     * Valida y actualiza un registro dentro de la transacción actual.
     * @param entity entidad que se desea persistir
     */
    void modificar(T entity);

    /**
     * Elimina el registro indicado y comunica los errores al nivel que invoca la operación.
     * @param id identificador del registro solicitado
     */
    void eliminar(Object id);

    /**
     * Busca un registro por su clave primaria.
     * @param id identificador obligatorio del registro
     * @return entidad encontrada, o null cuando no existe
     */
    T findById(Object id);

    /** @return todos los registros de la entidad administrada */
    List<T> findAll();

    /**
     * Consulta una página de registros.
     * @param first posición inicial, comenzando en cero
     * @param max tamaño positivo de la página
     * @return registros de la página solicitada
     */
    List<T> findRange(int first, int max);

    /** @return cantidad total de registros de la entidad */
    int contar();

}

