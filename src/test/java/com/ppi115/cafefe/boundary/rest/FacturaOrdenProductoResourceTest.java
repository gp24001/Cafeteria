package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.FacturaOrdenProductoDAO;
import com.ppi115.cafefe.entity.FacturaOrdenProducto;
import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import com.ppi115.cafefe.entity.Factura;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Pruebas unitarias de FacturaOrdenProductoResource con su DAO simulado mediante Mockito. */
class FacturaOrdenProductoResourceTest extends AbstractResourceTest<FacturaOrdenProducto> {
    @Mock private FacturaOrdenProductoDAO facturaOrdenProductoDAO;
    @InjectMocks private FacturaOrdenProductoResource recurso;

    FacturaOrdenProductoResourceTest() {
        super(FacturaOrdenProducto.class, "factura/{id_factura}/ordenProducto");
    }

    @BeforeEach
    void configurarRuta() {
        recurso.idFactura = idPadre.toString();
        lenient().when(facturaOrdenProductoDAO.existe(Factura.class, idPadre)).thenReturn(true);
    }

    @Override
    protected FacturaOrdenProducto entidad(UUID id) throws Exception {
        var registro = super.entidad(id);
        registro.setIdFactura(new Factura(idPadre));
        return registro;
    }

    @Order(1)
    @Test void consultarPagina() throws Exception {
        var registros = List.of(entidad(idEjemplo("FacturaOrdenProducto")));
        when(facturaOrdenProductoDAO.findRangePorRelacion("idFactura", idPadre, 0, 5)).thenReturn(registros);
        when(facturaOrdenProductoDAO.contarPorRelacion("idFactura", idPadre)).thenReturn(1);
        try (Response respuesta = recurso.findRange(0, 5)) {
            imprimirRespuesta("Listado paginado", "GET", rutaConcreta() + "?min=0&max=5", respuesta);
            assertEquals(200, respuesta.getStatus());
            assertEquals("1", respuesta.getHeaderString("Total-registros"));
            comprobarContenido(respuesta, registros);
        }
    }

    @Order(2)
    @Test void consultarPorId() throws Exception {
        UUID id = idEjemplo("FacturaOrdenProducto");
        var registro = entidad(id);
        when(facturaOrdenProductoDAO.findById(id)).thenReturn(registro);
        try (Response respuesta = recurso.findById(id.toString())) {
            imprimirRespuesta("Consulta por ID", "GET", rutaConcreta() + "/" + id, respuesta);
            assertEquals(200, respuesta.getStatus());
            comprobarContenido(respuesta, registro);
        }
    }

    @Order(6)
    @Test void consultarRegistroInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        try (Response respuesta = recurso.findById(id.toString())) {
            imprimirRespuesta("Registro inexistente", "GET", rutaConcreta() + "/" + id, respuesta);
            comprobarError(respuesta, 404);
        }
        verify(facturaOrdenProductoDAO).findById(id);
    }

    @Test void rechazarAsociacionAjena() throws Exception {
        UUID id = UUID.randomUUID();
        var ajeno = entidad(id);
        ajeno.setIdFactura(new Factura(UUID.randomUUID()));
        when(facturaOrdenProductoDAO.findById(id)).thenReturn(ajeno);
        try (Response respuesta = recurso.findById(id.toString())) {
            comprobarError(respuesta, 404);
        }
    }

    @Test void consultarPaginaDeUnPadreInexistenteDevuelve404() {
        when(facturaOrdenProductoDAO.existe(Factura.class, idPadre)).thenReturn(false);
        try (Response respuesta = recurso.findRange(0, 5)) {
            comprobarError(respuesta, 404);
        }
        verify(facturaOrdenProductoDAO, never()).findRangePorRelacion(anyString(), any(), anyInt(), anyInt());
        verify(facturaOrdenProductoDAO, never()).contarPorRelacion(anyString(), any());
    }
}
