package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.control.PagoDetalleDAO;
import com.ppi115.cafefe.entity.PagoDetalle;
import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import com.ppi115.cafefe.entity.Pago;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Pruebas unitarias de PagoDetalleResource con su DAO simulado mediante Mockito. */
class PagoDetalleResourceTest extends AbstractResourceTest<PagoDetalle> {
    @Mock private PagoDetalleDAO pagoDetalleDAO;
    @InjectMocks private PagoDetalleResource recurso;

    PagoDetalleResourceTest() {
        super(PagoDetalle.class, "pago/{id_pago}/detalle");
    }

    @BeforeEach
    void configurarRuta() {
        recurso.idPago = idPadre.toString();
        lenient().when(pagoDetalleDAO.existe(Pago.class, idPadre)).thenReturn(true);
    }

    @Override
    protected PagoDetalle entidad(UUID id) throws Exception {
        var registro = super.entidad(id);
        registro.setIdPago(new Pago(idPadre));
        return registro;
    }

    @Order(1)
    @Test void consultarPagina() throws Exception {
        var registros = List.of(entidad(idEjemplo("PagoDetalle")));
        when(pagoDetalleDAO.findRangePorRelacion("idPago", idPadre, 0, 5)).thenReturn(registros);
        when(pagoDetalleDAO.contarPorRelacion("idPago", idPadre)).thenReturn(1);
        try (Response respuesta = recurso.findRange(0, 5)) {
            imprimirRespuesta("Listado paginado", "GET", rutaConcreta() + "?min=0&max=5", respuesta);
            assertEquals(200, respuesta.getStatus());
            assertEquals("1", respuesta.getHeaderString("Total-registros"));
            comprobarContenido(respuesta, registros);
        }
    }

    @Order(2)
    @Test void consultarPorId() throws Exception {
        UUID id = idEjemplo("PagoDetalle");
        var registro = entidad(id);
        when(pagoDetalleDAO.findById(id)).thenReturn(registro);
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
        verify(pagoDetalleDAO).findById(id);
    }

    @Test void rechazarAsociacionAjena() throws Exception {
        UUID id = UUID.randomUUID();
        var ajeno = entidad(id);
        ajeno.setIdPago(new Pago(UUID.randomUUID()));
        when(pagoDetalleDAO.findById(id)).thenReturn(ajeno);
        try (Response respuesta = recurso.findById(id.toString())) {
            comprobarError(respuesta, 404);
        }
    }

    @Test void consultarPaginaDeUnPadreInexistenteDevuelve404() {
        when(pagoDetalleDAO.existe(Pago.class, idPadre)).thenReturn(false);
        try (Response respuesta = recurso.findRange(0, 5)) {
            comprobarError(respuesta, 404);
        }
        verify(pagoDetalleDAO, never()).findRangePorRelacion(anyString(), any(), anyInt(), anyInt());
        verify(pagoDetalleDAO, never()).contarPorRelacion(anyString(), any());
    }
}
