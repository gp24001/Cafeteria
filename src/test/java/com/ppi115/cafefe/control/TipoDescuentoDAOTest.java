package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.TipoDescuento;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;

import com.ppi115.cafefe.boundary.TipoDescuentoDAO;

public class TipoDescuentoDAOTest {

    private final UUID id = UUID.fromString(
            "8a4685fa-304d-48d5-b3fc-df98b513fea2"
    );

    private TipoDescuentoDAO crearDAO(EntityManager em) {
        TipoDescuentoDAO dao = new TipoDescuentoDAO();

        try {
            java.lang.reflect.Field campo =
                    TipoDescuentoDAO.class.getDeclaredField("em");

            campo.setAccessible(true);
            campo.set(dao, em);

        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }

        return dao;
    }

    @Test
    public void testCrear() {
        EntityManager em = mock(EntityManager.class);
        TipoDescuentoDAO dao = crearDAO(em);

        TipoDescuento tipoDescuento = new TipoDescuento();

        tipoDescuento.setIdTipoDescuento(id);
        tipoDescuento.setNombre("Descuento estudiante");

        dao.crear(tipoDescuento);

        verify(em).persist(tipoDescuento);
    }

    @Test
    public void testModificar() {
        EntityManager em = mock(EntityManager.class);
        TipoDescuentoDAO dao = crearDAO(em);

        TipoDescuento tipoDescuento = new TipoDescuento();

        tipoDescuento.setIdTipoDescuento(id);
        tipoDescuento.setNombre("Descuento estudiante modificado");

        dao.modificar(tipoDescuento);

        verify(em).merge(tipoDescuento);
    }

    @Test
    public void testEliminar() {
        EntityManager em = mock(EntityManager.class);
        TipoDescuentoDAO dao = crearDAO(em);

        TipoDescuento tipoDescuento = new TipoDescuento();

        tipoDescuento.setIdTipoDescuento(id);
        tipoDescuento.setNombre("Descuento para eliminar");

        when(em.find(TipoDescuento.class, id))
                .thenReturn(tipoDescuento);

        dao.eliminar(id);

        verify(em).find(TipoDescuento.class, id);
        verify(em).remove(tipoDescuento);
    }

    @Test
    public void testFindById() {
        EntityManager em = mock(EntityManager.class);
        TipoDescuentoDAO dao = crearDAO(em);

        TipoDescuento tipoDescuento = new TipoDescuento();

        tipoDescuento.setIdTipoDescuento(id);
        tipoDescuento.setNombre("Descuento estudiante");

        when(em.find(TipoDescuento.class, id))
                .thenReturn(tipoDescuento);

        TipoDescuento resultado = dao.findById(id);

        assertNotNull(resultado);
        assertEquals(tipoDescuento, resultado);

        verify(em).find(TipoDescuento.class, id);
    }

    @Test
    public void testFindAll() {
        EntityManager em = mock(EntityManager.class);
        TipoDescuentoDAO dao = crearDAO(em);

        List<TipoDescuento> lista = List.of(
                new TipoDescuento(id, "Descuento estudiante")
        );

        jakarta.persistence.TypedQuery<TipoDescuento> query =
                mock(jakarta.persistence.TypedQuery.class);

        jakarta.persistence.criteria.CriteriaBuilder cb =
                mock(jakarta.persistence.criteria.CriteriaBuilder.class);

        jakarta.persistence.criteria.CriteriaQuery<TipoDescuento> cq =
                mock(jakarta.persistence.criteria.CriteriaQuery.class);

        jakarta.persistence.criteria.Root<TipoDescuento> root =
                mock(jakarta.persistence.criteria.Root.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(TipoDescuento.class)).thenReturn(cq);
        when(cq.from(TipoDescuento.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<TipoDescuento> resultado = dao.findAll();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(
                "Descuento estudiante",
                resultado.get(0).getNombre()
        );
    }

    @Test
    public void testFindRange() {
        EntityManager em = mock(EntityManager.class);
        TipoDescuentoDAO dao = crearDAO(em);

        List<TipoDescuento> lista = List.of(
                new TipoDescuento(id, "Descuento estudiante")
        );

        jakarta.persistence.TypedQuery<TipoDescuento> query =
                mock(jakarta.persistence.TypedQuery.class);

        jakarta.persistence.criteria.CriteriaBuilder cb =
                mock(jakarta.persistence.criteria.CriteriaBuilder.class);

        jakarta.persistence.criteria.CriteriaQuery<TipoDescuento> cq =
                mock(jakarta.persistence.criteria.CriteriaQuery.class);

        jakarta.persistence.criteria.Root<TipoDescuento> root =
                mock(jakarta.persistence.criteria.Root.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(TipoDescuento.class)).thenReturn(cq);
        when(cq.from(TipoDescuento.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(query);
        when(query.setFirstResult(0)).thenReturn(query);
        when(query.setMaxResults(10)).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);

        List<TipoDescuento> resultado =
                dao.findRange(0, 10);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(query).setFirstResult(0);
        verify(query).setMaxResults(10);
    }

    @Test
    public void testContar() {
        EntityManager em = mock(EntityManager.class);
        TipoDescuentoDAO dao = crearDAO(em);

        jakarta.persistence.TypedQuery<Long> query =
                mock(jakarta.persistence.TypedQuery.class);

        jakarta.persistence.criteria.CriteriaBuilder cb =
                mock(jakarta.persistence.criteria.CriteriaBuilder.class);

        jakarta.persistence.criteria.CriteriaQuery<Long> cq =
                mock(jakarta.persistence.criteria.CriteriaQuery.class);

        jakarta.persistence.criteria.Root<TipoDescuento> root =
                mock(jakarta.persistence.criteria.Root.class);

        jakarta.persistence.criteria.Expression<Long> countExpression =
                mock(jakarta.persistence.criteria.Expression.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Long.class)).thenReturn(cq);
        when(cq.from(TipoDescuento.class)).thenReturn(root);
        when(cb.count(root)).thenReturn(countExpression);
        when(cq.select(countExpression)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(3L);

        int resultado = dao.contar();

        assertEquals(3, resultado);

        verify(query).getSingleResult();
    }
}

