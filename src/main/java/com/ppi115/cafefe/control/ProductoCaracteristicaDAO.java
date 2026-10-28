package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.ProductoCaracteristica;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

/** Acceso a datos de valor de una característica de producto. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class ProductoCaracteristicaDAO extends DefaultDAO<ProductoCaracteristica> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa ProductoCaracteristicaDAO con los datos recibidos.
     */
    public ProductoCaracteristicaDAO() {
        super(ProductoCaracteristica.class);
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
    protected void validar(ProductoCaracteristica r) {
        super.validar(r);
        // Las relaciones se eligen al crear; al editar se conservan las ya persistidas.
        ProductoCaracteristica anterior = em.find(ProductoCaracteristica.class, r.getIdProductoCaracteristica());
        if (anterior != null && !java.util.Objects.equals(
                anterior.getIdProducto() == null ? null : anterior.getIdProducto().getIdProducto(),
                r.getIdProducto() == null ? null : r.getIdProducto().getIdProducto()))
            throw new DatosInvalidosException("Las relaciones no pueden cambiarse durante la edición");
        if (anterior != null && !java.util.Objects.equals(
                anterior.getIdCaracteristica() == null ? null : anterior.getIdCaracteristica().getIdCaracteristica(),
                r.getIdCaracteristica() == null ? null : r.getIdCaracteristica().getIdCaracteristica()))
            throw new DatosInvalidosException("Las relaciones no pueden cambiarse durante la edición");


        var producto = ProductoDAO.exigirActivo(getEntityManager(), r.getIdProducto());
        var caracteristica = exigirReferencia(com.ppi115.cafefe.entity.Caracteristica.class,
                r.getIdCaracteristica() == null ? null : r.getIdCaracteristica().getIdCaracteristica(), "La característica");
        if (!Boolean.TRUE.equals(caracteristica.getActivo())) throw new DatosInvalidosException("La característica está inactiva");
        if (r.getValor() == null || r.getValor().isBlank()) throw new DatosInvalidosException("El valor es obligatorio");
        var tipo = caracteristica.getIdTipoCaracteristica();
        if (tipo != null && tipo.getExpresionRegular() != null && !tipo.getExpresionRegular().isBlank()) {
            try {
                if (!java.util.regex.Pattern.matches(tipo.getExpresionRegular(), r.getValor()))
                    throw new DatosInvalidosException("El valor no cumple el formato de la característica");
            } catch (java.util.regex.PatternSyntaxException e) {
                throw new DatosInvalidosException("La expresión regular configurada no es válida");
            }
        }
        if (findByIdProducto(producto.getIdProducto()).stream().anyMatch(a ->
                a.getIdCaracteristica().getIdCaracteristica().equals(caracteristica.getIdCaracteristica())
                && !a.getIdProductoCaracteristica().equals(r.getIdProductoCaracteristica())))
            throw new DatosInvalidosException("Esta característica ya está asignada al producto");
        r.setIdProducto(producto);
        r.setIdCaracteristica(caracteristica);
    }

    /**
     * Consulta únicamente las asignaciones pertenecientes al producto indicado.
     * @param idProducto identificador del producto
     * @return lista de registros que cumplen la consulta
     */
    public List<ProductoCaracteristica> findByIdProducto(UUID idProducto) {
        try {
        if (idProducto == null) throw new DatosInvalidosException("El ID del producto es obligatorio");
        return em.createQuery(
                "SELECT pc FROM ProductoCaracteristica pc JOIN FETCH pc.idProducto JOIN FETCH pc.idCaracteristica c LEFT JOIN FETCH c.idTipoCaracteristica "
                + "WHERE pc.idProducto.idProducto = :idProducto",
                ProductoCaracteristica.class)
                .setParameter("idProducto", idProducto)
                .getResultList();

        } catch (jakarta.persistence.PersistenceException e) {
            throw new DatosInvalidosException("No se pudo consultar la información solicitada", e);
        }
    }
}
