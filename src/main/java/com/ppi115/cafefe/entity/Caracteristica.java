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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que representa característica configurable y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa característica configurable y sus relaciones en la cafetería. */
@Entity
@Table(name = "caracteristica", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Caracteristica.findAll", query = "SELECT c FROM Caracteristica c")})
public class Caracteristica implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_caracteristica", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idCaracteristica;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @JoinColumn(name = "id_tipo_caracteristica", referencedColumnName = "id_tipo_caracteristica")
    @ManyToOne(fetch = FetchType.LAZY)
    private TipoCaracteristica idTipoCaracteristica;
    @OneToMany(mappedBy = "idCaracteristica", fetch = FetchType.LAZY)
    private List<ProductoCaracteristica> productoCaracteristicaList;

    /**
     * Inicializa Caracteristica con los datos recibidos.
     */
    public Caracteristica() {
    }

    /**
     * Inicializa Caracteristica con los datos recibidos.
     * @param idCaracteristica id caracteristica utilizado por la operación

     */
    public Caracteristica(UUID idCaracteristica) {
        this.idCaracteristica = idCaracteristica;
    }

    /**
     * Inicializa Caracteristica con los datos recibidos.
     * @param idCaracteristica id caracteristica utilizado por la operación
     * @param nombre nombre usado para identificar el dato en los mensajes

     */
    public Caracteristica(UUID idCaracteristica, String nombre) {
        this.idCaracteristica = idCaracteristica;
        this.nombre = nombre;
    }

    /**
     * Obtiene id caracteristica para la vista o la operación actual.
     * @return id caracteristica actual
     */
    public UUID getIdCaracteristica() {
        return idCaracteristica;
    }

    /**
     * Actualiza id caracteristica con el valor recibido.
     * @param idCaracteristica id caracteristica utilizado por la operación
     */
    public void setIdCaracteristica(UUID idCaracteristica) {
        this.idCaracteristica = idCaracteristica;
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
     * Obtiene id tipo caracteristica para la vista o la operación actual.
     * @return id tipo caracteristica actual
     */
    public TipoCaracteristica getIdTipoCaracteristica() {
        return idTipoCaracteristica;
    }

    /**
     * Actualiza id tipo caracteristica con el valor recibido.
     * @param idTipoCaracteristica id tipo caracteristica utilizado por la operación
     */
    public void setIdTipoCaracteristica(TipoCaracteristica idTipoCaracteristica) {
        this.idTipoCaracteristica = idTipoCaracteristica;
    }

    /**
     * Obtiene producto caracteristica list para la vista o la operación actual.
     * @return producto caracteristica list actual
     */
    public List<ProductoCaracteristica> getProductoCaracteristicaList() {
        return productoCaracteristicaList;
    }

    /**
     * Actualiza producto caracteristica list con el valor recibido.
     * @param productoCaracteristicaList producto caracteristica list utilizado por la operación
     */
    public void setProductoCaracteristicaList(List<ProductoCaracteristica> productoCaracteristicaList) {
        this.productoCaracteristicaList = productoCaracteristicaList;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idCaracteristica != null ? idCaracteristica.hashCode() : 0);
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
        if (!(object instanceof Caracteristica)) {
            return false;
        }
        Caracteristica other = (Caracteristica) object;
        if ((this.idCaracteristica == null && other.idCaracteristica != null) || (this.idCaracteristica != null && !this.idCaracteristica.equals(other.idCaracteristica))) {
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
        return "com.ppi115.cafefe.entity.Caracteristica[ idCaracteristica=" + idCaracteristica + " ]";
    }
    
}
