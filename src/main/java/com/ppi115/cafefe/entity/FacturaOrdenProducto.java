/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

/**
 * Entidad JPA que representa detalle facturado de una orden y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa detalle facturado de una orden y sus relaciones en la cafetería. */
@Entity
@Table(name = "factura_orden_producto", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "FacturaOrdenProducto.findAll", query = "SELECT f FROM FacturaOrdenProducto f")})
public class FacturaOrdenProducto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_factura_orden_producto", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idFacturaOrdenProducto;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "precio", precision = 8, scale = 2)
    private BigDecimal precio;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @JoinColumn(name = "id_factura", referencedColumnName = "id_factura")
    @ManyToOne(fetch = FetchType.LAZY)
    private Factura idFactura;
    @JoinColumn(name = "id_orden_producto", referencedColumnName = "id_orden_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private OrdenProducto idOrdenProducto;

    /**
     * Inicializa FacturaOrdenProducto con los datos recibidos.
     */
    public FacturaOrdenProducto() {
    }

    /**
     * Inicializa FacturaOrdenProducto con los datos recibidos.
     * @param idFacturaOrdenProducto id factura orden producto utilizado por la operación

     */
    public FacturaOrdenProducto(UUID idFacturaOrdenProducto) {
        this.idFacturaOrdenProducto = idFacturaOrdenProducto;
    }

    /**
     * Obtiene id factura orden producto para la vista o la operación actual.
     * @return id factura orden producto actual
     */
    public UUID getIdFacturaOrdenProducto() {
        return idFacturaOrdenProducto;
    }

    /**
     * Actualiza id factura orden producto con el valor recibido.
     * @param idFacturaOrdenProducto id factura orden producto utilizado por la operación
     */
    public void setIdFacturaOrdenProducto(UUID idFacturaOrdenProducto) {
        this.idFacturaOrdenProducto = idFacturaOrdenProducto;
    }

    /**
     * Obtiene precio para la vista o la operación actual.
     * @return precio actual
     */
    public BigDecimal getPrecio() {
        return precio;
    }

    /**
     * Actualiza precio con el valor recibido.
     * @param precio precio utilizado por la operación
     */
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
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
     * Obtiene id factura para la vista o la operación actual.
     * @return id factura actual
     */
    public Factura getIdFactura() {
        return idFactura;
    }

    /**
     * Actualiza id factura con el valor recibido.
     * @param idFactura id factura utilizado por la operación
     */
    public void setIdFactura(Factura idFactura) {
        this.idFactura = idFactura;
    }

    /**
     * Obtiene id orden producto para la vista o la operación actual.
     * @return id orden producto actual
     */
    public OrdenProducto getIdOrdenProducto() {
        return idOrdenProducto;
    }

    /**
     * Actualiza id orden producto con el valor recibido.
     * @param idOrdenProducto id orden producto utilizado por la operación
     */
    public void setIdOrdenProducto(OrdenProducto idOrdenProducto) {
        this.idOrdenProducto = idOrdenProducto;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idFacturaOrdenProducto != null ? idFacturaOrdenProducto.hashCode() : 0);
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
        if (!(object instanceof FacturaOrdenProducto)) {
            return false;
        }
        FacturaOrdenProducto other = (FacturaOrdenProducto) object;
        if ((this.idFacturaOrdenProducto == null && other.idFacturaOrdenProducto != null) || (this.idFacturaOrdenProducto != null && !this.idFacturaOrdenProducto.equals(other.idFacturaOrdenProducto))) {
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
        return "com.ppi115.cafefe.entity.FacturaOrdenProducto[ idFacturaOrdenProducto=" + idFacturaOrdenProducto + " ]";
    }
    
}
