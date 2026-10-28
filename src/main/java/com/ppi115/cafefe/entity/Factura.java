/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ppi115.cafefe.entity;

import com.ppi115.cafefe.control.UUIDConverter;
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
 * Entidad JPA que representa factura y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa factura y sus relaciones en la cafetería. */
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

    /**
     * Inicializa Factura con los datos recibidos.
     */
    public Factura() {
    }

    /**
     * Inicializa Factura con los datos recibidos.
     * @param idFactura id factura utilizado por la operación

     */
    public Factura(UUID idFactura) {
        this.idFactura = idFactura;
    }

    /**
     * Obtiene id factura para la vista o la operación actual.
     * @return id factura actual
     */
    public UUID getIdFactura() {
        return idFactura;
    }

    /**
     * Actualiza id factura con el valor recibido.
     * @param idFactura id factura utilizado por la operación
     */
    public void setIdFactura(UUID idFactura) {
        this.idFactura = idFactura;
    }

    /**
     * Obtiene cliente para la vista o la operación actual.
     * @return cliente actual
     */
    public String getCliente() {
        return cliente;
    }

    /**
     * Actualiza cliente con el valor recibido.
     * @param cliente cliente utilizado por la operación
     */
    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    /**
     * Obtiene fecha facturacion para la vista o la operación actual.
     * @return fecha facturacion actual
     */
    public Date getFechaFacturacion() {
        return fechaFacturacion;
    }

    /**
     * Actualiza fecha facturacion con el valor recibido.
     * @param fechaFacturacion fecha facturacion utilizado por la operación
     */
    public void setFechaFacturacion(Date fechaFacturacion) {
        this.fechaFacturacion = fechaFacturacion;
    }

    /**
     * Obtiene estado para la vista o la operación actual.
     * @return estado actual
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Actualiza estado con el valor recibido.
     * @param estado estado utilizado por la operación
     */
    public void setEstado(String estado) {
        this.estado = estado;
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
     * Obtiene pago list para la vista o la operación actual.
     * @return pago list actual
     */
    public List<Pago> getPagoList() {
        return pagoList;
    }

    /**
     * Actualiza pago list con el valor recibido.
     * @param pagoList pago list utilizado por la operación
     */
    public void setPagoList(List<Pago> pagoList) {
        this.pagoList = pagoList;
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
     * Obtiene id caja para la vista o la operación actual.
     * @return id caja actual
     */
    public Caja getIdCaja() {
        return idCaja;
    }

    /**
     * Actualiza id caja con el valor recibido.
     * @param idCaja id caja utilizado por la operación
     */
    public void setIdCaja(Caja idCaja) {
        this.idCaja = idCaja;
    }

    /**
     * Obtiene id empleado rol para la vista o la operación actual.
     * @return id empleado rol actual
     */
    public EmpleadoRol getIdEmpleadoRol() {
        return idEmpleadoRol;
    }

    /**
     * Actualiza id empleado rol con el valor recibido.
     * @param idEmpleadoRol id empleado rol utilizado por la operación
     */
    public void setIdEmpleadoRol(EmpleadoRol idEmpleadoRol) {
        this.idEmpleadoRol = idEmpleadoRol;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idFactura != null ? idFactura.hashCode() : 0);
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
        if (!(object instanceof Factura)) {
            return false;
        }
        Factura other = (Factura) object;
        if ((this.idFactura == null && other.idFactura != null) || (this.idFactura != null && !this.idFactura.equals(other.idFactura))) {
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
        return "com.ppi115.cafefe.entity.Factura[ idFactura=" + idFactura + " ]";
    }
    
}
