package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.DescuentoDAO;
import com.ppi115.cafefe.control.TipoDescuentoDAO;
import com.ppi115.cafefe.entity.Descuento;
import com.ppi115.cafefe.entity.TipoDescuento;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;

@Named("descuentoModel")
@ViewScoped
public class DescuentoModel extends AbstractModel<Descuento> implements Serializable {

    @Inject
    private DescuentoDAO dao;

    @Inject
    private TipoDescuentoDAO tipoDescuentoDAO;

    private UUID idTipoDescuentoSeleccionado;

    public DescuentoModel() {
        setNombreBean("Descuento");
    }

    @Override
    public DescuentoDAO getDao() {
        return dao;
    }

    @Override
    public Descuento createRegistrer() {
        Descuento r = new Descuento();
        r.setIdDescuento(UUID.randomUUID());
        r.setFechaInicio(new Date());
        idTipoDescuentoSeleccionado = null;
        return r;
    }

    @Override
    public Object getRegistrerById(Object id) {
        if (id == null) return null;
        return dao.findById(id);
    }

    @Override
    public Object getIdByRegistrer(Descuento registro) {
        if (registro == null) return null;
        return registro.getIdDescuento();
    }

    @Override
    public void guardar() {
        if (getRegistro().getFechaInicio() != null
                && getRegistro().getFechaFin() != null
                && getRegistro().getFechaFin().before(getRegistro().getFechaInicio())) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "La fecha fin no puede ser anterior a la fecha de inicio",
                    null));
            return;
        }

        if (getEstado() == ESTADO_CRUD.NUEVO) {
            if (idTipoDescuentoSeleccionado != null) {
                TipoDescuento tipo = tipoDescuentoDAO.findById(idTipoDescuentoSeleccionado);

                if (tipo != null && !Boolean.TRUE.equals(tipo.getActivo())) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "No se puede asignar un tipo de descuento inactivo",
                            null));
                    return;
                }

                getRegistro().setIdTipoDescuento(tipo);
            } else {
                getRegistro().setIdTipoDescuento(null);
            }
        }

        super.guardar();
    }

    @Override
    public void editar(Descuento registro) {
        super.editar(registro);
        cargarTipo(registro);
    }

    @Override
    public void seleccionar(SelectEvent<Descuento> event) {
        super.seleccionar(event);

        if (event != null) {
            cargarTipo(event.getObject());
        }
    }

    private void cargarTipo(Descuento registro) {
        if (registro != null && registro.getIdTipoDescuento() != null) {
            idTipoDescuentoSeleccionado =
                registro.getIdTipoDescuento().getIdTipoDescuento();
        } else {
            idTipoDescuentoSeleccionado = null;
        }
    }

    public List<TipoDescuento> getTiposDescuento() {
        return tipoDescuentoDAO.findAll();
    }

    public UUID getIdTipoDescuentoSeleccionado() {
        return idTipoDescuentoSeleccionado;
    }

    public void setIdTipoDescuentoSeleccionado(UUID idTipoDescuentoSeleccionado) {
        this.idTipoDescuentoSeleccionado = idTipoDescuentoSeleccionado;
    }
}