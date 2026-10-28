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

/** Conexión real a PostgreSQL y datos aislados por transacción para las pruebas de DAO. */
@EnabledIfSystemProperty(named = "integracion", matches = "true")
public abstract class IntegracionDAOBase {
    private static EntityManagerFactory factory;
    private static ValidatorFactory validadores;
    protected EntityManager em;
    protected Producto producto;
    protected TipoProducto tipo;
    protected Caracteristica caracteristica;
    protected Descuento descuento;

    /**
     * Abre los servicios reales de validación y persistencia con la configuración local de Liberty.
     */
    @BeforeAll
    static void conectar() throws Exception {
        Path config = Path.of(System.getProperty("cafefe.serverXml",
                System.getProperty("user.home") + "/Downloads/openliberty-jakartaee11-26.0.0.8/wlp/usr/servers/cafefeServer/server.xml"));
        var parser = DocumentBuilderFactory.newInstance();
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        var doc = parser.newDocumentBuilder().parse(config.toFile());
        Element propiedades = (Element) doc.getElementsByTagName("properties.postgresql").item(0);
        if (propiedades == null) throw new IllegalStateException("Falta la configuración PostgreSQL de Liberty");
        Map<String, Object> conexion = new HashMap<>();
        conexion.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        conexion.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://"
                + propiedades.getAttribute("serverName") + ":" + propiedades.getAttribute("portNumber")
                + "/" + propiedades.getAttribute("databaseName"));
        conexion.put("jakarta.persistence.jdbc.user", propiedades.getAttribute("user"));
        conexion.put("jakarta.persistence.jdbc.password", propiedades.getAttribute("password"));
        // Las credenciales se leen localmente y nunca se escriben en los reportes.
        validadores = Validation.byDefaultProvider().configure()
                .messageInterpolator(new org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator())
                .buildValidatorFactory();
        conexion.put("jakarta.persistence.validation.factory", validadores);
        factory = Persistence.createEntityManagerFactory("Cafefe-Integracion", conexion);
    }

    /**
     * Cierra las fábricas de persistencia y validación utilizadas por las pruebas.
     */
    @AfterAll
    static void cerrar() {
        if (factory != null) factory.close();
        if (validadores != null) validadores.close();
    }

    /**
     * Asigna una dependencia al objeto de prueba sin iniciar el contenedor Jakarta EE.
     * @param destino destino utilizado por la operación
     * @param clase clase utilizado por la operación
     * @param nombre nombre usado para identificar el dato en los mensajes
     * @param valor nuevo valor que se asignará
     */
    static void inyectar(Object destino, Class<?> clase, String nombre, Object valor) {
        try {
            var campo = clase.getDeclaredField(nombre);
            campo.setAccessible(true);
            campo.set(destino, valor);
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    /**
     * Verifica que dao.
     * @param dao dao utilizado por la operación
     * @return resultado de la consulta u operación
     */
    protected <D extends DefaultDAO<?>> D dao(D dao) {
        inyectar(dao, dao.getClass(), "em", em);
        inyectar(dao, DefaultDAO.class, "validator", validadores.getValidator());
        return dao;
    }

    protected Empleado empleado;
    protected Rol rol;
    protected EmpleadoRol empleadoRol;
    protected Orden orden;

    /**
     * Inicia una transacción y crea los datos válidos que necesita cada prueba.
     */
    @BeforeEach
    void preparar() {
        em = factory.createEntityManager();
        em.getTransaction().begin();
        producto = new Producto(UUID.randomUUID(), "Integración café", new BigDecimal("2.50"), true);
        dao(new ProductoDAO()).crear(producto);
        tipo = new TipoProducto(UUID.randomUUID(), "Integración bebidas");
        tipo.setActivo(true);
        dao(new TipoProductoDAO()).crear(tipo);
        var tipoCaracteristica = new TipoCaracteristica(UUID.randomUUID(), "Integración número");
        tipoCaracteristica.setActivo(true);
        tipoCaracteristica.setExpresionRegular("[0-9]+");
        dao(new TipoCaracteristicaDAO()).crear(tipoCaracteristica);
        caracteristica = new Caracteristica(UUID.randomUUID(), "Integración tamaño");
        caracteristica.setActivo(true);
        caracteristica.setIdTipoCaracteristica(tipoCaracteristica);
        dao(new CaracteristicaDAO()).crear(caracteristica);
        var tipoDescuento = new TipoDescuento(UUID.randomUUID(), "Integración promoción");
        tipoDescuento.setActivo(true);
        tipoDescuento.setDescuentoMaximo(25);
        dao(new TipoDescuentoDAO()).crear(tipoDescuento);
        descuento = new Descuento(UUID.randomUUID());
        descuento.setNombre("Integración descuento");
        descuento.setFechaInicio(new Date(0));
        descuento.setIdTipoDescuento(tipoDescuento);
        dao(new DescuentoDAO()).crear(descuento);
        empleado = new Empleado(UUID.randomUUID());
        empleado.setNombre("Integración"); empleado.setApellido("Prueba"); empleado.setActivo(true);
        dao(new EmpleadoDAO()).crear(empleado);
        rol = new Rol(UUID.randomUUID()); rol.setNombre("Camarero"); rol.setActivo(true);
        dao(new RolDAO()).crear(rol);
        empleadoRol = new EmpleadoRol(UUID.randomUUID());
        empleadoRol.setIdEmpleado(empleado); empleadoRol.setIdRol(rol); empleadoRol.setActivo(true);
        dao(new EmpleadoRolDAO()).crear(empleadoRol);
        orden = new Orden(UUID.randomUUID()); orden.setFechaOrden(new Date()); orden.setIdEmpleadoRol(empleadoRol);
        dao(new OrdenDAO()).crear(orden);
        em.flush();
    }

    /**
     * Revierte la transacción y cierra el contexto para no dejar registros de prueba.
     */
    @AfterEach
    void revertir() {
        if (em != null) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }

    /** Fabrica un registro válido que aún no existe; sus referencias sí están persistidas. */
    protected <T> T registroValido(Class<T> clase) {
        Object r;
        UUID id = UUID.randomUUID();
        if (clase == Producto.class) r = new Producto(id, "Integración producto", BigDecimal.ONE, true);
        else if (clase == TipoProducto.class) {
            var t = new TipoProducto(id, "Integración categoría"); t.setActivo(true); r = t;
        } else if (clase == TipoCaracteristica.class) {
            var t = new TipoCaracteristica(id, "Integración formato"); t.setActivo(true); t.setExpresionRegular("[0-9]+"); r = t;
        } else if (clase == Caracteristica.class) {
            var c = new Caracteristica(id, "Integración característica"); c.setActivo(true);
            c.setIdTipoCaracteristica(caracteristica.getIdTipoCaracteristica()); r = c;
        } else if (clase == TipoDescuento.class) {
            var t = new TipoDescuento(id, "Integración límite"); t.setActivo(true); t.setDescuentoMaximo(20); r = t;
        } else if (clase == Descuento.class) {
            var d = new Descuento(id, "Integración oferta", new Date());
            d.setIdTipoDescuento(descuento.getIdTipoDescuento()); r = d;
        } else if (clase == Empleado.class) {
            var e = new Empleado(id); e.setNombre("Integración"); e.setApellido("Empleado"); e.setActivo(true); r = e;
        } else if (clase == Rol.class) {
            var rol = new Rol(id); rol.setNombre("Integración puesto"); rol.setActivo(true); r = rol;
        } else if (clase == EmpleadoRol.class) {
            var er = new EmpleadoRol(id); er.setIdEmpleado(empleado); er.setIdRol(rol); er.setActivo(true); r = er;
        } else if (clase == Orden.class) {
            var o = new Orden(id); o.setFechaOrden(new Date()); o.setIdEmpleadoRol(empleadoRol); r = o;
        } else if (clase == OrdenProducto.class) {
            var op = new OrdenProducto(id); op.setIdOrden(orden); op.setIdProducto(producto); op.setPrecio(BigDecimal.ONE); r = op;
        } else if (clase == ProductoTipoProducto.class) {
            var pt = new ProductoTipoProducto(id); pt.setIdProducto(producto); pt.setIdTipoProducto(tipo);
            pt.setActivo(true); pt.setFechaCreacion(new Date()); r = pt;
        } else if (clase == ProductoCaracteristica.class) {
            var pc = new ProductoCaracteristica(id); pc.setIdProducto(producto); pc.setIdCaracteristica(caracteristica);
            pc.setValor("250"); r = pc;
        } else if (clase == DescuentoProducto.class) {
            var dp = new DescuentoProducto(id); dp.setIdProducto(producto); dp.setIdDescuento(descuento);
            dp.setFechaInicio(new Date()); dp.setValor(10); r = dp;
        } else throw new IllegalArgumentException("Entidad sin datos de prueba: " + clase.getSimpleName());
        return clase.cast(r);
    }
}
