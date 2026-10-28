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
 * Entidad JPA que representa descuento asignado a un producto y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa descuento asignado a un producto y sus relaciones en la cafetería. */
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

    /**
     * Inicializa DescuentoProducto con los datos recibidos.
     */
    public DescuentoProducto() {
    }

    /**
     * Inicializa DescuentoProducto con los datos recibidos.
     * @param idDescuentoProducto id descuento producto utilizado por la operación

     */
    public DescuentoProducto(UUID idDescuentoProducto) {
        this.idDescuentoProducto = idDescuentoProducto;
    }

    /**
     * Inicializa DescuentoProducto con los datos recibidos.
     * @param idDescuentoProducto id descuento producto utilizado por la operación
     * @param fechaInicio fecha inicio utilizado por la operación

     */
    public DescuentoProducto(UUID idDescuentoProducto, Date fechaInicio) {
        this.idDescuentoProducto = idDescuentoProducto;
        this.fechaInicio = fechaInicio;
    }

    /**
     * Obtiene id descuento producto para la vista o la operación actual.
     * @return id descuento producto actual
     */
    public UUID getIdDescuentoProducto() {
        return idDescuentoProducto;
    }

    /**
     * Actualiza id descuento producto con el valor recibido.
     * @param idDescuentoProducto id descuento producto utilizado por la operación
     */
    public void setIdDescuentoProducto(UUID idDescuentoProducto) {
        this.idDescuentoProducto = idDescuentoProducto;
    }

    /**
     * Obtiene fecha inicio para la vista o la operación actual.
     * @return fecha inicio actual
     */
    public Date getFechaInicio() {
        return fechaInicio;
    }

    /**
     * Actualiza fecha inicio con el valor recibido.
     * @param fechaInicio fecha inicio utilizado por la operación
     */
    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    /**
     * Obtiene fecha fin para la vista o la operación actual.
     * @return fecha fin actual
     */
    public Date getFechaFin() {
        return fechaFin;
    }

    /**
     * Actualiza fecha fin con el valor recibido.
     * @param fechaFin fecha fin utilizado por la operación
     */
    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    /**
     * Obtiene valor para la vista o la operación actual.
     * @return valor actual
     */
    public Integer getValor() {
        return valor;
    }

    /**
     * Actualiza valor con el valor recibido.
     * @param valor nuevo valor que se asignará
     */
    public void setValor(Integer valor) {
        this.valor = valor;
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
     * Obtiene id descuento para la vista o la operación actual.
     * @return id descuento actual
     */
    public Descuento getIdDescuento() {
        return idDescuento;
    }

    /**
     * Actualiza id descuento con el valor recibido.
     * @param idDescuento id descuento utilizado por la operación
     */
    public void setIdDescuento(Descuento idDescuento) {
        this.idDescuento = idDescuento;
    }

    /**
     * Obtiene id producto para la vista o la operación actual.
     * @return id producto actual
     */
    public Producto getIdProducto() {
        return idProducto;
    }

    /**
     * Actualiza id producto con el valor recibido.
     * @param idProducto identificador del producto
     */
    public void setIdProducto(Producto idProducto) {
        this.idProducto = idProducto;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idDescuentoProducto != null ? idDescuentoProducto.hashCode() : 0);
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
        if (!(object instanceof DescuentoProducto)) {
            return false;
        }
        DescuentoProducto other = (DescuentoProducto) object;
        if ((this.idDescuentoProducto == null && other.idDescuentoProducto != null) || (this.idDescuentoProducto != null && !this.idDescuentoProducto.equals(other.idDescuentoProducto))) {
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
        return "com.ppi115.cafefe.entity.DescuentoProducto[ idDescuentoProducto=" + idDescuentoProducto + " ]";
    }
    
}
