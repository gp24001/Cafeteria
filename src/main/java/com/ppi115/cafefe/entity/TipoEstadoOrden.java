/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ppi115.cafefe.entity;

import com.ppi115.cafefe.UUIDConverter;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
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
@Table(name = "tipo_estado_orden", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "TipoEstadoOrden.findAll", query = "SELECT t FROM TipoEstadoOrden t")})
public class TipoEstadoOrden implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_tipo_estado_orden", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idTipoEstadoOrden;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idTipoEstadoOrden", fetch = FetchType.LAZY)
    private List<Orden> ordenList;

    public TipoEstadoOrden() {
    }

    public TipoEstadoOrden(UUID idTipoEstadoOrden) {
        this.idTipoEstadoOrden = idTipoEstadoOrden;
    }

    public TipoEstadoOrden(UUID idTipoEstadoOrden, String nombre) {
        this.idTipoEstadoOrden = idTipoEstadoOrden;
        this.nombre = nombre;
    }

    public UUID getIdTipoEstadoOrden() {
        return idTipoEstadoOrden;
    }

    public void setIdTipoEstadoOrden(UUID idTipoEstadoOrden) {
        this.idTipoEstadoOrden = idTipoEstadoOrden;
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

    public List<Orden> getOrdenList() {
        return ordenList;
    }

    public void setOrdenList(List<Orden> ordenList) {
        this.ordenList = ordenList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipoEstadoOrden != null ? idTipoEstadoOrden.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipoEstadoOrden)) {
            return false;
        }
        TipoEstadoOrden other = (TipoEstadoOrden) object;
        if ((this.idTipoEstadoOrden == null && other.idTipoEstadoOrden != null) || (this.idTipoEstadoOrden != null && !this.idTipoEstadoOrden.equals(other.idTipoEstadoOrden))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.TipoEstadoOrden[ idTipoEstadoOrden=" + idTipoEstadoOrden + " ]";
    }
    
}
