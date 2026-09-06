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
@Table(name = "orden", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Orden.findAll", query = "SELECT o FROM Orden o")})
public class Orden implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_orden", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idOrden;
    @Column(name = "fecha_orden")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaOrden;
    @Size(max = 2147483647)
    @Column(name = "observacion", length = 2147483647)
    private String observacion;
    @OneToMany(mappedBy = "idOrden", fetch = FetchType.LAZY)
    private List<OrdenProducto> ordenProductoList;
    @JoinColumn(name = "id_empleado_rol", referencedColumnName = "id_empleado_rol", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private EmpleadoRol idEmpleadoRol;
    @JoinColumn(name = "id_tipo_estado_orden", referencedColumnName = "id_tipo_estado_orden", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TipoEstadoOrden idTipoEstadoOrden;

    public Orden() {
    }

    public Orden(UUID idOrden) {
        this.idOrden = idOrden;
    }

    public UUID getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(UUID idOrden) {
        this.idOrden = idOrden;
    }

    public Date getFechaOrden() {
        return fechaOrden;
    }

    public void setFechaOrden(Date fechaOrden) {
        this.fechaOrden = fechaOrden;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public List<OrdenProducto> getOrdenProductoList() {
        return ordenProductoList;
    }

    public void setOrdenProductoList(List<OrdenProducto> ordenProductoList) {
        this.ordenProductoList = ordenProductoList;
    }

    public EmpleadoRol getIdEmpleadoRol() {
        return idEmpleadoRol;
    }

    public void setIdEmpleadoRol(EmpleadoRol idEmpleadoRol) {
        this.idEmpleadoRol = idEmpleadoRol;
    }

    public TipoEstadoOrden getIdTipoEstadoOrden() {
        return idTipoEstadoOrden;
    }

    public void setIdTipoEstadoOrden(TipoEstadoOrden idTipoEstadoOrden) {
        this.idTipoEstadoOrden = idTipoEstadoOrden;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOrden != null ? idOrden.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Orden)) {
            return false;
        }
        Orden other = (Orden) object;
        if ((this.idOrden == null && other.idOrden != null) || (this.idOrden != null && !this.idOrden.equals(other.idOrden))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.Orden[ idOrden=" + idOrden + " ]";
    }
    
}
