package com.ppi115.cafefe.control;

import com.ppi115.cafefe.entity.TipoCaracteristica;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Acceso a datos de formato de características. Valida los datos antes de persistir y utiliza EntityManager para consultar sus referencias. */
@Stateless
@LocalBean
public class TipoCaracteristicaDAO extends DefaultDAO<TipoCaracteristica> {

    @PersistenceContext(unitName = "Cafefe-PU")
    private EntityManager em;

    /**
     * Inicializa TipoCaracteristicaDAO con los datos recibidos.
     */
    public TipoCaracteristicaDAO() {
        super(TipoCaracteristica.class);
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
    protected void validar(TipoCaracteristica r) {
        super.validar(r);
        if (r.getNombre() == null || r.getNombre().isBlank())
            throw new DatosInvalidosException("El nombre es obligatorio");
        if (r.getExpresionRegular() != null && !r.getExpresionRegular().isBlank()) {
            try { java.util.regex.Pattern.compile(r.getExpresionRegular()); }
            catch (java.util.regex.PatternSyntaxException e) {
                throw new DatosInvalidosException("La expresión regular no es válida", e);
            }
        }
    }
}
