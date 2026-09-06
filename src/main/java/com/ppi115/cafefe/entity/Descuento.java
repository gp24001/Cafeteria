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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author jazmi
 */
@Entity
@Table(name = "descuento", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Descuento.findAll", query = "SELECT d FROM Descuento d")})
public class Descuento implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_descuento", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idDescuento;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_inicio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicio;
    @Column(name = "fecha_fin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFin;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @JoinColumn(name = "id_tipo_descuento", referencedColumnName = "id_tipo_descuento")
    @ManyToOne(fetch = FetchType.LAZY)
    private TipoDescuento idTipoDescuento;
    @OneToMany(mappedBy = "idDescuento", fetch = FetchType.LAZY)
    private List<DescuentoProducto> descuentoProductoList;

    public Descuento() {
    }

    public Descuento(UUID idDescuento) {
        this.idDescuento = idDescuento;
    }

    public Descuento(UUID idDescuento, String nombre, Date fechaInicio) {
        this.idDescuento = idDescuento;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
    }

    public UUID getIdDescuento() {
        return idDescuento;
    }

    public void setIdDescuento(UUID idDescuento) {
        this.idDescuento = idDescuento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public TipoDescuento getIdTipoDescuento() {
        return idTipoDescuento;
    }

    public void setIdTipoDescuento(TipoDescuento idTipoDescuento) {
        this.idTipoDescuento = idTipoDescuento;
    }

    public List<DescuentoProducto> getDescuentoProductoList() {
        return descuentoProductoList;
    }

    public void setDescuentoProductoList(List<DescuentoProducto> descuentoProductoList) {
        this.descuentoProductoList = descuentoProductoList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idDescuento != null ? idDescuento.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Descuento)) {
            return false;
        }
        Descuento other = (Descuento) object;
        if ((this.idDescuento == null && other.idDescuento != null) || (this.idDescuento != null && !this.idDescuento.equals(other.idDescuento))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.Descuento[ idDescuento=" + idDescuento + " ]";
    }
    
}
