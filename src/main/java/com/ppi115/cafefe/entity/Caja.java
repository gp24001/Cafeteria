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
 * Entidad JPA que representa caja y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa caja y sus relaciones en la cafetería. */
@Entity
@Table(name = "caja", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Caja.findAll", query = "SELECT c FROM Caja c")})
public class Caja implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_caja", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idCaja;
    @Size(max = 150)
    @Column(name = "nombre", length = 150)
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @OneToMany(mappedBy = "idCaja", fetch = FetchType.LAZY)
    private List<Factura> facturaList;

    /**
     * Inicializa Caja con los datos recibidos.
     */
    public Caja() {
    }

    /**
     * Inicializa Caja con los datos recibidos.
     * @param idCaja id caja utilizado por la operación

     */
    public Caja(UUID idCaja) {
        this.idCaja = idCaja;
    }

    /**
     * Obtiene id caja para la vista o la operación actual.
     * @return id caja actual
     */
    public UUID getIdCaja() {
        return idCaja;
    }

    /**
     * Actualiza id caja con el valor recibido.
     * @param idCaja id caja utilizado por la operación
     */
    public void setIdCaja(UUID idCaja) {
        this.idCaja = idCaja;
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
     * Obtiene activo para la vista o la operación actual.
     * @return activo actual
     */
    public Boolean getActivo() {
        return activo;
    }

    /**
     * Actualiza activo con el valor recibido.
     * @param activo activo utilizado por la operación
     */
    public void setActivo(Boolean activo) {
        this.activo = activo;
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
     * Obtiene factura list para la vista o la operación actual.
     * @return factura list actual
     */
    public List<Factura> getFacturaList() {
        return facturaList;
    }

    /**
     * Actualiza factura list con el valor recibido.
     * @param facturaList factura list utilizado por la operación
     */
    public void setFacturaList(List<Factura> facturaList) {
        this.facturaList = facturaList;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idCaja != null ? idCaja.hashCode() : 0);
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
        if (!(object instanceof Caja)) {
            return false;
        }
        Caja other = (Caja) object;
        if ((this.idCaja == null && other.idCaja != null) || (this.idCaja != null && !this.idCaja.equals(other.idCaja))) {
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
        return "com.ppi115.cafefe.entity.Caja[ idCaja=" + idCaja + " ]";
    }
    
}
