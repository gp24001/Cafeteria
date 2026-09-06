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
@Table(name = "tipo_pago", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "TipoPago.findAll", query = "SELECT t FROM TipoPago t")})
public class TipoPago implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_tipo_pago", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idTipoPago;
    @Size(max = 150)
    @Column(name = "nombre", length = 150)
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idTipoPago", fetch = FetchType.LAZY)
    private List<PagoDetalle> pagoDetalleList;

    public TipoPago() {
    }

    public TipoPago(UUID idTipoPago) {
        this.idTipoPago = idTipoPago;
    }

    public UUID getIdTipoPago() {
        return idTipoPago;
    }

    public void setIdTipoPago(UUID idTipoPago) {
        this.idTipoPago = idTipoPago;
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

    public List<PagoDetalle> getPagoDetalleList() {
        return pagoDetalleList;
    }

    public void setPagoDetalleList(List<PagoDetalle> pagoDetalleList) {
        this.pagoDetalleList = pagoDetalleList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipoPago != null ? idTipoPago.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipoPago)) {
            return false;
        }
        TipoPago other = (TipoPago) object;
        if ((this.idTipoPago == null && other.idTipoPago != null) || (this.idTipoPago != null && !this.idTipoPago.equals(other.idTipoPago))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.TipoPago[ idTipoPago=" + idTipoPago + " ]";
    }
    
}
