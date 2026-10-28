package com.ppi115.cafefe.boundary.jsf;

import jakarta.inject.Inject;

import com.ppi115.cafefe.control.TipoCaracteristicaDAO;
import com.ppi115.cafefe.entity.TipoCaracteristica;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;

/** Estado y acciones JSF de formato de características. Coordina la vista y delega el acceso a datos en los DAO. */
@Named("tipoCaracteristicaModel")
@ViewScoped
public class TipoCaracteristicaModel extends AbstractModel<TipoCaracteristica> implements Serializable {

    @Inject
    private TipoCaracteristicaDAO dao;

    /**
     * Inicializa TipoCaracteristicaModel con los datos recibidos.
     */
    public TipoCaracteristicaModel() {
        setNombreBean("Tipo de Característica");
    }

    /**
     * Proporciona el DAO utilizado para leer y guardar los registros de esta vista.
     * @return dao actual
     */
    @Override
    public TipoCaracteristicaDAO getDao() {
        return dao;
    }

    /**
     * Construye un registro nuevo con su UUID y los valores iniciales de esta vista.
     * @return registro nuevo con su identificador y valores iniciales
     */
    @Override
    public TipoCaracteristica createRegistrer() {
        TipoCaracteristica r = new TipoCaracteristica();
        r.setIdTipoCaracteristica(UUID.randomUUID());
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
    public Object getIdByRegistrer(TipoCaracteristica registro) {
        if (registro == null) return null;
        return registro.getIdTipoCaracteristica();
    }


    /** Valida el formato en la vista antes de delegar el guardado al DAO. */
    @Override
    public void guardar() {
        if (getRegistro() == null) {
            informarError(new IllegalArgumentException("Debe crear o seleccionar un tipo de característica"));
            return;
        }
        try {
            String expresion = getRegistro().getExpresionRegular();
            if (expresion != null && !expresion.isBlank()) java.util.regex.Pattern.compile(expresion);
            super.guardar();
        } catch (java.util.regex.PatternSyntaxException e) {
            informarError(new IllegalArgumentException("La expresión regular no es válida", e));
        }
    }
}
