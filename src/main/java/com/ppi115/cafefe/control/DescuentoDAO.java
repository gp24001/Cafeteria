package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Descuento;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Acceso a datos de descuento y su vigencia. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class DescuentoDAO extends DefaultDAO<Descuento> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa DescuentoDAO con los datos recibidos.
     */
    public DescuentoDAO() {
        super(Descuento.class);
    }

    /**
     * Proporciona el contexto de persistencia administrado por Jakarta EE.
     * @return entity manager actual
     */
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Consulta todos los registros del catálogo en la base de datos.
     * @return lista de registros que cumplen la consulta
     */
    @Override
    public java.util.List<Descuento> findAll() {
        try {
            return em.createQuery("SELECT d FROM Descuento d LEFT JOIN FETCH d.idTipoDescuento",
                    Descuento.class).getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Comprueba las restricciones y las reglas de la entidad antes de escribir en la base de datos.
     * @param r registro que se desea validar o editar
     */
    @Override
    protected void validar(Descuento r) {
        super.validar(r);
        // Las relaciones se eligen al crear; al editar se conservan las ya persistidas.
        Descuento anterior = em.find(Descuento.class, r.getIdDescuento());
        if (anterior != null && !java.util.Objects.equals(
                anterior.getIdTipoDescuento() == null ? null : anterior.getIdTipoDescuento().getIdTipoDescuento(),
                r.getIdTipoDescuento() == null ? null : r.getIdTipoDescuento().getIdTipoDescuento()))
            throw new DatosInvalidosException("Las relaciones no pueden cambiarse durante la edición");

        if (r.getNombre() == null || r.getNombre().isBlank())
            throw new DatosInvalidosException("El nombre es obligatorio");
        if (r.getFechaFin() != null && r.getFechaFin().before(r.getFechaInicio()))
            throw new DatosInvalidosException("La fecha fin no puede ser anterior al inicio");
        var tipo = exigirReferencia(com.ppi115.cafefe.entity.TipoDescuento.class,
                r.getIdTipoDescuento() == null ? null : r.getIdTipoDescuento().getIdTipoDescuento(), "El tipo de descuento");
        if (!Boolean.TRUE.equals(tipo.getActivo())) throw new DatosInvalidosException("El tipo de descuento está inactivo");
        r.setIdTipoDescuento(tipo);
    }

    /**
     * Recupera el registro y su tipo para los formularios fuera de la transacción.
     * @param id identificador obligatorio del catálogo
     * @return registro encontrado, o null si no existe
     */
    @Override
    public Descuento findById(Object id) {
        if (id == null) throw new DatosInvalidosException("El identificador es obligatorio");
        try {
            var registros = em.createQuery("SELECT r FROM Descuento r LEFT JOIN FETCH r.idTipoDescuento WHERE r.idDescuento = :id",
                    Descuento.class).setParameter("id", id).getResultList();
            return registros.isEmpty() ? null : registros.get(0);
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar el registro seleccionado", e);
        }
    }
}
