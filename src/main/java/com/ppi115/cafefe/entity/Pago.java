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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que representa pago y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa pago y sus relaciones en la cafetería. */
@Entity
@Table(name = "pago", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Pago.findAll", query = "SELECT p FROM Pago p")})
public class Pago implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_pago", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idPago;
    @Size(max = 200)
    @Column(name = "estado", length = 200)
    private String estado;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @JoinColumn(name = "id_factura", referencedColumnName = "id_factura", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Factura idFactura;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idPago", fetch = FetchType.LAZY)
    private List<PagoDetalle> pagoDetalleList;

    /**
     * Inicializa Pago con los datos recibidos.
     */
    public Pago() {
    }

    /**
     * Inicializa Pago con los datos recibidos.
     * @param idPago id pago utilizado por la operación

     */
    public Pago(UUID idPago) {
        this.idPago = idPago;
    }

    /**
     * Obtiene id pago para la vista o la operación actual.
     * @return id pago actual
     */
    public UUID getIdPago() {
        return idPago;
    }

    /**
     * Actualiza id pago con el valor recibido.
     * @param idPago id pago utilizado por la operación
     */
    public void setIdPago(UUID idPago) {
        this.idPago = idPago;
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
     * Obtiene id factura para la vista o la operación actual.
     * @return id factura actual
     */
    public Factura getIdFactura() {
        return idFactura;
    }

    /**
     * Actualiza id factura con el valor recibido.
     * @param idFactura id factura utilizado por la operación
     */
    public void setIdFactura(Factura idFactura) {
        this.idFactura = idFactura;
    }

    /**
     * Obtiene pago detalle list para la vista o la operación actual.
     * @return pago detalle list actual
     */
    public List<PagoDetalle> getPagoDetalleList() {
        return pagoDetalleList;
    }

    /**
     * Actualiza pago detalle list con el valor recibido.
     * @param pagoDetalleList pago detalle list utilizado por la operación
     */
    public void setPagoDetalleList(List<PagoDetalle> pagoDetalleList) {
        this.pagoDetalleList = pagoDetalleList;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPago != null ? idPago.hashCode() : 0);
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
        if (!(object instanceof Pago)) {
            return false;
        }
        Pago other = (Pago) object;
        if ((this.idPago == null && other.idPago != null) || (this.idPago != null && !this.idPago.equals(other.idPago))) {
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
        return "com.ppi115.cafefe.entity.Pago[ idPago=" + idPago + " ]";
    }
    
}
