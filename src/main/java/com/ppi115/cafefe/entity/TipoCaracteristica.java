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
 * Entidad JPA que representa formato de características y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa formato de características y sus relaciones en la cafetería. */
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

    /**
     * Inicializa TipoCaracteristica con los datos recibidos.
     */
    public TipoCaracteristica() {
    }

    /**
     * Inicializa TipoCaracteristica con los datos recibidos.
     * @param idTipoCaracteristica id tipo caracteristica utilizado por la operación

     */
    public TipoCaracteristica(UUID idTipoCaracteristica) {
        this.idTipoCaracteristica = idTipoCaracteristica;
    }

    /**
     * Inicializa TipoCaracteristica con los datos recibidos.
     * @param idTipoCaracteristica id tipo caracteristica utilizado por la operación
     * @param nombre nombre usado para identificar el dato en los mensajes

     */
    public TipoCaracteristica(UUID idTipoCaracteristica, String nombre) {
        this.idTipoCaracteristica = idTipoCaracteristica;
        this.nombre = nombre;
    }

    /**
     * Obtiene id tipo caracteristica para la vista o la operación actual.
     * @return id tipo caracteristica actual
     */
    public UUID getIdTipoCaracteristica() {
        return idTipoCaracteristica;
    }

    /**
     * Actualiza id tipo caracteristica con el valor recibido.
     * @param idTipoCaracteristica id tipo caracteristica utilizado por la operación
     */
    public void setIdTipoCaracteristica(UUID idTipoCaracteristica) {
        this.idTipoCaracteristica = idTipoCaracteristica;
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
     * Obtiene expresion regular para la vista o la operación actual.
     * @return expresion regular actual
     */
    public String getExpresionRegular() {
        return expresionRegular;
    }

    /**
     * Actualiza expresion regular con el valor recibido.
     * @param expresionRegular expresion regular utilizado por la operación
     */
    public void setExpresionRegular(String expresionRegular) {
        this.expresionRegular = expresionRegular;
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
     * Obtiene caracteristica list para la vista o la operación actual.
     * @return caracteristica list actual
     */
    public List<Caracteristica> getCaracteristicaList() {
        return caracteristicaList;
    }

    /**
     * Actualiza caracteristica list con el valor recibido.
     * @param caracteristicaList caracteristica list utilizado por la operación
     */
    public void setCaracteristicaList(List<Caracteristica> caracteristicaList) {
        this.caracteristicaList = caracteristicaList;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipoCaracteristica != null ? idTipoCaracteristica.hashCode() : 0);
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
        if (!(object instanceof TipoCaracteristica)) {
            return false;
        }
        TipoCaracteristica other = (TipoCaracteristica) object;
        if ((this.idTipoCaracteristica == null && other.idTipoCaracteristica != null) || (this.idTipoCaracteristica != null && !this.idTipoCaracteristica.equals(other.idTipoCaracteristica))) {
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
        return "com.ppi115.cafefe.entity.TipoCaracteristica[ idTipoCaracteristica=" + idTipoCaracteristica + " ]";
    }
    
}
