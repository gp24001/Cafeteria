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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que representa línea de producto de una orden y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa línea de producto de una orden y sus relaciones en la cafetería. */
@Entity
@Table(name = "orden_producto", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "OrdenProducto.findAll", query = "SELECT o FROM OrdenProducto o")})
public class OrdenProducto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_orden_producto", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idOrdenProducto;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "precio", precision = 8, scale = 2)
    private BigDecimal precio;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @OneToMany(mappedBy = "idOrdenProducto", fetch = FetchType.LAZY)
    private List<FacturaOrdenProducto> facturaOrdenProductoList;
    @JoinColumn(name = "id_orden", referencedColumnName = "id_orden")
    @ManyToOne(fetch = FetchType.LAZY)
    private Orden idOrden;
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private Producto idProducto;

    /**
     * Inicializa OrdenProducto con los datos recibidos.
     */
    public OrdenProducto() {
    }

    /**
     * Inicializa OrdenProducto con los datos recibidos.
     * @param idOrdenProducto id orden producto utilizado por la operación

     */
    public OrdenProducto(UUID idOrdenProducto) {
        this.idOrdenProducto = idOrdenProducto;
    }

    /**
     * Obtiene id orden producto para la vista o la operación actual.
     * @return id orden producto actual
     */
    public UUID getIdOrdenProducto() {
        return idOrdenProducto;
    }

    /**
     * Actualiza id orden producto con el valor recibido.
     * @param idOrdenProducto id orden producto utilizado por la operación
     */
    public void setIdOrdenProducto(UUID idOrdenProducto) {
        this.idOrdenProducto = idOrdenProducto;
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
     * Obtiene factura orden producto list para la vista o la operación actual.
     * @return factura orden producto list actual
     */
    public List<FacturaOrdenProducto> getFacturaOrdenProductoList() {
        return facturaOrdenProductoList;
    }

    /**
     * Actualiza factura orden producto list con el valor recibido.
     * @param facturaOrdenProductoList factura orden producto list utilizado por la operación
     */
    public void setFacturaOrdenProductoList(List<FacturaOrdenProducto> facturaOrdenProductoList) {
        this.facturaOrdenProductoList = facturaOrdenProductoList;
    }

    /**
     * Obtiene id orden para la vista o la operación actual.
     * @return id orden actual
     */
    public Orden getIdOrden() {
        return idOrden;
    }

    /**
     * Actualiza id orden con el valor recibido.
     * @param idOrden identificador de la orden
     */
    public void setIdOrden(Orden idOrden) {
        this.idOrden = idOrden;
    }

    /**
     * Obtiene id producto para la vista o la operación actual.
     * @return id producto actual
     */
    public Producto getIdProducto() {
        return idProducto;
    }

    /**
     * Actualiza id producto con el valor recibido.
     * @param idProducto identificador del producto
     */
    public void setIdProducto(Producto idProducto) {
        this.idProducto = idProducto;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOrdenProducto != null ? idOrdenProducto.hashCode() : 0);
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
        if (!(object instanceof OrdenProducto)) {
            return false;
        }
        OrdenProducto other = (OrdenProducto) object;
        if ((this.idOrdenProducto == null && other.idOrdenProducto != null) || (this.idOrdenProducto != null && !this.idOrdenProducto.equals(other.idOrdenProducto))) {
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
        return "com.ppi115.cafefe.entity.OrdenProducto[ idOrdenProducto=" + idOrdenProducto + " ]";
    }
    
}
