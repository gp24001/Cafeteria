package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.TipoDescuento;
import com.ppi115.cafefe.control.DefaultDAO.DatosInvalidosException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;

/** Regresiones de validación y selección; complementan las pruebas con PostgreSQL. */
public class ValidacionDAOTest {

    /** Comprueba que los selectores UUID resuelven entidades permitidas sin convertidores de entidad. */
    @Test
    void selectoresDeOrdenResuelvenIdentificadoresSinClasesAdicionales() {
        try (var faces = mockStatic(jakarta.faces.context.FacesContext.class)) {
            faces.when(jakarta.faces.context.FacesContext::getCurrentInstance)
                    .thenReturn(mock(jakarta.faces.context.FacesContext.class));
            var modelo = new com.ppi115.cafefe.boundary.jsf.OrdenModel();
            var orden = new com.ppi115.cafefe.entity.Orden(UUID.randomUUID());
            orden.setFechaOrden(new java.util.Date());
            modelo.setRegistro(orden);
            modelo.setEstado(com.ppi115.cafefe.boundary.jsf.ESTADO_CRUD.NUEVO);
            var ordenDAO = mock(OrdenDAO.class);
            var productoDAO = mock(ProductoDAO.class);
            var descuentoDAO = mock(DescuentoProductoDAO.class);
            inyectar(modelo, modelo.getClass(), "dao", ordenDAO);
            inyectar(modelo, modelo.getClass(), "productoDao", productoDAO);
            inyectar(modelo, modelo.getClass(), "descuentoProductoDAO", descuentoDAO);
            var camarero = new com.ppi115.cafefe.entity.EmpleadoRol(UUID.randomUUID());
            when(ordenDAO.findCamareros()).thenReturn(List.of(camarero));
            var activo = new com.ppi115.cafefe.entity.Producto(UUID.randomUUID(), "Café", java.math.BigDecimal.TEN, true);
            var inactivo = new com.ppi115.cafefe.entity.Producto(UUID.randomUUID(), "Té", java.math.BigDecimal.ONE, false);
            when(productoDAO.findAll()).thenReturn(List.of(activo, inactivo));
            modelo.setIdEmpleadoRolSeleccionado(camarero.getIdEmpleadoRol());
            assertSame(camarero, orden.getIdEmpleadoRol());
            modelo.setIdEmpleadoRolSeleccionado(UUID.randomUUID());
            assertSame(camarero, orden.getIdEmpleadoRol());
            modelo.setIdProductoSeleccionado(inactivo.getIdProducto());
            assertNull(modelo.getProductoSeleccionado());
            modelo.setIdProductoSeleccionado(activo.getIdProducto());
            assertSame(activo, modelo.getProductoSeleccionado());
            var descuento = new com.ppi115.cafefe.entity.Descuento(UUID.randomUUID());
            var asignacion = new com.ppi115.cafefe.entity.DescuentoProducto(UUID.randomUUID());
            asignacion.setIdProducto(activo); asignacion.setIdDescuento(descuento);
            when(descuentoDAO.findVigentes(List.of(activo), orden.getFechaOrden())).thenReturn(List.of(asignacion));
            modelo.agregarProducto(null);
            modelo.setIdDescuentoSeleccionado(descuento.getIdDescuento());
            assertSame(descuento, modelo.getDescuentoSeleccionado());
            modelo.setIdDescuentoSeleccionado(UUID.randomUUID());
            assertSame(descuento, modelo.getDescuentoSeleccionado());
            modelo.setEstado(com.ppi115.cafefe.boundary.jsf.ESTADO_CRUD.EDITAR);
            modelo.setIdEmpleadoRolSeleccionado(null);
            modelo.setIdDescuentoSeleccionado(null);
            assertSame(camarero, orden.getIdEmpleadoRol());
            assertSame(descuento, modelo.getDescuentoSeleccionado());
        }
    }

