package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.AccesoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class AccesoFilterTest {
    private AccesoFilter filtro(AccesoService servicio) throws Exception {
        AccesoFilter filtro = new AccesoFilter();
        var campo = AccesoFilter.class.getDeclaredField("servicio");
        campo.setAccessible(true); campo.set(filtro, servicio); return filtro;
    }
    private HttpServletRequest peticion(String ruta) {
        var req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn("/cafefe");
        when(req.getRequestURI()).thenReturn("/cafefe/" + ruta + ".jsf");
        return req;
    }
    @Test void sinSesionRedirigeAlLogin() throws Exception {
        var req = peticion("Empleado"); var res = mock(HttpServletResponse.class); var chain = mock(FilterChain.class);
        filtro(mock(AccesoService.class)).doFilter(req, res, chain);
        verify(res).sendRedirect("/cafefe/Login.jsf"); verifyNoInteractions(chain);
    }
    @Test void camareroNoPuedeAbrirCatalogoPorURL() throws Exception {
        var servicio = mock(AccesoService.class);
        var acceso = new AccesoService.Acceso(UUID.randomUUID(), "Prueba", Set.of("camarero"));
        var req = peticion("Empleado"); var sesion = mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(sesion);
        when(sesion.getAttribute(AccesoModel.CLAVE_SESION)).thenReturn(acceso);
        when(servicio.consultar(acceso.idEmpleado())).thenReturn(acceso);
        var res = mock(HttpServletResponse.class); var chain = mock(FilterChain.class);
        filtro(servicio).doFilter(req, res, chain);
        verify(res).sendError(403); verifyNoInteractions(chain);
    }
    @Test void administradorPuedeAbrirCatalogo() throws Exception {
        var servicio = mock(AccesoService.class);
        var acceso = new AccesoService.Acceso(UUID.randomUUID(), "Prueba", Set.of("administrador"));
        var req = peticion("Empleado"); var sesion = mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(sesion);
        when(sesion.getAttribute(AccesoModel.CLAVE_SESION)).thenReturn(acceso);
        when(servicio.consultar(acceso.idEmpleado())).thenReturn(acceso);
        var res = mock(HttpServletResponse.class); var chain = mock(FilterChain.class);
        filtro(servicio).doFilter(req, res, chain);
        verify(chain).doFilter(req, res);
    }
    @Test void revalidaRolesAntesDeCadaPeticion() throws Exception {
        var servicio = mock(AccesoService.class);
        var acceso = new AccesoService.Acceso(UUID.randomUUID(), "Prueba", Set.of("administrador"));
        var req = peticion("Empleado"); var sesion = mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(sesion);
        when(sesion.getAttribute(AccesoModel.CLAVE_SESION)).thenReturn(acceso);
        when(servicio.consultar(acceso.idEmpleado())).thenThrow(new IllegalArgumentException("Inactivo"));
        var res = mock(HttpServletResponse.class); var chain = mock(FilterChain.class);
        filtro(servicio).doFilter(req, res, chain);
        verify(sesion).invalidate(); verify(res).sendRedirect("/cafefe/Login.jsf"); verifyNoInteractions(chain);
    }
}
