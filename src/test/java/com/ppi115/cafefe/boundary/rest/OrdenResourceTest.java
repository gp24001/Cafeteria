package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.OrdenDAO;
import com.ppi115.cafefe.entity.Orden;
import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Pruebas unitarias de OrdenResource con su DAO simulado mediante Mockito. */
class OrdenResourceTest extends AbstractResourceTest<Orden> {
    @Mock private OrdenDAO ordenDAO;
    @InjectMocks private OrdenResource recurso;

    OrdenResourceTest() {
        super(Orden.class, "orden");
    }

    @Test void cicloDelRegistro() throws Exception {
        var almacen = simularAlmacen(ordenDAO);
        when(ordenDAO.findRange(0, 5)).thenAnswer(llamada -> List.copyOf(almacen.values()));
        when(ordenDAO.contar()).thenAnswer(llamada -> almacen.size());
        prepararUbicacion();
        var nuevo = entidad(null);
        String solicitud = jsonDe(nuevo);
        UUID id;
        try (Response respuesta = recurso.crear(nuevo)) {
            imprimirRespuesta("Crear registro", "POST", rutaConcreta(), solicitud, respuesta);
            var creado = (Orden) leerJson(respuesta, Orden.class);
            id = creado.getIdOrden();
            assertNotNull(id);
            comprobarCreacion(respuesta, id);
            comprobarContenido(respuesta, nuevo);
        }

        String destino = rutaConcreta() + "/" + id;
        try (Response respuesta = recurso.findRange(0, 5)) {
            imprimirRespuesta("Listado tras crear", "GET", rutaConcreta() + "?min=0&max=5", respuesta);
            assertEquals(200, respuesta.getStatus());
            assertEquals("1", respuesta.getHeaderString("Total-registros"));
            comprobarContenido(respuesta, List.of(nuevo));
        }
        try (Response respuesta = recurso.findById(id.toString())) {
            imprimirRespuesta("Consultar el registro creado", "GET", destino, respuesta);
            assertEquals(200, respuesta.getStatus());
            comprobarContenido(respuesta, nuevo);
        }

        var editado = entidad(id);
        editado.setObservacion("Actualizado en el turno de tarde");
        solicitud = jsonDe(editado);
        try (Response respuesta = recurso.actualizar(id.toString(), editado)) {
            imprimirRespuesta("Actualizar registro", "PUT", destino, solicitud, respuesta);
            assertEquals(200, respuesta.getStatus());
            comprobarContenido(respuesta, editado);
        }
        try (Response respuesta = recurso.findById(id.toString())) {
            imprimirRespuesta("Verificar el cambio", "GET", destino, respuesta);
            assertEquals(200, respuesta.getStatus());
            comprobarContenido(respuesta, editado);
        }
        try (Response respuesta = recurso.eliminar(id.toString())) {
            imprimirRespuesta("Eliminar registro", "DELETE", destino, respuesta);
            assertEquals(204, respuesta.getStatus());
            assertFalse(respuesta.hasEntity());
        }
        try (Response respuesta = recurso.findById(id.toString())) {
            imprimirRespuesta("Verificar la eliminación", "GET", destino, respuesta);
            comprobarError(respuesta, 404);
        }
    }

    @Test void rechazarCreacionSinDatos() {
        try (Response respuesta = recurso.crear(null)) {
            comprobarError(respuesta, 422);
        }
        verify(ordenDAO, never()).crear(any());
    }

    @Test void laEdicionRespetaElIdDeLaRutaAunqueElCuerpoIndiqueOtro() throws Exception {
        comprobarIdDeRutaEnEdicion(ordenDAO, recurso::actualizar);
    }

    @Test void editarUnRegistroInexistenteDevuelve404SinGuardar() throws Exception {
        comprobarEdicionInexistente(ordenDAO, recurso::actualizar);
    }

    @Test void informaAlClienteCuandoLasRelacionesImpidenBorrar() throws Exception {
        comprobarBorradoConRelaciones(ordenDAO, recurso::eliminar);
    }
}
