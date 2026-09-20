package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.TipoDescuentoDAO;
import com.ppi115.cafefe.entity.TipoDescuento;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Named("tipoDescuentoModel")
@ViewScoped
public class TipoDescuentoModel extends AbstractModel<TipoDescuento> implements Serializable {

    @Inject
    TipoDescuentoDAO dao;

    @Override
    public Object getRegistrerById(Object id) {
        return dao.findById(id);
    }

    @Override
    public Object getIdByRegistrer(TipoDescuento object) {
        return object.getIdTipoDescuento();
    }

    @Override
    public TipoDescuento createRegistrer() {
        TipoDescuento r = new TipoDescuento();
        r.setIdTipoDescuento(UUID.randomUUID());
        r.setActivo(true);
        r.setDescuentoMaximo(50);
        return r;
    }

    @Override
    public List<TipoDescuento> getList() {
        return list;
    }

    @Override
    public void setList(List<TipoDescuento> list) {
        this.list = list;
    }

    @Override
    public TipoDescuento getRegistro() {
        return registro;
    }

    @Override
    public void setRegistro(TipoDescuento registro) {
        this.registro = registro;
    }

    @Override
    public ESTADO_CRUD getEstado() {
        return estado;
    }

    @Override
    public void setEstado(ESTADO_CRUD estado) {
        this.estado = estado;
    }

    @Override
    public int getPrimero() {
        return primero;
    }

    @Override
    public void setPrimero(int primero) {
        this.primero = primero;
    }

    @Override
    public int getTamanioPagina() {
        return tamanioPagina;
    }

    @Override
    public void setTamanioPagina(int tamanioPagina) {
        this.tamanioPagina = tamanioPagina;
    }

    @Override
    public int getTotalRegistros() {
        return totalRegistros;
    }

    @Override
    public void setTotalRegistros(int totalRegistros) {
        this.totalRegistros = totalRegistros;
    }
}