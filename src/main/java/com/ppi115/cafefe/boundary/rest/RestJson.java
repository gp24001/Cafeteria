package com.ppi115.cafefe.boundary.rest;

import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/** Serializa cada registro con referencias por UUID, sin recorrer las entidades relacionadas. */
public final class RestJson {
    private RestJson() {
    }

    public static String serializar(Object valor) {
        try (var json = JsonbBuilder.create(new JsonbConfig().withFormatting(true))) {
            return json.toJson(datos(valor));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo preparar el JSON", ex);
        }
    }

    private static Object datos(Object valor) throws ReflectiveOperationException {
        if (valor instanceof Collection<?> registros) {
            var resultado = new ArrayList<>();
            for (Object registro : registros) resultado.add(datos(registro));
            return resultado;
        }
        if (valor == null) return null;
        Class<?> tipo = valor.getClass();
        // Las entidades usan este orden para declarar las propiedades públicas de su JSON.
        while (tipo.getAnnotation(JsonbPropertyOrder.class) == null && tipo.getSuperclass() != null) {
            tipo = tipo.getSuperclass();
        }
        var orden = tipo.getAnnotation(JsonbPropertyOrder.class);
        if (orden == null) return valor;
        Map<String, Object> resultado = new LinkedHashMap<>();
        for (String nombre : orden.value()) {
            Field campo = tipo.getDeclaredField(nombre);
            String getter = "get" + Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);
            Object propiedad = tipo.getMethod(getter).invoke(valor);
            if (propiedad == null) continue;
            if (campo.isAnnotationPresent(ManyToOne.class)) {
                resultado.put(nombre, referencia(propiedad, campo.getType()));
            } else if (propiedad instanceof Date fecha) {
                resultado.put(nombre, java.time.Instant.ofEpochMilli(fecha.getTime()).toString());
            } else {
                resultado.put(nombre, propiedad);
            }
        }
        return resultado;
    }

    private static Map<String, Object> referencia(Object entidad, Class<?> tipo) throws ReflectiveOperationException {
        for (Field campo : tipo.getDeclaredFields()) {
            if (campo.isAnnotationPresent(Id.class)) {
                Object id = tipo.getMethod("get" + Character.toUpperCase(campo.getName().charAt(0))
                        + campo.getName().substring(1)).invoke(entidad);
                if (id == null) throw new IllegalArgumentException("La referencia debe tener un identificador");
                return Map.of(campo.getName(), id);
            }
        }
        throw new IllegalArgumentException("La referencia debe tener un identificador");
    }
}
