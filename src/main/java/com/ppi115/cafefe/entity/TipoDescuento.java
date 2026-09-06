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
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author jazmi
 */
@Entity
@Table(name = "tipo_descuento", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "TipoDescuento.findAll", query = "SELECT t FROM TipoDescuento t")})
public class TipoDescuento implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_tipo_descuento", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idTipoDescuento;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @Column(name = "descuento_maximo")
    private Integer descuentoMaximo;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @OneToMany(mappedBy = "idTipoDescuento", fetch = FetchType.LAZY)
    private List<Descuento> descuentoList;

    public TipoDescuento() {
    }

    public TipoDescuento(UUID idTipoDescuento) {
        this.idTipoDescuento = idTipoDescuento;
    }

    public TipoDescuento(UUID idTipoDescuento, String nombre) {
        this.idTipoDescuento = idTipoDescuento;
        this.nombre = nombre;
    }

    public UUID getIdTipoDescuento() {
        return idTipoDescuento;
    }

    public void setIdTipoDescuento(UUID idTipoDescuento) {
        this.idTipoDescuento = idTipoDescuento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Integer getDescuentoMaximo() {
        return descuentoMaximo;
    }

    public void setDescuentoMaximo(Integer descuentoMaximo) {
        this.descuentoMaximo = descuentoMaximo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<Descuento> getDescuentoList() {
        return descuentoList;
    }

    public void setDescuentoList(List<Descuento> descuentoList) {
        this.descuentoList = descuentoList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipoDescuento != null ? idTipoDescuento.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipoDescuento)) {
            return false;
        }
        TipoDescuento other = (TipoDescuento) object;
        if ((this.idTipoDescuento == null && other.idTipoDescuento != null) || (this.idTipoDescuento != null && !this.idTipoDescuento.equals(other.idTipoDescuento))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.TipoDescuento[ idTipoDescuento=" + idTipoDescuento + " ]";
    }
    
}
