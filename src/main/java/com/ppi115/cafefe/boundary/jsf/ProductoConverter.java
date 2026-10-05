package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.ProductoDAO;
import com.ppi115.cafefe.entity.Producto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;

@Named("productoConverter")
@ApplicationScoped
public class ProductoConverter implements Converter<Producto> {

    @Inject
    private ProductoDAO dao;

    @Override
    public Producto getAsObject(FacesContext ctx, UIComponent c, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return dao.findById(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new ConverterException("Producto inválido");
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent c, Producto value) {
        return value == null || value.getIdProducto() == null
                ? "" : value.getIdProducto().toString();
    }
}