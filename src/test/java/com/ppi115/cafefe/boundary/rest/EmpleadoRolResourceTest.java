package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.EmpleadoRolDAO;
import com.ppi115.cafefe.entity.EmpleadoRol;
import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import com.ppi115.cafefe.entity.Empleado;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Pruebas unitarias de EmpleadoRolResource con su DAO simulado mediante Mockito. */
class EmpleadoRolResourceTest extends AbstractResourceTest<EmpleadoRol> {
    @Mock private EmpleadoRolDAO empleadoRolDAO;
    @InjectMocks private EmpleadoRolResource recurso;

    EmpleadoRolResourceTest() {
        super(EmpleadoRol.class, "empleado/{id_empleado}/rol");
    }

    @BeforeEach
    void configurarRuta() {
        recurso.idEmpleado = idPadre.toString();
        lenient().when(empleadoRolDAO.existe(Empleado.class, idPadre)).thenReturn(true);
    }

    @Override
    protected EmpleadoRol entidad(UUID id) throws Exception {
        var registro = super.entidad(id);
        registro.setIdEmpleado(new Empleado(idPadre));
        return registro;
    }

    @Test void cicloDelRegistro() throws Exception {
        var almacen = simularAlmacen(empleadoRolDAO);
        when(empleadoRolDAO.findRangePorRelacion("idEmpleado", idPadre, 0, 5)).thenAnswer(llamada -> List.copyOf(almacen.values()));
        when(empleadoRolDAO.contarPorRelacion("idEmpleado", idPadre)).thenAnswer(llamada -> almacen.size());
        prepararUbicacion();
        var nuevo = entidad(null);
        String solicitud = jsonDe(nuevo);
        UUID id;
        try (Response respuesta = recurso.crear(nuevo)) {
            imprimirRespuesta("Crear registro", "POST", rutaConcreta(), solicitud, respuesta);
            var creado = (EmpleadoRol) leerJson(respuesta, EmpleadoRol.class);
            id = creado.getIdEmpleadoRol();
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
        verify(empleadoRolDAO, never()).crear(any());
    }

    @Test void rechazarAsociacionAjena() throws Exception {
        UUID id = UUID.randomUUID();
        var ajeno = entidad(id);
        ajeno.setIdEmpleado(new Empleado(UUID.randomUUID()));
        when(empleadoRolDAO.findById(id)).thenReturn(ajeno);
        try (Response respuesta = recurso.findById(id.toString())) {
            comprobarError(respuesta, 404);
        }
    }

    @Test void consultarPaginaDeUnPadreInexistenteDevuelve404() {
        when(empleadoRolDAO.existe(Empleado.class, idPadre)).thenReturn(false);
        try (Response respuesta = recurso.findRange(0, 5)) {
            comprobarError(respuesta, 404);
        }
        verify(empleadoRolDAO, never()).findRangePorRelacion(anyString(), any(), anyInt(), anyInt());
        verify(empleadoRolDAO, never()).contarPorRelacion(anyString(), any());
    }

    @Test void impideBorrarUnaAsociacionDesdeOtroPadre() throws Exception {
        UUID id = UUID.randomUUID();
        var ajeno = entidad(id);
        ajeno.setIdEmpleado(new Empleado(UUID.randomUUID()));
        when(empleadoRolDAO.findById(id)).thenReturn(ajeno);
        try (Response respuesta = recurso.eliminar(id.toString())) {
            comprobarError(respuesta, 404);
        }
        verify(empleadoRolDAO, never()).eliminar(any());
    }
}
