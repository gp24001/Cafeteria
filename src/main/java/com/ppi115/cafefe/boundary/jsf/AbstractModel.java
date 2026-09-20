package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.DefaultDAO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.List;

public abstract class AbstractModel<T> implements Serializable {

    @Inject
    DefaultDAO<T> dao;

    @Inject
    FacesContext facescontext;

    ESTADO_CRUD estado = ESTADO_CRUD.NINGUNO;

    List<T> list;

    T registro;

    int primero = 0;
    int tamanioPagina = 10;
    int totalRegistros = 0;

    public abstract Object getRegistrerById(Object id);

    public abstract Object getIdByRegistrer(T object);

    public abstract T createRegistrer();

    @PostConstruct
    public void init() {
        inicializarListas();
        registro = createRegistrer();
        cargarList();
    }

    public void inicializarListas() {
    }

    public void cargarList() {
        list = dao.findAll();
        totalRegistros = dao.contar();
    }

    public void cargarRango() {
        list = dao.findRange(primero, tamanioPagina);
        totalRegistros = dao.contar();
    }

    public void findRange(int first, int max) {
        this.primero = first;
        this.tamanioPagina = max;
        cargarRango();
    }

    public void nuevo() {
        registro = createRegistrer();
        estado = ESTADO_CRUD.NUEVO;
    }

    public void guardar() {
        if (registro == null) {
            facescontext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, "El registro no puede ser nulo", null));
            return;
        }
        try {
            if (estado == ESTADO_CRUD.EDITAR) {
                dao.modificar(registro);
                facescontext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "Se modificó el registro", null));
            } else {
                dao.crear(registro);
                facescontext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "Se registró el registro", null));
            }
            registro = createRegistrer();
            estado = ESTADO_CRUD.NINGUNO;
            cargarList();
        } catch (IllegalArgumentException e) {
            facescontext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar: " + e.getMessage(), null));
        }
    }

    public void editar(T object) {
        if (object == null) {
            facescontext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Debe seleccionar un registro para editar", null));
            return;
        }
        this.registro = object;
        estado = ESTADO_CRUD.EDITAR;
    }

    public void eliminar(T object) {
        if (object == null) {
            facescontext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, "Debe seleccionar un registro para eliminar", null));
            return;
        }
        try {
            Object id = getIdByRegistrer(object);
            if (id == null) {
                facescontext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, "El registro no tiene ID", null));
                return;
            }
            dao.eliminar(id);
            facescontext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "Se eliminó el registro", null));
            cargarList();
        } catch (IllegalArgumentException e) {
            facescontext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar: " + e.getMessage(), null));
        }
    }

    
    
    public void seleccionar(Object id) {
    this.registro = list.stream().filter(r -> getIdByRegistrer(r).equals(id)).findFirst() .orElse(null);
    estado = ESTADO_CRUD.EDITAR;
}
    
    
    
    public void cancelar() {
        registro = createRegistrer();
        estado = ESTADO_CRUD.NINGUNO;
    }

    public void btnNuevo(ActionEvent event) {
        nuevo();
    }

    public void btnGuardar(ActionEvent event) {
        guardar();
    }

    public void btnCancelar(ActionEvent event) {
        cancelar();
    }

    public Object getRowKey(T object) {
        return getIdByRegistrer(object);
    }

    public T byIdRegistrer(Object id) {
        return (T) getRegistrerById(id);
    }

    
    
    // Getters y Setters

    public DefaultDAO<T> getDao() {
        return dao;
    }

    public void setDao(DefaultDAO<T> dao) {
        this.dao = dao;
    }

    public FacesContext getFacescontext() {
        return facescontext;
    }

    public void setFacescontext(FacesContext facescontext) {
        this.facescontext = facescontext;
    }

    public ESTADO_CRUD getEstado() {
        return estado;
    }

    public void setEstado(ESTADO_CRUD estado) {
        this.estado = estado;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public T getRegistro() {
        return registro;
    }

    public void setRegistro(T registro) {
        this.registro = registro;
    }

    public int getPrimero() {
        return primero;
    }

    public void setPrimero(int primero) {
        this.primero = primero;
    }

    public int getTamanioPagina() {
        return tamanioPagina;
    }

    public void setTamanioPagina(int tamanioPagina) {
        this.tamanioPagina = tamanioPagina;
    }

    public int getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(int totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    
    
    
}