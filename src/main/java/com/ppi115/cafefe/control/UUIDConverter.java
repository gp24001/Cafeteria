
package com.ppi115.cafefe.control;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

@Converter
public class UUIDConverter implements AttributeConverter<UUID, UUID> {
    
    @Override
    public UUID convertToDatabaseColumn(UUID attribute) { 
        return attribute; 
    } 
    
    @Override public UUID convertToEntityAttribute(UUID dbData) {
        return dbData; }
    
}
