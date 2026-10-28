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
 * Entidad JPA que representa asignación de categoría a producto y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa asignación de categoría a producto y sus relaciones en la cafetería. */
@Entity
@Table(name = "producto_tipo_producto", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "ProductoTipoProducto.findAll", query = "SELECT p FROM ProductoTipoProducto p")})
public class ProductoTipoProducto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_producto_tipo_producto", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idProductoTipoProducto;
    @Column(name = "activo")
    private Boolean activo;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @NotNull(message = "El producto es obligatorio")
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private Producto idProducto;
    @NotNull(message = "El tipo de producto es obligatorio")
    @JoinColumn(name = "id_tipo_producto", referencedColumnName = "id_tipo_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private TipoProducto idTipoProducto;

    /**
     * Inicializa ProductoTipoProducto con los datos recibidos.
     */
    public ProductoTipoProducto() {
    }

    /**
     * Inicializa ProductoTipoProducto con los datos recibidos.
     * @param idProductoTipoProducto id producto tipo producto utilizado por la operación

     */
    public ProductoTipoProducto(UUID idProductoTipoProducto) {
        this.idProductoTipoProducto = idProductoTipoProducto;
    }

    /**
     * Obtiene id producto tipo producto para la vista o la operación actual.
     * @return id producto tipo producto actual
     */
    public UUID getIdProductoTipoProducto() {
        return idProductoTipoProducto;
    }

    /**
     * Actualiza id producto tipo producto con el valor recibido.
     * @param idProductoTipoProducto id producto tipo producto utilizado por la operación
     */
    public void setIdProductoTipoProducto(UUID idProductoTipoProducto) {
        this.idProductoTipoProducto = idProductoTipoProducto;
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
     * Obtiene fecha creacion para la vista o la operación actual.
     * @return fecha creacion actual
     */
    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Actualiza fecha creacion con el valor recibido.
     * @param fechaCreacion fecha creacion utilizado por la operación
     */
    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
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
     * Obtiene id tipo producto para la vista o la operación actual.
     * @return id tipo producto actual
     */
    public TipoProducto getIdTipoProducto() {
        return idTipoProducto;
    }

    /**
     * Actualiza id tipo producto con el valor recibido.
     * @param idTipoProducto id tipo producto utilizado por la operación
     */
    public void setIdTipoProducto(TipoProducto idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProductoTipoProducto != null ? idProductoTipoProducto.hashCode() : 0);
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
        if (!(object instanceof ProductoTipoProducto)) {
            return false;
        }
        ProductoTipoProducto other = (ProductoTipoProducto) object;
        if ((this.idProductoTipoProducto == null && other.idProductoTipoProducto != null) || (this.idProductoTipoProducto != null && !this.idProductoTipoProducto.equals(other.idProductoTipoProducto))) {
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
        return "com.ppi115.cafefe.entity.ProductoTipoProducto[ idProductoTipoProducto=" + idProductoTipoProducto + " ]";
    }
    
}
