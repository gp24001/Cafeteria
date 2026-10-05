package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.EmpleadoRolDAO;
import com.ppi115.cafefe.entity.EmpleadoRol;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;

@Named("empleadoRolConverter")
@ApplicationScoped
public class EmpleadoRolConverter implements Converter<EmpleadoRol> {

    @Inject
    private EmpleadoRolDAO dao;

    @Override
    public EmpleadoRol getAsObject(FacesContext ctx, UIComponent c, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return dao.findById(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new ConverterException("Empleado inválido");
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent c, EmpleadoRol value) {
        return value == null || value.getIdEmpleadoRol() == null
                ? "" : value.getIdEmpleadoRol().toString();
    }
}