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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author jazmi
 */
@Entity
@Table(name = "descuento_producto", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "DescuentoProducto.findAll", query = "SELECT d FROM DescuentoProducto d")})
public class DescuentoProducto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_descuento_producto", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idDescuentoProducto;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_inicio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicio;
    @Column(name = "fecha_fin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFin;
    @Column(name = "valor")
    private Integer valor;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @JoinColumn(name = "id_descuento", referencedColumnName = "id_descuento")
    @ManyToOne(fetch = FetchType.LAZY)
    private Descuento idDescuento;
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private Producto idProducto;

    public DescuentoProducto() {
    }

    public DescuentoProducto(UUID idDescuentoProducto) {
        this.idDescuentoProducto = idDescuentoProducto;
    }

    public DescuentoProducto(UUID idDescuentoProducto, Date fechaInicio) {
        this.idDescuentoProducto = idDescuentoProducto;
        this.fechaInicio = fechaInicio;
    }

    public UUID getIdDescuentoProducto() {
        return idDescuentoProducto;
    }

    public void setIdDescuentoProducto(UUID idDescuentoProducto) {
        this.idDescuentoProducto = idDescuentoProducto;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Descuento getIdDescuento() {
        return idDescuento;
    }

    public void setIdDescuento(Descuento idDescuento) {
        this.idDescuento = idDescuento;
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
        hash += (idDescuentoProducto != null ? idDescuentoProducto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DescuentoProducto)) {
            return false;
        }
        DescuentoProducto other = (DescuentoProducto) object;
        if ((this.idDescuentoProducto == null && other.idDescuentoProducto != null) || (this.idDescuentoProducto != null && !this.idDescuentoProducto.equals(other.idDescuentoProducto))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.DescuentoProducto[ idDescuentoProducto=" + idDescuentoProducto + " ]";
    }
    
}