    /** Verifica que guardar datos generales no recrea los formularios de otras pestañas. */
    @Test
    void guardarProductoConservaElBorradorDeOtraPestana() {
        try (var faces = mockStatic(jakarta.faces.context.FacesContext.class)) {
            faces.when(jakarta.faces.context.FacesContext::getCurrentInstance)
                    .thenReturn(mock(jakarta.faces.context.FacesContext.class));
            var modelo = new com.ppi115.cafefe.boundary.jsf.ProductoModel();
            var tipos = new com.ppi115.cafefe.boundary.jsf.TipoProductoModel();
            var caracteristicas = new com.ppi115.cafefe.boundary.jsf.CaracteristicaModel();
            var descuentos = new com.ppi115.cafefe.boundary.jsf.DescuentoModel();
            inyectar(modelo, modelo.getClass(), "dao", mock(ProductoDAO.class));
            inyectar(modelo, modelo.getClass(), "tipos", tipos);
            inyectar(modelo, modelo.getClass(), "caracteristicas", caracteristicas);
            inyectar(modelo, modelo.getClass(), "descuentos", descuentos);
            inyectar(modelo, com.ppi115.cafefe.boundary.jsf.AbstractModel.class, "validator", validador());
            inyectar(tipos, tipos.getClass(), "asignacionDAO", mock(ProductoTipoProductoDAO.class));
            inyectar(caracteristicas, caracteristicas.getClass(), "asignacionDAO", mock(ProductoCaracteristicaDAO.class));
            inyectar(descuentos, descuentos.getClass(), "asignacionDAO", mock(DescuentoProductoDAO.class));
            var producto = new com.ppi115.cafefe.entity.Producto(UUID.randomUUID());
            producto.setActivo(true);
            var evento = mock(org.primefaces.event.SelectEvent.class);
            when(evento.getObject()).thenReturn(producto);
            modelo.seleccionar(evento);
            var borrador = caracteristicas.getAsignacionesProducto().getRegistro();
            borrador.setValor("250");
            modelo.guardar();
            assertSame(producto, modelo.getSeleccionado());
            assertSame(borrador, caracteristicas.getAsignacionesProducto().getRegistro());
            assertEquals("250", borrador.getValor());
        }
    }

    /** Verifica que guardar o cancelar un rol mantiene el empleado y la pestaña seleccionados. */
    @Test
    void guardarRolNoBorraLaSeleccionDelEmpleado() {
        try (var faces = mockStatic(jakarta.faces.context.FacesContext.class)) {
            faces.when(jakarta.faces.context.FacesContext::getCurrentInstance)
                    .thenReturn(mock(jakarta.faces.context.FacesContext.class));
            var modelo = new com.ppi115.cafefe.boundary.jsf.EmpleadoModel();
            var daoRoles = mock(RolDAO.class);
            var daoAsignaciones = mock(EmpleadoRolDAO.class);
            inyectar(modelo, modelo.getClass(), "rolDAO", daoRoles);
            inyectar(modelo, modelo.getClass(), "empleadoRolDAO", daoAsignaciones);
            inyectar(modelo, com.ppi115.cafefe.boundary.jsf.AbstractModel.class, "validator", validador());
            var empleado = new com.ppi115.cafefe.entity.Empleado(UUID.randomUUID());
            empleado.setActivo(true);
            var evento = mock(org.primefaces.event.SelectEvent.class);
            when(evento.getObject()).thenReturn(empleado);
            modelo.seleccionar(evento);
            var rol = new com.ppi115.cafefe.entity.Rol(UUID.randomUUID());
            rol.setActivo(true);
            when(daoRoles.findById(rol.getIdRol())).thenReturn(rol);
            modelo.setIdRolSeleccionado(rol.getIdRol());
            modelo.setPestanaActiva(1);
            var asignacion = modelo.getEmpleadoRol();
            modelo.guardarRol();
            verify(daoAsignaciones).crear(asignacion);
            assertSame(empleado, modelo.getSeleccionado());
            assertSame(empleado, modelo.getRegistro());
            assertSame(asignacion, modelo.getEmpleadoRolSeleccionado());
            assertEquals(com.ppi115.cafefe.boundary.jsf.ESTADO_CRUD.EDITAR, modelo.getEstadoRol());
            assertEquals(1, modelo.getPestanaActiva());
            modelo.cancelarRol();
            assertSame(empleado, modelo.getSeleccionado());
            assertEquals(1, modelo.getPestanaActiva());
        }
    }

