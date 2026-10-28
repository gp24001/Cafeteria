package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.Caracteristica;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Acceso a datos de característica configurable. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@jakarta.ejb.LocalBean
public class CaracteristicaDAO extends DefaultDAO<Caracteristica> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa CaracteristicaDAO con los datos recibidos.
     */
    public CaracteristicaDAO() {
        super(Caracteristica.class);
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
    public java.util.List<Caracteristica> findAll() {
        try {
            return em.createQuery("SELECT c FROM Caracteristica c LEFT JOIN FETCH c.idTipoCaracteristica",
                    Caracteristica.class).getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }

    /**
     * Comprueba las restricciones y las reglas de la entidad antes de escribir en la base de datos.
     * @param r registro que se desea validar o editar
     */
    @Override
    protected void validar(Caracteristica r) {
        super.validar(r);
        // Las relaciones se eligen al crear; al editar se conservan las ya persistidas.
        Caracteristica anterior = em.find(Caracteristica.class, r.getIdCaracteristica());
        if (anterior != null && !java.util.Objects.equals(
                anterior.getIdTipoCaracteristica() == null ? null : anterior.getIdTipoCaracteristica().getIdTipoCaracteristica(),
                r.getIdTipoCaracteristica() == null ? null : r.getIdTipoCaracteristica().getIdTipoCaracteristica()))
            throw new DatosInvalidosException("Las relaciones no pueden cambiarse durante la edición");

        if (r.getNombre() == null || r.getNombre().isBlank())
            throw new DatosInvalidosException("El nombre es obligatorio");
        var tipo = exigirReferencia(com.ppi115.cafefe.entity.TipoCaracteristica.class,
                r.getIdTipoCaracteristica() == null ? null : r.getIdTipoCaracteristica().getIdTipoCaracteristica(), "El tipo de característica");
        if (!Boolean.TRUE.equals(tipo.getActivo())) throw new DatosInvalidosException("El tipo de característica está inactivo");
        r.setIdTipoCaracteristica(tipo);
    }

    /**
     * Recupera el registro y su tipo para los formularios fuera de la transacción.
     * @param id identificador obligatorio del catálogo
     * @return registro encontrado, o null si no existe
     */
    @Override
    public Caracteristica findById(Object id) {
        if (id == null) throw new DatosInvalidosException("El identificador es obligatorio");
        try {
            var registros = em.createQuery("SELECT r FROM Caracteristica r LEFT JOIN FETCH r.idTipoCaracteristica WHERE r.idCaracteristica = :id",
                    Caracteristica.class).setParameter("id", id).getResultList();
            return registros.isEmpty() ? null : registros.get(0);
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar el registro seleccionado", e);
        }
    }
}
