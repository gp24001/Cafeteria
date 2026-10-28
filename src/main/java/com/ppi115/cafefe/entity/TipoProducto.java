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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que representa categoría de productos y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa categoría de productos y sus relaciones en la cafetería. */
@Entity
@Table(name = "tipo_producto", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "TipoProducto.findAll", query = "SELECT t FROM TipoProducto t")})
public class TipoProducto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_tipo_producto", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idTipoProducto;
    @Basic(optional = false)
    @NotNull
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @OneToMany(mappedBy = "idTipoProducto", fetch = FetchType.LAZY)
    private List<ProductoTipoProducto> productoTipoProductoList;

    /**
     * Inicializa TipoProducto con los datos recibidos.
     */
    public TipoProducto() {
    }

    /**
     * Inicializa TipoProducto con los datos recibidos.
     * @param idTipoProducto id tipo producto utilizado por la operación

     */
    public TipoProducto(UUID idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }

    /**
     * Inicializa TipoProducto con los datos recibidos.
     * @param idTipoProducto id tipo producto utilizado por la operación
     * @param nombre nombre usado para identificar el dato en los mensajes

     */
    public TipoProducto(UUID idTipoProducto, String nombre) {
        this.idTipoProducto = idTipoProducto;
        this.nombre = nombre;
    }

    /**
     * Obtiene id tipo producto para la vista o la operación actual.
     * @return id tipo producto actual
     */
    public UUID getIdTipoProducto() {
        return idTipoProducto;
    }

    /**
     * Actualiza id tipo producto con el valor recibido.
     * @param idTipoProducto id tipo producto utilizado por la operación
     */
    public void setIdTipoProducto(UUID idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
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
     * Obtiene producto tipo producto list para la vista o la operación actual.
     * @return producto tipo producto list actual
     */
    public List<ProductoTipoProducto> getProductoTipoProductoList() {
        return productoTipoProductoList;
    }

    /**
     * Actualiza producto tipo producto list con el valor recibido.
     * @param productoTipoProductoList producto tipo producto list utilizado por la operación
     */
    public void setProductoTipoProductoList(List<ProductoTipoProducto> productoTipoProductoList) {
        this.productoTipoProductoList = productoTipoProductoList;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipoProducto != null ? idTipoProducto.hashCode() : 0);
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
        if (!(object instanceof TipoProducto)) {
            return false;
        }
        TipoProducto other = (TipoProducto) object;
        if ((this.idTipoProducto == null && other.idTipoProducto != null) || (this.idTipoProducto != null && !this.idTipoProducto.equals(other.idTipoProducto))) {
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
        return "com.ppi115.cafefe.entity.TipoProducto[ idTipoProducto=" + idTipoProducto + " ]";
    }
    
}
