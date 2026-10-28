
package com.ppi115.cafefe.control;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

/** Conversión entre los identificadores mostrados por JSF y los valores persistidos. */
@Converter
@jakarta.faces.convert.FacesConverter(value = "uuidConverter", forClass = UUID.class)
public class UUIDConverter implements AttributeConverter<UUID, UUID>, jakarta.faces.convert.Converter<UUID> {

    /**
     * Convierte la opción enviada por los selectores JSF.
     * @param context contexto de la vista
     * @param component selector que envía el valor
     * @param value UUID en formato de texto
     * @return identificador válido o null si no se seleccionó una opción
     */
    @Override
    public UUID getAsObject(jakarta.faces.context.FacesContext context,
            jakarta.faces.component.UIComponent component, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new jakarta.faces.convert.ConverterException(new jakarta.faces.application.FacesMessage(
                    jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "El identificador seleccionado no es válido", null), e);
        }
    }

    /**
     * Representa un UUID como valor de una opción JSF.
     * @param context contexto de la vista
     * @param component selector que presenta el valor
     * @param value identificador de la opción
     * @return UUID textual o cadena vacía para una opción sin selección
     */
    @Override
    public String getAsString(jakarta.faces.context.FacesContext context,
            jakarta.faces.component.UIComponent component, UUID value) {
        return value == null ? "" : value.toString();
    }
    
    /**
     * Conserva el UUID al enviarlo al tipo UUID de PostgreSQL.
     * @param attribute attribute utilizado por la operación
     * @return resultado de la consulta u operación
     */
    @Override
    public UUID convertToDatabaseColumn(UUID attribute) { 
        return attribute; 
    } 
    
    /**
     * Conserva el UUID al recuperar el valor de PostgreSQL.
     * @param dbData db data utilizado por la operación
     * @return resultado de la consulta u operación
     */
    @Override
    public UUID convertToEntityAttribute(UUID dbData) {
        return dbData; }
    
}