    /** Verifica la conversión usada por los selectores compactos sin modificar la conversión JPA. */
    @Test
    void convertirUuidEnSelectores() {
        var converter = new UUIDConverter();
        UUID id = UUID.randomUUID();
        assertEquals(id, converter.getAsObject(null, null, id.toString()));
        assertEquals(id.toString(), converter.getAsString(null, null, id));
        assertNull(converter.getAsObject(null, null, ""));
        assertEquals("", converter.getAsString(null, null, null));
        assertThrows(jakarta.faces.convert.ConverterException.class,
                () -> converter.getAsObject(null, null, "identificador inválido"));
        assertEquals(id, converter.convertToDatabaseColumn(id));
        assertEquals(id, converter.convertToEntityAttribute(id));
    }

    /** Comprueba que el Model entrega al servicio únicamente la línea seleccionada. */
    @Test
    void descuentoDeOrdenSeAplicaSoloALaLineaSeleccionada() {
        try (var faces = mockStatic(jakarta.faces.context.FacesContext.class)) {
            var contexto = mock(jakarta.faces.context.FacesContext.class);
            faces.when(jakarta.faces.context.FacesContext::getCurrentInstance).thenReturn(contexto);
            var modelo = new com.ppi115.cafefe.boundary.jsf.OrdenModel();
            var orden = new com.ppi115.cafefe.entity.Orden(UUID.randomUUID());
            orden.setFechaOrden(new java.util.Date());
            modelo.setRegistro(orden);
            modelo.setEstado(com.ppi115.cafefe.boundary.jsf.ESTADO_CRUD.NUEVO);
            var servicio = mock(DescuentoProductoDAO.class);
            inyectar(modelo, modelo.getClass(), "descuentoProductoDAO", servicio);
            var producto = new com.ppi115.cafefe.entity.Producto(UUID.randomUUID(), "Café", new java.math.BigDecimal("10.00"), true);
            var otro = new com.ppi115.cafefe.entity.Producto(UUID.randomUUID(), "Té", new java.math.BigDecimal("5.00"), true);
            var descuento = new com.ppi115.cafefe.entity.Descuento(UUID.randomUUID());
            var asignado = new com.ppi115.cafefe.entity.DescuentoProducto(UUID.randomUUID());
            asignado.setIdProducto(producto); asignado.setIdDescuento(descuento);
            when(servicio.findVigentes(List.of(producto), orden.getFechaOrden())).thenReturn(List.of(asignado));
            when(servicio.findVigentes(List.of(otro), orden.getFechaOrden())).thenReturn(List.of());
            modelo.setProductoSeleccionado(producto); modelo.agregarProducto(null);
            modelo.setProductoSeleccionado(otro); modelo.agregarProducto(null);
            var primera = modelo.getLineas().get(0);
            modelo.setLineaSeleccionada(primera); modelo.cargarDescuentos(null);
            modelo.setDescuentoSeleccionado(descuento);
            when(servicio.aplicar(descuento, primera, orden.getFechaOrden())).thenReturn(new DescuentoProductoDAO.Resultado(1, 0));
            modelo.aplicarDescuento(null);
            verify(servicio).aplicar(descuento, primera, orden.getFechaOrden());
            verify(servicio, never()).aplicar(any(), anyList(), any());
            assertEquals(new java.math.BigDecimal("5.00"), modelo.getLineas().get(1).getPrecio());
            assertSame(primera, modelo.getLineaSeleccionada());
            assertSame(orden, modelo.getRegistro());
            // Otra fecha ya no ofrece el descuento anterior y limpia su selección.
            var fueraDeRango = new java.util.Date(orden.getFechaOrden().getTime() + 86400000L);
            when(servicio.findVigentes(List.of(producto), fueraDeRango)).thenReturn(List.of());
            orden.setFechaOrden(fueraDeRango);
            modelo.cambiarFecha();
            assertTrue(modelo.getDescuentosAplicables().isEmpty());
            assertNull(modelo.getDescuentoSeleccionado());
            verify(servicio).findVigentes(List.of(producto), fueraDeRango);
            verify(servicio, never()).findAsignados(any());
        }
    }

