package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.DefaultDAO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

public abstract class AbstractModel<T> implements Serializable {

    private ESTADO_CRUD estado = ESTADO_CRUD.NINGUNO;

    private LazyDataModel<T> model;

    private T registro;

    private T seleccionado;

    private String nombreBean = "";

    private int primero = 0;

    private int maximo = 10;

    private int totalRegistros = 0;


    // ========================================
    // MÉTODOS QUE IMPLEMENTA CADA MODEL
    // ========================================

    public abstract DefaultDAO<T> getDao();

    public abstract Object getRegistrerById(Object id);

    public abstract Object getIdByRegistrer(T object);

    public abstract T createRegistrer();


    // ========================================
    // INICIALIZACIÓN
    // ========================================

    @PostConstruct public void init() {
        model = new LazyDataModel<T>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return getDao().contar(filterBy);
            }
            @Override
            public List<T> load(int first,int max,Map<String, SortMeta> sortBy,Map<String, FilterMeta> filterBy) {
                primero = first;
                maximo = max;
                totalRegistros = getDao().contar();
                setRowCount(totalRegistros);
                return getDao().findRange(first, max, filterBy);
            }
            @Override
            public String getRowKey(T object) {
                return AbstractModel.this.getRowKey(object);
            }
            @Override
            public T getRowData(String rowKey) {
                return getRowDataByKey(rowKey);
            }
        };
        registro = createRegistrer();
    }


    // ========================================
    // CRUD
    // ========================================

    public void nuevo() {
        registro = createRegistrer();
        estado = ESTADO_CRUD.NUEVO;
    }


    public void guardar() {
        if (registro == null) {
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"El registro no puede ser nulo",null));
            return;
        }
        try {
            if (estado == ESTADO_CRUD.EDITAR) {
                getDao().modificar(registro);
                getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO,"Se modificó el registro",null));
            } else {
                getDao().crear(registro);
                getFacesContext().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,"Se creó el registro",null));
            }
            registro = createRegistrer();
            estado = ESTADO_CRUD.NINGUNO;
            seleccionado = null;
            if (model != null) {
                model.setRowCount(getDao().contar());
            }
        } catch (IllegalArgumentException e) {
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error al guardar: "+ e.getMessage(),null));
        } catch (Exception e) {
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"Ocurrió un error inesperado al guardar",null));
        }
    }


    public void editar(T object) {
        if (object == null) {
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"Debe seleccionar un registro para editar",null ));
            return;
        }
        registro = object;
        seleccionado = object;
        estado = ESTADO_CRUD.EDITAR;
    }

    public void eliminar(T object) {
        if (object == null) {
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"Debe seleccionar un registro para eliminar",null));
            return;
        }
        Object id = getIdByRegistrer(object);
        if (id == null) {
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"El registro no tiene ID",null));
            return;
        }
        try {
            getDao().eliminar(id);
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO,"Se eliminó el registro",null));
            registro = createRegistrer();
            estado = ESTADO_CRUD.NINGUNO;
            seleccionado = null;
            if (model != null) {
                model.setRowCount(getDao().contar());
            }
        } catch (IllegalArgumentException e) {
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error al eliminar: "+ e.getMessage(),null));
        } catch (Exception e) {
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"Ocurrió un error inesperado al eliminar",null));
        }
    }

    public void cancelar() {
        registro = createRegistrer();
        estado = ESTADO_CRUD.NINGUNO;
        seleccionado = null;
    }


    // ========================================
    // SELECCIÓN
    // ========================================

    public void seleccionar(SelectEvent<T> event) {
        if (event == null
                || event.getObject() == null) {
            return;
        }
        seleccionado = event.getObject();
        registro = event.getObject();
        estado = ESTADO_CRUD.EDITAR;
    }


    // ========================================
    // BOTONES
    // ========================================
    public void btnNuevo(ActionEvent event) {
        nuevo();
    }

    public void btnGuardar(ActionEvent event) {
        guardar();
    }

    public void btnEliminar(ActionEvent event) {
        eliminar(registro);
    }

    public void btnCancelar(ActionEvent event) {
        cancelar();
    }

    // ========================================
    // ROW KEY
    // ========================================

    public String getRowKey(T object) {
        if (object == null) {
            return null;
        }
        Object id =getIdByRegistrer(object);
        return id != null? id.toString(): null;
    }


    protected T getRowDataByKey(String rowKey) {
        if (rowKey == null|| rowKey.isBlank()) {
            return null;
        }
        try {
            UUID id = UUID.fromString(rowKey);
            return byIdRegistrer(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public T byIdRegistrer(Object id) {
        if (id == null) {
            return null;
        }
        return (T)getRegistrerById(id);
    }


    private FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }


    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    public ESTADO_CRUD getEstado() {
        return estado;
    }

    public void setEstado(ESTADO_CRUD estado) {
        this.estado = estado;
    }

    public LazyDataModel<T> getModel() {
        return model;
    }

    public void setModel(LazyDataModel<T> model) {
        this.model = model;
    }

    public T getRegistro() {
        return registro;
    }

    public void setRegistro(T registro) {
        this.registro = registro;
    }

    public T getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(T seleccionado) {
        this.seleccionado = seleccionado;
    }

    public String getNombreBean() {
        return nombreBean;
    }

    public void setNombreBean(String nombreBean) {
        this.nombreBean = nombreBean;
    }

    public int getPrimero() {
        return primero;
    }

    public void setPrimero(int primero) {
        this.primero = primero;
    }

    public int getMaximo() {
        return maximo;
    }

    public void setMaximo(int maximo) {
        this.maximo = maximo;
    }

    public int getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(int totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    
}