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
 * Entidad JPA que representa descuento y su vigencia y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa descuento y su vigencia y sus relaciones en la cafetería. */
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
    @NotNull(message = "Debe seleccionar un tipo de descuento")
    @JoinColumn(name = "id_tipo_descuento", referencedColumnName = "id_tipo_descuento")
    @ManyToOne(fetch = FetchType.LAZY)
    private TipoDescuento idTipoDescuento;
    @OneToMany(mappedBy = "idDescuento", fetch = FetchType.LAZY)
    private List<DescuentoProducto> descuentoProductoList;

    /**
     * Inicializa Descuento con los datos recibidos.
     */
    public Descuento() {
    }

    /**
     * Inicializa Descuento con los datos recibidos.
     * @param idDescuento id descuento utilizado por la operación

     */
    public Descuento(UUID idDescuento) {
        this.idDescuento = idDescuento;
    }

    /**
     * Inicializa Descuento con los datos recibidos.
     * @param idDescuento id descuento utilizado por la operación
     * @param nombre nombre usado para identificar el dato en los mensajes
     * @param fechaInicio fecha inicio utilizado por la operación

     */
    public Descuento(UUID idDescuento, String nombre, Date fechaInicio) {
        this.idDescuento = idDescuento;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
    }

    /**
     * Obtiene id descuento para la vista o la operación actual.
     * @return id descuento actual
     */
    public UUID getIdDescuento() {
        return idDescuento;
    }

    /**
     * Actualiza id descuento con el valor recibido.
     * @param idDescuento id descuento utilizado por la operación
     */
    public void setIdDescuento(UUID idDescuento) {
        this.idDescuento = idDescuento;
    }

    /**
     * Obtiene nombre para la vista o la operación actual.
     * @return nombre actual
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Actualiza nombre con el valor recibido.
     * @param nombre nombre usado para identificar el dato en los mensajes
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
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
     * Obtiene id tipo descuento para la vista o la operación actual.
     * @return id tipo descuento actual
     */
    public TipoDescuento getIdTipoDescuento() {
        return idTipoDescuento;
    }

    /**
     * Actualiza id tipo descuento con el valor recibido.
     * @param idTipoDescuento id tipo descuento utilizado por la operación
     */
    public void setIdTipoDescuento(TipoDescuento idTipoDescuento) {
        this.idTipoDescuento = idTipoDescuento;
    }

    /**
     * Obtiene descuento producto list para la vista o la operación actual.
     * @return descuento producto list actual
     */
    public List<DescuentoProducto> getDescuentoProductoList() {
        return descuentoProductoList;
    }

    /**
     * Actualiza descuento producto list con el valor recibido.
     * @param descuentoProductoList descuento producto list utilizado por la operación
     */
    public void setDescuentoProductoList(List<DescuentoProducto> descuentoProductoList) {
        this.descuentoProductoList = descuentoProductoList;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idDescuento != null ? idDescuento.hashCode() : 0);
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
        if (!(object instanceof Descuento)) {
            return false;
        }
        Descuento other = (Descuento) object;
        if ((this.idDescuento == null && other.idDescuento != null) || (this.idDescuento != null && !this.idDescuento.equals(other.idDescuento))) {
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
        return "com.ppi115.cafefe.entity.Descuento[ idDescuento=" + idDescuento + " ]";
    }
    
}
