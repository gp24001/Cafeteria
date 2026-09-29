package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.TipoCaracteristicaDAO;
import com.ppi115.cafefe.entity.TipoCaracteristica;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;

@Named("tipoCaracteristicaModel")
@ViewScoped
public class TipoCaracteristicaModel extends AbstractModel<TipoCaracteristica> implements Serializable {

    @Inject
    private TipoCaracteristicaDAO dao;

    @Override
    public TipoCaracteristicaDAO getDao() {
        return dao;
    }

    @Override
    public TipoCaracteristica createRegistrer() {
        TipoCaracteristica r = new TipoCaracteristica();
        r.setIdTipoCaracteristica(UUID.randomUUID());
        r.setActivo(true);
        return r;
    }

    @Override
    public Object getRegistrerById(Object id) {
        if (id == null) {
            return null;
        }
        return dao.findById(id);
    }

    @Override
    public Object getIdByRegistrer(TipoCaracteristica registro) {
        if (registro == null) {
            return null;
        }
        return registro.getIdTipoCaracteristica();
    }

    @Override
    public String getNombreBean() {
        return "Tipo de Característica";
    }
}
