package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.DescuentoDAO;
import com.ppi115.cafefe.entity.Descuento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;

@Named("descuentoConverter")
@ApplicationScoped
public class DescuentoConverter implements Converter<Descuento> {

    @Inject
    private DescuentoDAO dao;

    @Override
    public Descuento getAsObject(FacesContext ctx, UIComponent c, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return dao.findById(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new ConverterException("Descuento inválido");
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent c, Descuento value) {
        return value == null || value.getIdDescuento() == null
                ? "" : value.getIdDescuento().toString();
    }
}