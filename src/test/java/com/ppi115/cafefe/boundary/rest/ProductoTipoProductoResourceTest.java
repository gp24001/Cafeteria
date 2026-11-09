package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.ProductoTipoProductoDAO;
import com.ppi115.cafefe.entity.ProductoTipoProducto;
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

/** Pruebas unitarias de ProductoTipoProductoResource con su DAO simulado mediante Mockito. */
class ProductoTipoProductoResourceTest extends AbstractResourceTest<ProductoTipoProducto> {
    @Mock private ProductoTipoProductoDAO productoTipoProductoDAO;
    @InjectMocks private ProductoTipoProductoResource recurso;

    ProductoTipoProductoResourceTest() {
        super(ProductoTipoProducto.class, "producto/{id_producto}/tipoproducto");
    }

    @BeforeEach
    void configurarRuta() {
        recurso.idProducto = idPadre.toString();
        lenient().when(productoTipoProductoDAO.existe(Producto.class, idPadre)).thenReturn(true);
    }

    @Override
    protected ProductoTipoProducto entidad(UUID id) throws Exception {
        var registro = super.entidad(id);
        registro.setIdProducto(new Producto(idPadre));
        return registro;
    }

    @Test void cicloDelRegistro() throws Exception {
        var almacen = simularAlmacen(productoTipoProductoDAO);
        when(productoTipoProductoDAO.findRangePorRelacion("idProducto", idPadre, 0, 5)).thenAnswer(llamada -> List.copyOf(almacen.values()));
        when(productoTipoProductoDAO.contarPorRelacion("idProducto", idPadre)).thenAnswer(llamada -> almacen.size());
        prepararUbicacion();
        var nuevo = entidad(null);
        String solicitud = jsonDe(nuevo);
        UUID id;
        try (Response respuesta = recurso.crear(nuevo)) {
            imprimirRespuesta("Crear registro", "POST", rutaConcreta(), solicitud, respuesta);
            var creado = (ProductoTipoProducto) leerJson(respuesta, ProductoTipoProducto.class);
            id = creado.getIdProductoTipoProducto();
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
        verify(productoTipoProductoDAO, never()).crear(any());
    }

    @Test void rechazarAsociacionAjena() throws Exception {
        UUID id = UUID.randomUUID();
        var ajeno = entidad(id);
        ajeno.setIdProducto(new Producto(UUID.randomUUID()));
        when(productoTipoProductoDAO.findById(id)).thenReturn(ajeno);
        try (Response respuesta = recurso.findById(id.toString())) {
            comprobarError(respuesta, 404);
        }
    }

    @Test void consultarPaginaDeUnPadreInexistenteDevuelve404() {
        when(productoTipoProductoDAO.existe(Producto.class, idPadre)).thenReturn(false);
        try (Response respuesta = recurso.findRange(0, 5)) {
            comprobarError(respuesta, 404);
        }
        verify(productoTipoProductoDAO, never()).findRangePorRelacion(anyString(), any(), anyInt(), anyInt());
        verify(productoTipoProductoDAO, never()).contarPorRelacion(anyString(), any());
    }

    @Test void impideBorrarUnaAsociacionDesdeOtroPadre() throws Exception {
        UUID id = UUID.randomUUID();
        var ajeno = entidad(id);
        ajeno.setIdProducto(new Producto(UUID.randomUUID()));
        when(productoTipoProductoDAO.findById(id)).thenReturn(ajeno);
        try (Response respuesta = recurso.eliminar(id.toString())) {
            comprobarError(respuesta, 404);
        }
        verify(productoTipoProductoDAO, never()).eliminar(any());
    }
}