    /**
     * Comprueba que todos los DAO conservan la causa de los errores de lectura.
     * @return casos ejecutados para cada DAO concreto
     */
    @org.junit.jupiter.api.TestFactory
    java.util.stream.Stream<org.junit.jupiter.api.DynamicTest> erroresDeConsultaEnTodosLosDAO() {
        return java.util.stream.Stream.of(new ProductoDAO(), new TipoProductoDAO(), new CaracteristicaDAO(),
                new TipoCaracteristicaDAO(), new DescuentoDAO(), new TipoDescuentoDAO(),
                new ProductoTipoProductoDAO(), new ProductoCaracteristicaDAO(), new DescuentoProductoDAO(),
                new EmpleadoDAO(), new EmpleadoRolDAO(), new RolDAO(), new OrdenDAO(), new OrdenProductoDAO())
                .map(dao -> org.junit.jupiter.api.DynamicTest.dynamicTest(dao.getClass().getSimpleName(), () -> {
                    EntityManager em = mock(EntityManager.class);
                    var causa = new jakarta.persistence.PersistenceException("Conexión no disponible");
                    when(em.find(any(Class.class), any())).thenThrow(causa);
                    when(em.getCriteriaBuilder()).thenThrow(causa);
                    when(em.createQuery(anyString(), any(Class.class))).thenThrow(causa);
                    inyectar(dao, dao.getClass(), "em", em);
                    var error = assertThrows(DatosInvalidosException.class, () -> dao.findById(UUID.randomUUID()));
                    assertSame(causa, error.getCause());
                    assertThrows(DatosInvalidosException.class, dao::findAll);
                    assertThrows(DatosInvalidosException.class, () -> dao.findRange(0, 5));
                    assertThrows(DatosInvalidosException.class, () -> dao.findRange(0, 5, java.util.Map.of()));
                    assertThrows(DatosInvalidosException.class, dao::contar);
                }));
    }

