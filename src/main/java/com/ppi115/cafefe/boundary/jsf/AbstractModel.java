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
/** CRUD y paginación originales de las vistas; comparte la validación y los mensajes de error. */

public abstract class AbstractModel<T> implements Serializable {

    @jakarta.inject.Inject
    private jakarta.validation.Validator validator;

    /**
     * Proporciona el validador de restricciones utilizado antes de guardar.
     * @return validator actual
     */
    protected jakarta.validation.Validator getValidator() { return validator; }

    /**
     * Informa de una validación fallida o de un error al acceder a los datos.
     * @param error causa conservada para el registro de diagnóstico
     */
    protected void informarError(RuntimeException error) {
        boolean validacion = error instanceof IllegalArgumentException;
        if (!validacion) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, "Error al procesar la vista", error);
        }
        FacesContext contexto = getFacesContext();
        if (contexto != null) {
            contexto.addMessage(null, new FacesMessage(validacion ? FacesMessage.SEVERITY_WARN : FacesMessage.SEVERITY_ERROR,
                    validacion ? error.getMessage() : "No se pudo completar la operación. Intente nuevamente", null));
        }
    }

    /**
     * Protege las consultas de las vistas sin alterar el CRUD original.
     * @param <R> tipo devuelto por la consulta
     * @param consulta lectura que se delega al DAO
     * @param alternativa valor mostrado si la lectura falla
     * @return resultado de la consulta o valor alternativo después de informar del error
     */
    protected <R> R consultar(java.util.function.Supplier<R> consulta, R alternativa) {
        try {
            return consulta.get();
        } catch (RuntimeException e) {
            informarError(e);
            return alternativa;
        }
    }

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

    /**
     * Proporciona el DAO utilizado para leer y guardar los registros de esta vista.
     * @return dao actual
     */
    public abstract DefaultDAO<T> getDao();

    /**
     * Recupera el registro solicitado a través del DAO de la vista.
     * @param id identificador del registro solicitado
     * @return registro encontrado, o null si no existe
     */
    public abstract Object getRegistrerById(Object id);

    /**
     * Obtiene el identificador usado por la selección y las tablas de JSF.
     * @param object object utilizado por la operación
     * @return identificador del registro, o null si no hay registro
     */
    public abstract Object getIdByRegistrer(T object);

    /**
     * Construye un registro nuevo con su UUID y los valores iniciales de esta vista.
     * @return registro nuevo con su identificador y valores iniciales
     */
    public abstract T createRegistrer();

    // ========================================
    // INICIALIZACIÓN
    // ========================================

    @PostConstruct public void init() {
        model = new LazyDataModel<T>() {
            /**
             * Cuenta las filas que PrimeFaces debe paginar según los filtros actuales.
             * @param filterBy filtros activos de la tabla
             * @return resultado de la consulta u operación
             */
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return consultar(() -> getDao().contar(filterBy), 0);
            }
            /**
             * Carga la página de filas solicitada por la tabla de PrimeFaces.
             * @param first posición inicial de la página, comenzando en cero
             * @param max cantidad máxima de registros que se devolverán
             * @param sortBy orden solicitado por la tabla
             * @param filterBy filtros activos de la tabla
             * @return resultado de la consulta u operación
             */
            @Override
            public List<T> load(int first,int max,Map<String, SortMeta> sortBy,Map<String, FilterMeta> filterBy) {
                primero = first;
                maximo = max;
                totalRegistros = consultar(() -> getDao().contar(), 0);
                setRowCount(totalRegistros);
                return consultar(() -> getDao().findRange(first, max, filterBy), java.util.List.of());
            }
            /**
             * Convierte el identificador del registro en la clave de fila de PrimeFaces.
             * @param object object utilizado por la operación
             * @return row key actual
             */
            @Override
            public String getRowKey(T object) {
                return AbstractModel.this.getRowKey(object);
            }
            /**
             * Recupera el registro correspondiente a una clave de fila de PrimeFaces.
             * @param rowKey clave textual de la fila
             * @return row data actual
             */
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

    /**
     * Prepara el formulario para introducir un registro nuevo.
     */
    public void nuevo() {
        registro = createRegistrer();
        estado = ESTADO_CRUD.NUEVO;
    }

    /**
     * Valida el formulario y delega la escritura en la capa de persistencia.
     */
    public void guardar() {
        if (registro == null) {
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"El registro no puede ser nulo",null));
            return;
        }
        if (estado != ESTADO_CRUD.NUEVO && estado != ESTADO_CRUD.EDITAR) {
            informarError(new IllegalArgumentException("Debe crear o seleccionar un registro antes de guardar"));
            return;
        }
        try {
            var errores = getValidator().validate(registro);
            if (!errores.isEmpty()) {
                errores.forEach(e -> getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage(), null)));
                return;
            }
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

    /**
     * Carga el registro indicado y activa el modo de edición.
     * @param object object utilizado por la operación
     */
    public void editar(T object) {
        if (object == null) {
            getFacesContext().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"Debe seleccionar un registro para editar",null ));
            return;
        }
        registro = object;
        seleccionado = object;
        estado = ESTADO_CRUD.EDITAR;
    }

    /**
     * Elimina el registro indicado y comunica los errores al nivel que invoca la operación.
     * @param object object utilizado por la operación

     */
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

    /**
     * Descarta la edición actual y restablece el estado del formulario correspondiente.
     */
    public void cancelar() {
        registro = createRegistrer();
        estado = ESTADO_CRUD.NINGUNO;
        seleccionado = null;
    }

    // ========================================
    // SELECCIÓN
    // ========================================

    /**
     * Carga la fila elegida para editarla sin cambiar su identificador.
     * @param event evento generado por la selección o acción de la vista
     */
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
    /**
     * Atiende la acción nuevo del componente de botones.
     * @param event evento generado por la selección o acción de la vista
     */
    public void btnNuevo(ActionEvent event) {
        nuevo();
    }

    /**
     * Atiende la acción guardar del componente de botones.
     * @param event evento generado por la selección o acción de la vista
     */
    public void btnGuardar(ActionEvent event) {
        guardar();
    }

    /**
     * Atiende la acción eliminar del componente de botones.
     * @param event evento generado por la selección o acción de la vista
     */
    public void btnEliminar(ActionEvent event) {
        eliminar(registro);
    }

    /**
     * Atiende la acción cancelar del componente de botones.
     * @param event evento generado por la selección o acción de la vista
     */
    public void btnCancelar(ActionEvent event) {
        cancelar();
    }

    // ========================================
    // ROW KEY
    // ========================================

    /**
     * Convierte el identificador del registro en la clave de fila de PrimeFaces.
     * @param object object utilizado por la operación
     * @return row key actual
     */
    public String getRowKey(T object) {
        if (object == null) {
            return null;
        }
        Object id =getIdByRegistrer(object);
        return id != null? id.toString(): null;
    }

    /**
     * Valida la clave UUID de la tabla y recupera el registro correspondiente.
     * @param rowKey clave textual de la fila
     * @return row data by key actual
     */
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

    /**
     * Recupera y convierte al tipo de la vista el registro solicitado por ID.
     * @param id identificador del registro solicitado
     * @return resultado de la consulta u operación
     */
    @SuppressWarnings("unchecked")
    public T byIdRegistrer(Object id) {
        if (id == null) {
            return null;
        }
        return consultar(() -> (T)getRegistrerById(id), null);
    }

    /**
     * Obtiene faces context para la vista o la operación actual.
     * @return faces context actual
     */
    private FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    /**
     * Obtiene estado para la vista o la operación actual.
     * @return estado actual
     */
    public ESTADO_CRUD getEstado() {
        return estado;
    }

    /**
     * Actualiza estado con el valor recibido.
     * @param estado estado utilizado por la operación
     */
    public void setEstado(ESTADO_CRUD estado) {
        this.estado = estado;
    }

    /**
     * Obtiene model para la vista o la operación actual.
     * @return model actual
     */
    public LazyDataModel<T> getModel() {
        return model;
    }

    /**
     * Actualiza model con el valor recibido.
     * @param model model utilizado por la operación
     */
    public void setModel(LazyDataModel<T> model) {
        this.model = model;
    }

    /**
     * Obtiene registro para la vista o la operación actual.
     * @return registro actual
     */
    public T getRegistro() {
        return registro;
    }

    /**
     * Actualiza registro con el valor recibido.
     * @param registro registro que se desea consultar o editar
     */
    public void setRegistro(T registro) {
        this.registro = registro;
    }

    /**
     * Obtiene seleccionado para la vista o la operación actual.
     * @return seleccionado actual
     */
    public T getSeleccionado() {
        return seleccionado;
    }

    /**
     * Actualiza seleccionado con el valor recibido.
     * @param seleccionado seleccionado utilizado por la operación
     */
    public void setSeleccionado(T seleccionado) {
        this.seleccionado = seleccionado;
    }

    /**
     * Obtiene nombre bean para la vista o la operación actual.
     * @return nombre bean actual
     */
    public String getNombreBean() {
        return nombreBean;
    }

    /**
     * Actualiza nombre bean con el valor recibido.
     * @param nombreBean nombre bean utilizado por la operación
     */
    public void setNombreBean(String nombreBean) {
        this.nombreBean = nombreBean;
    }

    /**
     * Obtiene primero para la vista o la operación actual.
     * @return primero actual
     */
    public int getPrimero() {
        return primero;
    }

    /**
     * Actualiza primero con el valor recibido.
     * @param primero primero utilizado por la operación
     */
    public void setPrimero(int primero) {
        this.primero = primero;
    }

    /**
     * Obtiene maximo para la vista o la operación actual.
     * @return maximo actual
     */
    public int getMaximo() {
        return maximo;
    }

    /**
     * Actualiza maximo con el valor recibido.
     * @param maximo maximo utilizado por la operación
     */
    public void setMaximo(int maximo) {
        this.maximo = maximo;
    }

    /**
     * Obtiene total registros para la vista o la operación actual.
     * @return total registros actual
     */
    public int getTotalRegistros() {
        return totalRegistros;
    }

    /**
     * Actualiza total registros con el valor recibido.
     * @param totalRegistros total registros utilizado por la operación
     */
    public void setTotalRegistros(int totalRegistros) {
        this.totalRegistros = totalRegistros;
    }
}
