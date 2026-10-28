package com.ppi115.cafefe.boundary.jsf;

import jakarta.inject.Inject;

import com.ppi115.cafefe.control.TipoDescuentoDAO;
import com.ppi115.cafefe.entity.TipoDescuento;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;

/** Estado y acciones JSF de tipo y límite de descuento. Coordina la vista y delega el acceso a datos en los DAO. */
@Named("tipoDescuentoModel")
@ViewScoped
public class TipoDescuentoModel extends AbstractModel<TipoDescuento> implements Serializable {

    @Inject
    private TipoDescuentoDAO dao;

    /**
     * Inicializa TipoDescuentoModel con los datos recibidos.
     */
    public TipoDescuentoModel() {
        setNombreBean("Tipo de Descuento");
    }

    /**
     * Proporciona el DAO utilizado para leer y guardar los registros de esta vista.
     * @return dao actual
     */
    @Override
    public TipoDescuentoDAO getDao() {
        return dao;
    }

    /**
     * Construye un registro nuevo con su UUID y los valores iniciales de esta vista.
     * @return registro nuevo con su identificador y valores iniciales
     */
    @Override
    public TipoDescuento createRegistrer() {
        TipoDescuento r = new TipoDescuento();
        r.setIdTipoDescuento(UUID.randomUUID());
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
        if (id == null) return null;
        return consultar(() -> dao.findById(id), null);
    }

    /**
     * Obtiene el identificador usado por la selección y las tablas de JSF.
     * @param registro registro que se desea consultar o editar
     * @return identificador del registro, o null si no hay registro
     */
    @Override
    public Object getIdByRegistrer(TipoDescuento registro) {
        if (registro == null) return null;
        return registro.getIdTipoDescuento();
    }
}
