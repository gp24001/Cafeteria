package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.CajaDAO;
import com.ppi115.cafefe.entity.Caja;
import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Pruebas unitarias de CajaResource con su DAO simulado mediante Mockito. */
class CajaResourceTest extends AbstractResourceTest<Caja> {
    @Mock private CajaDAO cajaDAO;
    @InjectMocks private CajaResource recurso;

    CajaResourceTest() {
        super(Caja.class, "caja");
    }

    @Test void cicloDelRegistro() throws Exception {
        var almacen = simularAlmacen(cajaDAO);
        when(cajaDAO.findRange(0, 5)).thenAnswer(llamada -> List.copyOf(almacen.values()));
        when(cajaDAO.contar()).thenAnswer(llamada -> almacen.size());
        prepararUbicacion();
        var nuevo = entidad(null);
        String solicitud = jsonDe(nuevo);
        UUID id;
        try (Response respuesta = recurso.crear(nuevo)) {
            imprimirRespuesta("Crear registro", "POST", rutaConcreta(), solicitud, respuesta);
            var creado = (Caja) leerJson(respuesta, Caja.class);
            id = creado.getIdCaja();
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
        editado.setObservaciones("Actualizado en el turno de tarde");
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
        verify(cajaDAO, never()).crear(any());
    }

    @Test void rechazarRangoInvalido() {
        try (Response respuesta = recurso.findRange(-1, 5)) {
            assertEquals(422, respuesta.getStatus());
            assertEquals("min,max", respuesta.getHeaderString("Wrong-Param"));
        }
        verifyNoInteractions(cajaDAO);
    }

    @Test void laEdicionRespetaElIdDeLaRutaAunqueElCuerpoIndiqueOtro() throws Exception {
        comprobarIdDeRutaEnEdicion(cajaDAO, recurso::actualizar);
    }

    @Test void editarUnRegistroInexistenteDevuelve404SinGuardar() throws Exception {
        comprobarEdicionInexistente(cajaDAO, recurso::actualizar);
    }

    @Test void informaAlClienteCuandoLasRelacionesImpidenBorrar() throws Exception {
        comprobarBorradoConRelaciones(cajaDAO, recurso::eliminar);
    }
}
