package com.ppi115.cafefe.boundary.server;

import com.ppi115.cafefe.boundary.TipoDescuentoDAO;
import com.ppi115.cafefe.entity.TipoDescuento;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "TipoDescuentoServer", urlPatterns = {"/tipo_descuento"})
public class TipoDescuentoServer extends HttpServlet {

    @Inject
    private TipoDescuentoDAO tipoDescuentoDAO;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            Map<String, String[]> parametros = request.getParameterMap();
            if (parametros.containsKey("id_tipo_descuento") && parametros.containsKey("nombre") && parametros.containsKey("observaciones") && parametros.containsKey("descuento_maximo")) {
                TipoDescuento tipoDescuento = new TipoDescuento();
                tipoDescuento.setIdTipoDescuento(UUID.fromString(request.getParameter("id_tipo_descuento")));
                tipoDescuento.setNombre(request.getParameter("nombre"));
                tipoDescuento.setObservaciones(request.getParameter("observaciones"));
                tipoDescuento.setDescuentoMaximo(Integer.valueOf(request.getParameter("descuento_maximo")));
                tipoDescuento.setActivo(true);
                tipoDescuentoDAO.crear(tipoDescuento);
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Tipo descuento</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Tipo de descuento creado correctamente</h1>");
                out.println("</body>");
                out.println("</html>");
            }
            out.flush();
        }
    }
  
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Servlet para TipoDescuento";
    }
}
