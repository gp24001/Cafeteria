package com.ppi115.cafefe.entity;

import com.ppi115.cafefe.control.UUIDConverter;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** Entidad JPA que representa detalle de pago y sus relaciones en la cafetería. */
@Entity
@Table(name = "pago_detalle", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "PagoDetalle.findAll", query = "SELECT p FROM PagoDetalle p")})
public class PagoDetalle implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_pago_detalle", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idPagoDetalle;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "monto", precision = 8, scale = 2)
    private BigDecimal monto;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @Size(max = 2147483647)
    @Column(name = "referencia_externa", length = 2147483647)
    private String referenciaExterna;
    @JoinColumn(name = "id_pago", referencedColumnName = "id_pago", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Pago idPago;

    /**
     * Inicializa PagoDetalle con los datos recibidos.
     */
    public PagoDetalle() {
    }

    /**
     * Inicializa PagoDetalle con los datos recibidos.
     * @param idPagoDetalle id pago detalle utilizado por la operación

     */
    public PagoDetalle(UUID idPagoDetalle) {
        this.idPagoDetalle = idPagoDetalle;
    }

    /**
     * Obtiene id pago detalle para la vista o la operación actual.
     * @return id pago detalle actual
     */
    public UUID getIdPagoDetalle() {
        return idPagoDetalle;
    }

    /**
     * Actualiza id pago detalle con el valor recibido.
     * @param idPagoDetalle id pago detalle utilizado por la operación
     */
    public void setIdPagoDetalle(UUID idPagoDetalle) {
        this.idPagoDetalle = idPagoDetalle;
    }

    /**
     * Obtiene monto para la vista o la operación actual.
     * @return monto actual
     */
    public BigDecimal getMonto() {
        return monto;
    }

    /**
     * Actualiza monto con el valor recibido.
     * @param monto monto utilizado por la operación
     */
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    /**
     * Obtiene observaciones para la vista o la operación actual.
     * @return observaciones actual
     */
    public String getObservaciones() {
        return observaciones;
    }

    /**
     * Actualiza observaciones con el valor recibido.
     * @param observaciones observaciones utilizado por la operación
     */
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /**
     * Obtiene referencia externa para la vista o la operación actual.
     * @return referencia externa actual
     */
    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    /**
     * Actualiza referencia externa con el valor recibido.
     * @param referenciaExterna referencia externa utilizado por la operación
     */
    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    /**
     * Obtiene id pago para la vista o la operación actual.
     * @return id pago actual
     */
    public Pago getIdPago() {
        return idPago;
    }

    /**
     * Actualiza id pago con el valor recibido.
     * @param idPago id pago utilizado por la operación
     */
    public void setIdPago(Pago idPago) {
        this.idPago = idPago;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPagoDetalle != null ? idPagoDetalle.hashCode() : 0);
        return hash;
    }

    /**
     * Compara la identidad persistente de dos entidades mediante su identificador.
     * @param object object utilizado por la operación
     * @return true cuando se cumple la condición comprobada
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PagoDetalle)) {
            return false;
        }
        PagoDetalle other = (PagoDetalle) object;
        if ((this.idPagoDetalle == null && other.idPagoDetalle != null) || (this.idPagoDetalle != null && !this.idPagoDetalle.equals(other.idPagoDetalle))) {
            return false;
        }
        return true;
    }

    /**
     * Devuelve una representación textual que permite identificar la entidad.
     * @return texto con el tipo y el identificador de la entidad
     */
    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.PagoDetalle[ idPagoDetalle=" + idPagoDetalle + " ]";
    }
    
}
