package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.AccesoService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("accesoModel")
@SessionScoped
public class AccesoModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Inject private AccesoService servicio;
    private String nombre;
    private AccesoService.Acceso acceso;
    public static final String CLAVE_SESION = "cafefe.acceso";

    public String ingresar() {
        FacesContext contexto = FacesContext.getCurrentInstance();
        try {
            AccesoService.Acceso nuevo = servicio.ingresar(nombre);
            contexto.getExternalContext().getSession(true);
            ((jakarta.servlet.http.HttpServletRequest) contexto.getExternalContext().getRequest()).changeSessionId();
            acceso = nuevo;
            contexto.getExternalContext().getSessionMap().put(CLAVE_SESION, acceso);
            nombre = null;
            return "/Inicio?faces-redirect=true";
        } catch (IllegalArgumentException ex) {
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, ex.getMessage(), null));
            return null;
        }
    }
    public String salir() {
        acceso = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/Login?faces-redirect=true";
    }
    private AccesoService.Acceso actual() {
        return (AccesoService.Acceso) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get(CLAVE_SESION);
    }
    public boolean permite(String pagina) { return actual() != null && actual().permite(pagina); }
    public boolean isAutenticado() { return actual() != null; }
    public String getNombreEmpleado() { return actual() == null ? "" : actual().nombre(); }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
