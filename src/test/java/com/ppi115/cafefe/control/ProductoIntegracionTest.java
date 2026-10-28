package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.*;
import jakarta.persistence.*;
import jakarta.validation.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.w3c.dom.Element;
import static org.junit.jupiter.api.Assertions.*;

/** Casos de integración que combinan productos con categorías, características y descuentos. */
@EnabledIfSystemProperty(named = "integracion", matches = "true")
public class ProductoIntegracionTest extends IntegracionDAOBase {
    /**
     * Construye una categoría de prueba vinculada al producto y tipo de la transacción.
     * @return resultado de la consulta u operación
     */
    private ProductoTipoProducto categoria() {
        var r = new ProductoTipoProducto(UUID.randomUUID());
        r.setIdProducto(producto);
        r.setIdTipoProducto(tipo);
        r.setActivo(true);
        r.setFechaCreacion(new Date());
        return r;
    }

    /**
     * Construye una característica de prueba con un valor que cumple el formato configurado.
     * @return resultado de la consulta u operación
     */
    private ProductoCaracteristica detalle() {
        var r = new ProductoCaracteristica(UUID.randomUUID());
        r.setIdProducto(producto);
        r.setIdCaracteristica(caracteristica);
        r.setValor("250");
        return r;
    }

    /**
     * Construye un descuento de prueba dentro de los límites y fechas permitidos.
     * @return resultado de la consulta u operación
     */
    private DescuentoProducto promocion() {
        var r = new DescuentoProducto(UUID.randomUUID());
        r.setIdProducto(producto);
        r.setIdDescuento(descuento);
        r.setFechaInicio(new Date());
        r.setValor(10);
        return r;
    }

    /**
     * Verifica que producto crear modificar consultar eliminar.
     */
    @Test
    void productoCrearModificarConsultarEliminar() {
        var dao = dao(new ProductoDAO());
        var nuevo = new Producto(UUID.randomUUID(), "Integración CRUD", BigDecimal.ONE, true);
        dao.crear(nuevo);
        em.flush(); em.clear();
        var leido = dao.findById(nuevo.getIdProducto());
        assertEquals("Integración CRUD", leido.getNombre());
        leido.setNombre("Integración modificado");
        dao.modificar(leido);
        em.flush(); em.clear();
        assertEquals("Integración modificado", dao.findById(nuevo.getIdProducto()).getNombre());
        dao.eliminar(nuevo.getIdProducto());
        em.flush(); em.clear();
        assertNull(dao.findById(nuevo.getIdProducto()));
    }

    /**
     * Verifica que producto invalido no se persiste.
     */
    @Test
    void productoInvalidoNoSePersiste() {
        var invalido = new Producto(UUID.randomUUID(), " ", BigDecimal.ZERO, true);
        assertThrows(IllegalArgumentException.class, () -> dao(new ProductoDAO()).crear(invalido));
        assertNull(em.find(Producto.class, invalido.getIdProducto()));
    }

    /**
     * Verifica que categorias multiples sin duplicados yfiltradas por producto.
     */
    @Test
    void categoriasMultiplesSinDuplicadosYFiltradasPorProducto() {
        var dao = dao(new ProductoTipoProductoDAO());
        var primera = categoria();
        dao.crear(primera);
        assertThrows(IllegalArgumentException.class, () -> dao.crear(categoria()));
        var otra = new TipoProducto(UUID.randomUUID(), "Integración otra categoría");
        otra.setActivo(true);
        dao(new TipoProductoDAO()).crear(otra);
        var segunda = categoria(); segunda.setIdTipoProducto(otra);
        dao.crear(segunda);
        em.flush(); em.clear();
        assertEquals(2, dao.findByIdProducto(producto.getIdProducto()).size());
        assertTrue(dao.findByIdProducto(UUID.randomUUID()).isEmpty());
        var editar = dao.findById(primera.getIdProductoTipoProducto());
        editar.setObservaciones("Cambio válido");
        dao.modificar(editar);
        em.flush();
        dao.eliminar(segunda.getIdProductoTipoProducto());
        em.flush();
        assertEquals(1, dao.findByIdProducto(producto.getIdProducto()).size());
    }

    /**
     * Verifica que caracteristica crear editar eliminar yvalidar formato.
     */
    @Test
    void caracteristicaCrearEditarEliminarYValidarFormato() {
        var dao = dao(new ProductoCaracteristicaDAO());
        var r = detalle(); dao.crear(r);
        em.flush(); em.clear();
        r = dao.findById(r.getIdProductoCaracteristica());
        assertEquals("250", r.getValor());
        r.setValor("500"); dao.modificar(r);
        em.flush(); em.clear();
        assertEquals("500", dao.findByIdProducto(producto.getIdProducto()).get(0).getValor());
        var invalido = detalle(); invalido.setValor("texto");
        assertThrows(IllegalArgumentException.class, () -> dao.crear(invalido));
        assertThrows(IllegalArgumentException.class, () -> dao.modificar(invalido));
        assertThrows(IllegalArgumentException.class, () -> dao.crear(detalle()));
        dao.eliminar(r.getIdProductoCaracteristica()); em.flush();
        assertTrue(dao.findByIdProducto(producto.getIdProducto()).isEmpty());
    }

    /**
     * Verifica que descuento crear editar eliminar sin duplicados.
     */
    @Test
    void descuentoCrearEditarEliminarSinDuplicados() {
        var dao = dao(new DescuentoProductoDAO());
        var r = promocion(); dao.crear(r);
        em.flush(); em.clear();
        assertTrue(dao.existeProductoDescuento(producto.getIdProducto(), descuento.getIdDescuento()));
        assertThrows(IllegalArgumentException.class, () -> dao.crear(promocion()));
        r = dao.findById(r.getIdDescuentoProducto());
        r.setValor(15); dao.modificar(r); em.flush(); em.clear();
        assertEquals(15, dao.findByIdProducto(producto.getIdProducto()).get(0).getValor());
        dao.eliminar(r.getIdDescuentoProducto()); em.flush();
        assertFalse(dao.existeProductoDescuento(producto.getIdProducto(), descuento.getIdDescuento()));
    }

    /**
     * Verifica que descuento valida limites yfechas.
     */
    @Test
    void descuentoValidaLimitesYFechas() {
        var dao = dao(new DescuentoProductoDAO());
        var r = promocion(); r.setValor(26);
        assertThrows(IllegalArgumentException.class, () -> dao.crear(r));
        r.setValor(10); r.setFechaFin(new Date(0));
        assertThrows(IllegalArgumentException.class, () -> dao.crear(r));
        r.setFechaFin(null); r.setIdProducto(new Producto(UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class, () -> dao.modificar(r));
    }

    /**
     * Verifica que asignaciones rechazan catalogos inactivos.
     */
    @Test
    void asignacionesRechazanCatalogosInactivos() {
        tipo.setActivo(false);
        caracteristica.setActivo(false);
        descuento.getIdTipoDescuento().setActivo(false);
        assertThrows(IllegalArgumentException.class, () -> dao(new ProductoTipoProductoDAO()).crear(categoria()));
        assertThrows(IllegalArgumentException.class, () -> dao(new ProductoCaracteristicaDAO()).crear(detalle()));
        assertThrows(IllegalArgumentException.class, () -> dao(new DescuentoProductoDAO()).crear(promocion()));
    }
}
