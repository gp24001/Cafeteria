package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Orden;
import com.ppi115.cafefe.entity.OrdenProducto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

/** Guarda la orden y sus líneas en UNA sola transacción. */
@Stateless
@LocalBean
public class OrdenService {

    @Inject
    private OrdenDAO ordenDao;

    @Inject
    private OrdenProductoDAO ordenProductoDao;

    public void guardar(Orden orden, boolean esNueva,
            List<OrdenProducto> lineas, List<UUID> lineasEliminadas) {

        if (orden == null) {
            throw new IllegalArgumentException("La orden no puede ser null");
        }
        if (orden.getIdEmpleadoRol() == null) {
            throw new IllegalArgumentException("Debe seleccionar un empleado");
        }

        if (esNueva) {
            ordenDao.crear(orden);
        } else {
            ordenDao.modificar(orden);
        }

        Orden managed = ordenDao.findById(orden.getIdOrden());

        if (lineasEliminadas != null) {
            for (UUID id : lineasEliminadas) {
                ordenProductoDao.eliminar(id);
            }
        }
        if (lineas != null) {
            for (OrdenProducto l : lineas) {
                l.setIdOrden(managed);
                if (ordenProductoDao.findById(l.getIdOrdenProducto()) == null) {
                    ordenProductoDao.crear(l);
                } else {
                    ordenProductoDao.modificar(l);
                }
            }
        }
    }
}