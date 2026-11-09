package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.ProductoCaracteristicaDAO;
import com.ppi115.cafefe.entity.ProductoCaracteristica;
import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import com.ppi115.cafefe.entity.Producto;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Pruebas unitarias de ProductoCaracteristicaResource con su DAO simulado mediante Mockito. */
class ProductoCaracteristicaResourceTest extends AbstractResourceTest<ProductoCaracteristica> {
    @Mock private ProductoCaracteristicaDAO productoCaracteristicaDAO;
    @InjectMocks private ProductoCaracteristicaResource recurso;

    ProductoCaracteristicaResourceTest() {
        super(ProductoCaracteristica.class, "producto/{id_producto}/caracteristica");
    }

    @BeforeEach
    void configurarRuta() {
        recurso.idProducto = idPadre.toString();
        lenient().when(productoCaracteristicaDAO.existe(Producto.class, idPadre)).thenReturn(true);
    }

    @Override
    protected ProductoCaracteristica entidad(UUID id) throws Exception {
        var registro = super.entidad(id);
        registro.setIdProducto(new Producto(idPadre));
        return registro;
    }

    @Test void cicloDelRegistro() throws Exception {
        var almacen = simularAlmacen(productoCaracteristicaDAO);
        when(productoCaracteristicaDAO.findRangePorRelacion("idProducto", idPadre, 0, 5)).thenAnswer(llamada -> List.copyOf(almacen.values()));
        when(productoCaracteristicaDAO.contarPorRelacion("idProducto", idPadre)).thenAnswer(llamada -> almacen.size());
        prepararUbicacion();
        var nuevo = entidad(null);
        String solicitud = jsonDe(nuevo);
        UUID id;
        try (Response respuesta = recurso.crear(nuevo)) {
            imprimirRespuesta("Crear registro", "POST", rutaConcreta(), solicitud, respuesta);
            var creado = (ProductoCaracteristica) leerJson(respuesta, ProductoCaracteristica.class);
            id = creado.getIdProductoCaracteristica();
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
        editado.setValor("350");
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
        verify(productoCaracteristicaDAO, never()).crear(any());
    }

    @Test void rechazarAsociacionAjena() throws Exception {
        UUID id = UUID.randomUUID();
        var ajeno = entidad(id);
        ajeno.setIdProducto(new Producto(UUID.randomUUID()));
        when(productoCaracteristicaDAO.findById(id)).thenReturn(ajeno);
        try (Response respuesta = recurso.findById(id.toString())) {
            comprobarError(respuesta, 404);
        }
    }

    @Test void consultarPaginaDeUnPadreInexistenteDevuelve404() {
        when(productoCaracteristicaDAO.existe(Producto.class, idPadre)).thenReturn(false);
        try (Response respuesta = recurso.findRange(0, 5)) {
            comprobarError(respuesta, 404);
        }
        verify(productoCaracteristicaDAO, never()).findRangePorRelacion(anyString(), any(), anyInt(), anyInt());
        verify(productoCaracteristicaDAO, never()).contarPorRelacion(anyString(), any());
    }

    @Test void impideBorrarUnaAsociacionDesdeOtroPadre() throws Exception {
        UUID id = UUID.randomUUID();
        var ajeno = entidad(id);
        ajeno.setIdProducto(new Producto(UUID.randomUUID()));
        when(productoCaracteristicaDAO.findById(id)).thenReturn(ajeno);
        try (Response respuesta = recurso.eliminar(id.toString())) {
            comprobarError(respuesta, 404);
        }
        verify(productoCaracteristicaDAO, never()).eliminar(any());
    }
}
