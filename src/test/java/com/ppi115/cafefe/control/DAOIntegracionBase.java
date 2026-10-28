package com.ppi115.cafefe.control;

import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.primefaces.model.FilterMeta;
import static org.junit.jupiter.api.Assertions.*;

/** Casos comunes ejecutados por cada clase DAOIntegracionTest, sin simular la persistencia. */
public abstract class DAOIntegracionBase<T> extends IntegracionDAOBase {
    private final Class<T> entidad;
    private final Supplier<? extends DefaultDAO<T>> fabrica;

    /**
     * Inicializa DAOIntegracionBase con los datos recibidos.
     * @param entidad entidad utilizado por la operación
     * @param fabrica fabrica utilizado por la operación

     */
    protected DAOIntegracionBase(Class<T> entidad, Supplier<? extends DefaultDAO<T>> fabrica) {
        this.entidad = entidad;
        this.fabrica = fabrica;
    }

    /**
     * Obtiene el identificador JPA de un registro de prueba.
     * @param registro registro que se desea consultar o editar
     * @return resultado de la consulta u operación
     */
    private Object id(T registro) {
        return em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(registro);
    }

    /**
     * Elige un campo de texto que permita comprobar una modificación persistida.
     * @return resultado de la consulta u operación
     */
    private Field campoEditable() {
        for (String nombre : new String[]{"observaciones", "comentarios", "observacion", "valor", "nombre"}) {
            try {
                Field campo = entidad.getDeclaredField(nombre);
                if (campo.getType() == String.class) {
                    campo.setAccessible(true);
                    return campo;
                }
            } catch (NoSuchFieldException ignorada) {
                // Algunas entidades usan observacion y otras observaciones.
            }
        }
        throw new AssertionError("No se definió un campo de edición para " + entidad.getSimpleName());
    }

    /**
     * Verifica que crear consultar modificar eliminar.
     */
    @Test
    void crearConsultarModificarEliminar() throws Exception {
        var dao = dao(fabrica.get());
        T nuevo = registroValido(entidad);
        Object id = id(nuevo);
        dao.crear(nuevo);
        em.flush();
        em.clear();
        T encontrado = dao.findById(id);
        assertNotNull(encontrado);
        Field campo = campoEditable();
        String valor = campo.getName().equals("valor") ? "500" : "Integración modificada";
        campo.set(encontrado, valor);
        dao.modificar(encontrado);
        em.flush();
        em.clear();
        assertEquals(valor, campo.get(dao.findById(id)));
        dao.eliminar(id);
        em.flush();
        em.clear();
        assertNull(dao.findById(id));
    }

    /**
     * Verifica que consultar listar paginar yfiltrar.
     */
    @Test
    void consultarListarPaginarYFiltrar() throws Exception {
        var dao = dao(fabrica.get());
        T nuevo = registroValido(entidad);
        Field campo = campoEditable();
        String marca = campo.getName().equals("valor") ? "250" : "Integración filtro";
        campo.set(nuevo, marca);
        dao.crear(nuevo);
        em.flush();
        assertTrue(dao.findAll().contains(nuevo));
        assertTrue(dao.contar() > 0);
        assertTrue(dao.findRange(0, 2).size() <= 2);
        var filtros = Map.of(campo.getName(), FilterMeta.builder().field(campo.getName()).filterValue(marca).build());
        var filtrados = dao.findRange(0, 1000, filtros);
        assertTrue(filtrados.contains(nuevo));
        assertEquals(filtrados.size(), dao.contar(filtros));
    }

    /**
     * Verifica que rechazar crear omodificar sin datos obligatorios.
     */
    @Test
    void rechazarCrearOModificarSinDatosObligatorios() throws Exception {
        var dao = dao(fabrica.get());
        T vacio = entidad.getDeclaredConstructor().newInstance();
        // Conserva un ID para verificar que se rechazan los datos y no solo el identificador.
        for (Field campo : entidad.getDeclaredFields()) {
            if (campo.isAnnotationPresent(Id.class)) {
                campo.setAccessible(true);
                campo.set(vacio, java.util.UUID.randomUUID());
            }
        }
        assertThrows(IllegalArgumentException.class, () -> dao.crear(vacio));
        assertThrows(IllegalArgumentException.class, () -> dao.modificar(vacio));
        assertNull(dao.findById(id(vacio)));
    }

    /**
     * Verifica que rechazar parametros nulos yrangos invalidos.
     */
    @Test
    void rechazarParametrosNulosYRangosInvalidos() {
        var dao = dao(fabrica.get());
        assertThrows(IllegalArgumentException.class, () -> dao.crear(null));
        assertThrows(IllegalArgumentException.class, () -> dao.modificar(null));
        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(null));
        assertThrows(IllegalArgumentException.class, () -> dao.findById(null));
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(0, 0, Map.of()));
    }

    /**
     * Verifica que rollback no deja registros de prueba.
     */
    @Test
    void rollbackNoDejaRegistrosDePrueba() {
        var dao = dao(fabrica.get());
        T nuevo = registroValido(entidad);
        Object id = id(nuevo);
        dao.crear(nuevo);
        em.flush();
        assertNotNull(dao.findById(id));
        em.getTransaction().rollback();
        em.clear();
        em.getTransaction().begin();
        assertNull(dao.findById(id));
    }
}