    /**
     * Comprueba validaciones de entrada y presentación de errores en cada Model.
     * @return casos sin dependencias del servidor ni del navegador
     */
    @org.junit.jupiter.api.TestFactory
    java.util.stream.Stream<org.junit.jupiter.api.DynamicTest> entradasInvalidasYErroresEnTodosLosModel() {
        return java.util.stream.Stream.of("Producto", "TipoProducto", "Caracteristica", "TipoCaracteristica",
                "Descuento", "TipoDescuento", "Empleado", "EmpleadoRol", "Rol", "Orden")
                .map(nombre -> org.junit.jupiter.api.DynamicTest.dynamicTest(nombre + "Model", () -> {
                    try (var faces = mockStatic(jakarta.faces.context.FacesContext.class)) {
                        var contexto = mock(jakarta.faces.context.FacesContext.class);
                        faces.when(jakarta.faces.context.FacesContext::getCurrentInstance).thenReturn(contexto);
                        Class<?> tipoModel = Class.forName("com.ppi115.cafefe.boundary.jsf." + nombre + "Model");
                        com.ppi115.cafefe.boundary.jsf.AbstractModel modelo =
                                (com.ppi115.cafefe.boundary.jsf.AbstractModel) tipoModel.getDeclaredConstructor().newInstance();
                        Class<? extends DefaultDAO> tipoDao = (Class<? extends DefaultDAO>)
                                Class.forName("com.ppi115.cafefe.control." + nombre + "DAO");
                        DefaultDAO dao = mock(tipoDao);
                        inyectar(modelo, tipoModel, "dao", dao);
                        inyectar(modelo, com.ppi115.cafefe.boundary.jsf.AbstractModel.class, "validator", validador());
                        assertDoesNotThrow(modelo::guardar);
                        assertDoesNotThrow(() -> modelo.editar(null));
                        assertDoesNotThrow(() -> modelo.seleccionar(null));
                        when(dao.findById(any())).thenThrow(new DatosInvalidosException("No se pudo leer el registro"));
                        assertNull(modelo.getRegistrerById(UUID.randomUUID()));
                        verify(dao, never()).crear(any());
                        verify(dao, never()).modificar(any());
                        verify(contexto, atLeastOnce()).addMessage(eq(null), any(jakarta.faces.application.FacesMessage.class));
                    }
                }));
    }
    /**
     * Asigna una dependencia al objeto de prueba sin iniciar el contenedor Jakarta EE.
     * @param objeto objeto utilizado por la operación
     * @param clase clase utilizado por la operación
     * @param nombre nombre usado para identificar el dato en los mensajes
     * @param valor nuevo valor que se asignará
     */
    private static void inyectar(Object objeto, Class<?> clase, String nombre, Object valor) {
        try {
            var campo = clase.getDeclaredField(nombre);
            campo.setAccessible(true);
            campo.set(objeto, valor);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Construye un validador simulado para aislar las pruebas unitarias de persistencia.
     * @return resultado de la consulta u operación
     */
    private static jakarta.validation.Validator validador() {
        var validator = mock(jakarta.validation.Validator.class);
        when(validator.validate(any())).thenReturn(java.util.Set.of());
        return validator;
    }

    /**
     * Verifica que rechaza entidad nula sin persistir.
     */
    @Test
    public void rechazaEntidadNulaSinPersistir() {
        EntityManager em = mock(EntityManager.class);
        var dao = crearDAO(em);
        assertThrows(IllegalArgumentException.class, () -> dao.crear(null));
        assertThrows(IllegalArgumentException.class, () -> dao.modificar(null));
        verifyNoInteractions(em);
    }

    /**
     * Verifica que traduce error de persistencia conservando causa.
     */
    @Test
    public void traduceErrorDePersistenciaConservandoCausa() {
        EntityManager em = mock(EntityManager.class);
        var dao = crearDAO(em);
        var fallo = new jakarta.persistence.PersistenceException("detalle técnico");
        doThrow(fallo).when(em).persist(any());
        doThrow(fallo).when(em).merge(any());
        var registro = new TipoDescuento();
        var error = assertThrows(DatosInvalidosException.class, () -> dao.crear(registro));
        assertSame(fallo, error.getCause());
        assertFalse(error.getMessage().contains("detalle técnico"));
        assertThrows(DatosInvalidosException.class, () -> dao.modificar(registro));
    }

    /**
     * Verifica que rechaza identificador yrangos invalidos.
     */
    @Test
    public void rechazaIdentificadorYRangosInvalidos() {
        EntityManager em = mock(EntityManager.class);
        var dao = crearDAO(em);
        assertThrows(IllegalArgumentException.class, () -> dao.findById(null));
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(0, 0, java.util.Map.of()));
        verifyNoInteractions(em);
    }

    /**
     * Verifica que valida restricciones antes de crear omodificar.
     */
    @Test
    public void validaRestriccionesAntesDeCrearOModificar() {
        EntityManager em = mock(EntityManager.class);
        var dao = crearDAO(em);
        var validator = validador();
        jakarta.validation.ConstraintViolation<TipoDescuento> error = mock(jakarta.validation.ConstraintViolation.class);
        when(error.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(error.getMessage()).thenReturn("El nombre es obligatorio");
        var registro = new TipoDescuento();
        when(validator.validate(registro)).thenReturn(java.util.Set.of(error));
        inyectar(dao, DefaultDAO.class, "validator", validator);
        assertThrows(IllegalArgumentException.class, () -> dao.crear(registro));
        assertThrows(IllegalArgumentException.class, () -> dao.modificar(registro));
        verifyNoInteractions(em);
    }

    /**
     * Verifica que dao caracteristica valida formato tambien al modificar.
     */
    @Test
    public void daoCaracteristicaValidaFormatoTambienAlModificar() {
        EntityManager em = mock(EntityManager.class);
        var dao = new ProductoCaracteristicaDAO();
        inyectar(dao, ProductoCaracteristicaDAO.class, "em", em);
        inyectar(dao, DefaultDAO.class, "validator", validador());
        var producto = new com.ppi115.cafefe.entity.Producto(UUID.randomUUID());
        producto.setActivo(true);
        var caracteristica = new com.ppi115.cafefe.entity.Caracteristica(UUID.randomUUID());
        caracteristica.setActivo(true);
        var tipo = new com.ppi115.cafefe.entity.TipoCaracteristica();
        tipo.setExpresionRegular("[0-9]+");
        caracteristica.setIdTipoCaracteristica(tipo);
        var asignacion = new com.ppi115.cafefe.entity.ProductoCaracteristica(UUID.randomUUID());
        asignacion.setIdProducto(producto);
        asignacion.setIdCaracteristica(caracteristica);
        asignacion.setValor("texto");
        when(em.find(com.ppi115.cafefe.entity.Producto.class, producto.getIdProducto())).thenReturn(producto);
        when(em.find(com.ppi115.cafefe.entity.Caracteristica.class, caracteristica.getIdCaracteristica())).thenReturn(caracteristica);
        assertThrows(IllegalArgumentException.class, () -> dao.crear(asignacion));
        assertThrows(IllegalArgumentException.class, () -> dao.modificar(asignacion));
        verify(em, never()).persist(any());
        verify(em, never()).merge(any());
    }

    /**
     * Verifica que dao rechaza producto inactivo aunque la vista lo permita.
     */
    @Test
    public void daoRechazaProductoInactivoAunqueLaVistaLoPermita() {
        EntityManager em = mock(EntityManager.class);
        var dao = new ProductoTipoProductoDAO();
        inyectar(dao, ProductoTipoProductoDAO.class, "em", em);
        inyectar(dao, DefaultDAO.class, "validator", validador());
        var producto = new com.ppi115.cafefe.entity.Producto(UUID.randomUUID());
        var asignacion = new com.ppi115.cafefe.entity.ProductoTipoProducto(UUID.randomUUID());
        asignacion.setIdProducto(producto);
        when(em.find(com.ppi115.cafefe.entity.Producto.class, producto.getIdProducto())).thenReturn(producto);
        assertThrows(IllegalArgumentException.class, () -> dao.crear(asignacion));
        assertThrows(IllegalArgumentException.class, () -> dao.modificar(asignacion));
        verify(em, never()).persist(any());
        verify(em, never()).merge(any());
    }

    /**
     * Verifica que guardar caracteristica conserva producto pestana yvalor.
     */
    @Test
    public void guardarCaracteristicaConservaProductoPestanaYValor() {
        try (var faces = mockStatic(jakarta.faces.context.FacesContext.class)) {
            var contexto = mock(jakarta.faces.context.FacesContext.class);
            faces.when(jakarta.faces.context.FacesContext::getCurrentInstance).thenReturn(contexto);
            var modelo = new com.ppi115.cafefe.boundary.jsf.ProductoModel();
            var tipos = new com.ppi115.cafefe.boundary.jsf.TipoProductoModel();
            var caracteristicas = new com.ppi115.cafefe.boundary.jsf.CaracteristicaModel();
            var descuentos = new com.ppi115.cafefe.boundary.jsf.DescuentoModel();
            var tiposDAO = mock(ProductoTipoProductoDAO.class);
            var caracteristicasDAO = mock(ProductoCaracteristicaDAO.class);
            var descuentosDAO = mock(DescuentoProductoDAO.class);
            inyectar(modelo, modelo.getClass(), "tipos", tipos);
            inyectar(modelo, modelo.getClass(), "caracteristicas", caracteristicas);
            inyectar(modelo, modelo.getClass(), "descuentos", descuentos);
            inyectar(tipos, tipos.getClass(), "asignacionDAO", tiposDAO);
            inyectar(caracteristicas, caracteristicas.getClass(), "asignacionDAO", caracteristicasDAO);
            inyectar(descuentos, descuentos.getClass(), "asignacionDAO", descuentosDAO);
            inyectar(caracteristicas, com.ppi115.cafefe.boundary.jsf.AbstractModel.class, "validator", validador());
            var producto = new com.ppi115.cafefe.entity.Producto(UUID.randomUUID());
            producto.setActivo(true);
            var primera = new com.ppi115.cafefe.entity.ProductoTipoProducto(UUID.randomUUID());
            var segunda = new com.ppi115.cafefe.entity.ProductoTipoProducto(UUID.randomUUID());
            when(tiposDAO.findByIdProducto(producto.getIdProducto())).thenReturn(List.of(primera, segunda));
            var evento = mock(org.primefaces.event.SelectEvent.class);
            when(evento.getObject()).thenReturn(producto);
            modelo.seleccionar(evento);
            modelo.setPestanaActiva(2);
            var panel = caracteristicas.getAsignacionesProducto();
            var asignacion = panel.getRegistro();
            asignacion.setValor("250");
            panel.guardar();
            assertSame(producto, modelo.getSeleccionado());
            assertSame(producto, modelo.getRegistro());
            assertSame(producto, panel.getRegistro().getIdProducto());
            assertEquals("250", panel.getRegistro().getValor());
            assertEquals(2, modelo.getPestanaActiva());
            assertEquals(2, tipos.getAsignacionesProducto().getAsignaciones().size());
            verify(caracteristicasDAO).crear(asignacion);
            panel.guardar();
            verify(caracteristicasDAO).modificar(asignacion);
            doThrow(new DatosInvalidosException("Valor inválido")).when(caracteristicasDAO).modificar(asignacion);
            panel.guardar();
            assertSame(producto, modelo.getSeleccionado());
            assertEquals("250", panel.getRegistro().getValor());
            panel.cancelar();
            assertSame(producto, modelo.getSeleccionado());
            assertSame(producto, panel.getRegistro().getIdProducto());
            assertEquals(2, modelo.getPestanaActiva());
        }
    }

    /**
     * Construye un DAO con las dependencias simuladas utilizadas por las pruebas unitarias.
     * @param em em utilizado por la operación
     * @return resultado de la consulta u operación
     */
    private TipoDescuentoDAO crearDAO(EntityManager em) {
        TipoDescuentoDAO dao = new TipoDescuentoDAO();

        try {
            java.lang.reflect.Field campo =
                    TipoDescuentoDAO.class.getDeclaredField("em");

            campo.setAccessible(true);
            campo.set(dao, em);
            var validacion = DefaultDAO.class.getDeclaredField("validator");
            validacion.setAccessible(true);
            var validator = mock(jakarta.validation.Validator.class);
            when(validator.validate(any())).thenReturn(java.util.Set.of());
            validacion.set(dao, validator);

        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }

        return dao;
    }

}
