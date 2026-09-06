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
@Table(name = "tipo_caracteristica", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "TipoCaracteristica.findAll", query = "SELECT t FROM TipoCaracteristica t")})
public class TipoCaracteristica implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_tipo_caracteristica", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idTipoCaracteristica;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;
    @Size(max = 2147483647)
    @Column(name = "expresion_regular", length = 2147483647)
    private String expresionRegular;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @OneToMany(mappedBy = "idTipoCaracteristica", fetch = FetchType.LAZY)
    private List<Caracteristica> caracteristicaList;

    public TipoCaracteristica() {
    }

    public TipoCaracteristica(UUID idTipoCaracteristica) {
        this.idTipoCaracteristica = idTipoCaracteristica;
    }

    public TipoCaracteristica(UUID idTipoCaracteristica, String nombre) {
        this.idTipoCaracteristica = idTipoCaracteristica;
        this.nombre = nombre;
    }

    public UUID getIdTipoCaracteristica() {
        return idTipoCaracteristica;
    }

    public void setIdTipoCaracteristica(UUID idTipoCaracteristica) {
        this.idTipoCaracteristica = idTipoCaracteristica;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getExpresionRegular() {
        return expresionRegular;
    }

    public void setExpresionRegular(String expresionRegular) {
        this.expresionRegular = expresionRegular;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<Caracteristica> getCaracteristicaList() {
        return caracteristicaList;
    }

    public void setCaracteristicaList(List<Caracteristica> caracteristicaList) {
        this.caracteristicaList = caracteristicaList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipoCaracteristica != null ? idTipoCaracteristica.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipoCaracteristica)) {
            return false;
        }
        TipoCaracteristica other = (TipoCaracteristica) object;
        if ((this.idTipoCaracteristica == null && other.idTipoCaracteristica != null) || (this.idTipoCaracteristica != null && !this.idTipoCaracteristica.equals(other.idTipoCaracteristica))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.TipoCaracteristica[ idTipoCaracteristica=" + idTipoCaracteristica + " ]";
    }
    
}
