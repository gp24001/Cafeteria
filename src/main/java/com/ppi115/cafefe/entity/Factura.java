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
@Table(name = "factura", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Factura.findAll", query = "SELECT f FROM Factura f")})
public class Factura implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_factura", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idFactura;
    @Size(max = 200)
    @Column(name = "cliente", length = 200)
    private String cliente;
    @Column(name = "fecha_facturacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFacturacion;
    @Size(max = 200)
    @Column(name = "estado", length = 200)
    private String estado;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idFactura", fetch = FetchType.LAZY)
    private List<Pago> pagoList;
    @OneToMany(mappedBy = "idFactura", fetch = FetchType.LAZY)
    private List<FacturaOrdenProducto> facturaOrdenProductoList;
    @JoinColumn(name = "id_caja", referencedColumnName = "id_caja")
    @ManyToOne(fetch = FetchType.LAZY)
    private Caja idCaja;
    @JoinColumn(name = "id_empleado_rol", referencedColumnName = "id_empleado_rol")
    @ManyToOne(fetch = FetchType.LAZY)
    private EmpleadoRol idEmpleadoRol;

    public Factura() {
    }

    public Factura(UUID idFactura) {
        this.idFactura = idFactura;
    }

    public UUID getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(UUID idFactura) {
        this.idFactura = idFactura;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public Date getFechaFacturacion() {
        return fechaFacturacion;
    }

    public void setFechaFacturacion(Date fechaFacturacion) {
        this.fechaFacturacion = fechaFacturacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<Pago> getPagoList() {
        return pagoList;
    }

    public void setPagoList(List<Pago> pagoList) {
        this.pagoList = pagoList;
    }

    public List<FacturaOrdenProducto> getFacturaOrdenProductoList() {
        return facturaOrdenProductoList;
    }

    public void setFacturaOrdenProductoList(List<FacturaOrdenProducto> facturaOrdenProductoList) {
        this.facturaOrdenProductoList = facturaOrdenProductoList;
    }

    public Caja getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(Caja idCaja) {
        this.idCaja = idCaja;
    }

    public EmpleadoRol getIdEmpleadoRol() {
        return idEmpleadoRol;
    }

    public void setIdEmpleadoRol(EmpleadoRol idEmpleadoRol) {
        this.idEmpleadoRol = idEmpleadoRol;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idFactura != null ? idFactura.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Factura)) {
            return false;
        }
        Factura other = (Factura) object;
        if ((this.idFactura == null && other.idFactura != null) || (this.idFactura != null && !this.idFactura.equals(other.idFactura))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.Factura[ idFactura=" + idFactura + " ]";
    }
    
}
