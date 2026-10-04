package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.TipoDescuentoDAO;
import com.ppi115.cafefe.entity.TipoDescuento;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;

@Named("tipoDescuentoModel")
@ViewScoped
public class TipoDescuentoModel extends AbstractModel<TipoDescuento> implements Serializable {

    @Inject
    private TipoDescuentoDAO dao;

    public TipoDescuentoModel() {
        setNombreBean("Tipo de Descuento");
    }

    @Override
    public TipoDescuentoDAO getDao() {
        return dao;
    }

    @Override
    public TipoDescuento createRegistrer() {
        TipoDescuento r = new TipoDescuento();
        r.setIdTipoDescuento(UUID.randomUUID());
        r.setActivo(true);
        return r;
    }

    @Override
    public Object getRegistrerById(Object id) {
        if (id == null) return null;
        return dao.findById(id);
    }

    @Override
    public Object getIdByRegistrer(TipoDescuento registro) {
        if (registro == null) return null;
        return registro.getIdTipoDescuento();
    }
}