package com.ppi115.cafefe.boundary.jsf;

import com.ppi115.cafefe.control.AccesoService;
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Aplica a las páginas JSF los mismos permisos que utiliza el menú. */
@WebFilter(urlPatterns = {"*.jsf", "*.xhtml"})
public class AccesoFilter implements Filter {
    @Inject private AccesoService servicio;
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String ruta = req.getRequestURI().substring(req.getContextPath().length());
        if (ruta.startsWith("/jakarta.faces.resource/") || ruta.startsWith("/resources/")) {
            chain.doFilter(request, response); return;
        }
        String pagina = ruta.substring(ruta.lastIndexOf('/') + 1).replaceFirst("\\.(jsf|xhtml)$", "");
        if ("Login".equals(pagina)) { chain.doFilter(request, response); return; }
        HttpSession sesion = req.getSession(false);
        AccesoService.Acceso acceso = sesion == null ? null
                : (AccesoService.Acceso) sesion.getAttribute(AccesoModel.CLAVE_SESION);
        if (acceso != null) {
            try {
                acceso = servicio.consultar(acceso.idEmpleado());
                sesion.setAttribute(AccesoModel.CLAVE_SESION, acceso);
            } catch (IllegalArgumentException ex) {
                sesion.invalidate(); acceso = null;
            }
        }
        if (acceso == null) {
            String destino = req.getContextPath() + "/Login.jsf";
            if ("partial/ajax".equals(req.getHeader("Faces-Request"))) {
                res.setContentType("text/xml;charset=UTF-8");
                res.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<partial-response><redirect url=\"" + destino + "\"/></partial-response>");
            } else res.sendRedirect(destino);
            return;
        }
        res.setHeader("Cache-Control", "no-store");
        if (!acceso.permite(pagina)) { res.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        chain.doFilter(request, response);
    }
}
