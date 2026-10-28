package com.ppi115.cafefe.boundary.jsf;

import jakarta.inject.Inject;

import com.ppi115.cafefe.control.RolDAO;
import com.ppi115.cafefe.entity.Rol;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;

/** Estado y acciones JSF de rol de empleado. Coordina la vista y delega el acceso a datos en los DAO. */
@Named("rolModel")
@ViewScoped
public class RolModel extends AbstractModel<Rol> implements Serializable {

    @Inject
    private RolDAO dao;

    /**
     * Proporciona el DAO utilizado para leer y guardar los registros de esta vista.
     * @return dao actual
     */
    @Override
    public RolDAO getDao() {
        return dao;
    }

    /**
     * Construye un registro nuevo con su UUID y los valores iniciales de esta vista.
     * @return registro nuevo con su identificador y valores iniciales
     */
    @Override
    public Rol createRegistrer() {
        Rol r = new Rol();
        r.setIdRol(UUID.randomUUID());
        r.setActivo(true);
        return r;
    }

    /**
     * Recupera el registro solicitado a través del DAO de la vista.
     * @param id identificador del registro solicitado
     * @return registro encontrado, o null si no existe
     */
    @Override
    public Object getRegistrerById(Object id) {
        if (id == null) {
            return null;
        }
        return consultar(() -> dao.findById(id), null);
    }

    /**
     * Obtiene el identificador usado por la selección y las tablas de JSF.
     * @param registro registro que se desea consultar o editar
     * @return identificador del registro, o null si no hay registro
     */
    @Override
    public Object getIdByRegistrer(Rol registro) {
        if (registro == null) {
            return null;
        }
        return registro.getIdRol();
    }

    /**
     * Inicializa RolModel con los datos recibidos.
     */
    public RolModel() {
        setNombreBean("Rol");
    }
}
