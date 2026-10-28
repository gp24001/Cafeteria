package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.ProductoTipoProducto;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

/** Acceso a datos de asignación de categoría a producto. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class ProductoTipoProductoDAO extends DefaultDAO<ProductoTipoProducto> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa ProductoTipoProductoDAO con los datos recibidos.
     */
    public ProductoTipoProductoDAO() {
        super(ProductoTipoProducto.class);
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
     * Comprueba las restricciones y las reglas de la entidad antes de escribir en la base de datos.
     * @param r registro que se desea validar o editar
     */
    @Override
    protected void validar(ProductoTipoProducto r) {
        super.validar(r);
        // Las relaciones se eligen al crear; al editar se conservan las ya persistidas.
        ProductoTipoProducto anterior = em.find(ProductoTipoProducto.class, r.getIdProductoTipoProducto());
        if (anterior != null && !java.util.Objects.equals(
                anterior.getIdProducto() == null ? null : anterior.getIdProducto().getIdProducto(),
                r.getIdProducto() == null ? null : r.getIdProducto().getIdProducto()))
            throw new DatosInvalidosException("Las relaciones no pueden cambiarse durante la edición");
        if (anterior != null && !java.util.Objects.equals(
                anterior.getIdTipoProducto() == null ? null : anterior.getIdTipoProducto().getIdTipoProducto(),
                r.getIdTipoProducto() == null ? null : r.getIdTipoProducto().getIdTipoProducto()))
            throw new DatosInvalidosException("Las relaciones no pueden cambiarse durante la edición");


        var producto = ProductoDAO.exigirActivo(getEntityManager(), r.getIdProducto());
        var tipo = exigirReferencia(com.ppi115.cafefe.entity.TipoProducto.class,
                r.getIdTipoProducto() == null ? null : r.getIdTipoProducto().getIdTipoProducto(), "El tipo de producto");
        if (!Boolean.TRUE.equals(tipo.getActivo())) throw new DatosInvalidosException("El tipo de producto está inactivo");
        if (r.getActivo() == null || r.getFechaCreacion() == null)
            throw new DatosInvalidosException("El estado y la fecha de creación son obligatorios");
        if (findByIdProducto(producto.getIdProducto()).stream().anyMatch(a ->
                a.getIdTipoProducto().getIdTipoProducto().equals(tipo.getIdTipoProducto())
                && !a.getIdProductoTipoProducto().equals(r.getIdProductoTipoProducto())))
            throw new DatosInvalidosException("Este tipo ya está asignado al producto");
        r.setIdProducto(producto);
        r.setIdTipoProducto(tipo);
    }

    /**
     * Consulta únicamente las asignaciones pertenecientes al producto indicado.
     * @param idProducto identificador del producto
     * @return lista de registros que cumplen la consulta
     */
    public List<ProductoTipoProducto> findByIdProducto(UUID idProducto) {
        try {
            if (idProducto == null) throw new DatosInvalidosException("El ID del producto es obligatorio");
            return em.createQuery(
                    "SELECT ptp FROM ProductoTipoProducto ptp JOIN FETCH ptp.idTipoProducto JOIN FETCH ptp.idProducto "
                    + "WHERE ptp.idProducto.idProducto = :idProducto "
                    ,
                    ProductoTipoProducto.class)
                    .setParameter("idProducto", idProducto)
                    .getResultList();
        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }
}
