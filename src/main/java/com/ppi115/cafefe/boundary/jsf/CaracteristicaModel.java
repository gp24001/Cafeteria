package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.CaracteristicaDAO;
import com.ppi115.cafefe.control.TipoCaracteristicaDAO;
import com.ppi115.cafefe.entity.Caracteristica;
import com.ppi115.cafefe.entity.TipoCaracteristica;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;

@Named("caracteristicaModel")
@ViewScoped
public class CaracteristicaModel extends AbstractModel<Caracteristica> implements Serializable {

    @Inject
    private CaracteristicaDAO dao;

    @Inject
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    private UUID idTipoCaracteristicaSeleccionado;

    public CaracteristicaModel() {
        setNombreBean("Característica");
    }

    @Override
    public CaracteristicaDAO getDao() {
        return dao;
    }

    @Override
    public Caracteristica createRegistrer() {
        Caracteristica r = new Caracteristica();
        r.setIdCaracteristica(UUID.randomUUID());
        r.setActivo(true);
        idTipoCaracteristicaSeleccionado = null;
        return r;
    }

    @Override
    public Object getRegistrerById(Object id) {
        if (id == null) return null;
        return dao.findById(id);
    }

    @Override
    public Object getIdByRegistrer(Caracteristica registro) {
        if (registro == null) return null;
        return registro.getIdCaracteristica();
    }

    @Override
    public void guardar() {
        if (getEstado() == ESTADO_CRUD.NUEVO) {
            if (idTipoCaracteristicaSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Debe seleccionar un tipo de característica",
                        null));
                return;
            }

            TipoCaracteristica tipo =
                tipoCaracteristicaDAO.findById(idTipoCaracteristicaSeleccionado);

            if (tipo == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "El tipo de característica no existe",
                        null));
                return;
            }

            if (!Boolean.TRUE.equals(tipo.getActivo())) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "No se puede asignar un tipo de característica inactivo",
                        null));
                return;
            }

            getRegistro().setIdTipoCaracteristica(tipo);
        }

        super.guardar();
    }

    @Override
    public void editar(Caracteristica registro) {
        super.editar(registro);
        cargarTipo(registro);
    }

    @Override
    public void seleccionar(SelectEvent<Caracteristica> event) {
        super.seleccionar(event);

        if (event != null) {
            cargarTipo(event.getObject());
        }
    }

    private void cargarTipo(Caracteristica registro) {
        if (registro != null && registro.getIdTipoCaracteristica() != null) {
            idTipoCaracteristicaSeleccionado =
                registro.getIdTipoCaracteristica().getIdTipoCaracteristica();
        } else {
            idTipoCaracteristicaSeleccionado = null;
        }
    }

    public List<TipoCaracteristica> getTiposCaracteristica() {
        return tipoCaracteristicaDAO.findAll();
    }

    public UUID getIdTipoCaracteristicaSeleccionado() {
        return idTipoCaracteristicaSeleccionado;
    }

    public void setIdTipoCaracteristicaSeleccionado(UUID idTipoCaracteristicaSeleccionado) {
        this.idTipoCaracteristicaSeleccionado = idTipoCaracteristicaSeleccionado;
    }
}