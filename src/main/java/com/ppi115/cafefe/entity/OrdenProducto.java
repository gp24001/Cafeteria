/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ppi115.cafefe.entity;

import com.ppi115.cafefe.UUIDConverter;
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
 *
 * @author jazmi
 */
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

    public OrdenProducto() {
    }

    public OrdenProducto(UUID idOrdenProducto) {
        this.idOrdenProducto = idOrdenProducto;
    }

    public UUID getIdOrdenProducto() {
        return idOrdenProducto;
    }

    public void setIdOrdenProducto(UUID idOrdenProducto) {
        this.idOrdenProducto = idOrdenProducto;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<FacturaOrdenProducto> getFacturaOrdenProductoList() {
        return facturaOrdenProductoList;
    }

    public void setFacturaOrdenProductoList(List<FacturaOrdenProducto> facturaOrdenProductoList) {
        this.facturaOrdenProductoList = facturaOrdenProductoList;
    }

    public Orden getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(Orden idOrden) {
        this.idOrden = idOrden;
    }

    public Producto getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Producto idProducto) {
        this.idProducto = idProducto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOrdenProducto != null ? idOrdenProducto.hashCode() : 0);
        return hash;
    }

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

    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.OrdenProducto[ idOrdenProducto=" + idOrdenProducto + " ]";
    }
    
}
