/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ppi115.cafefe.entity;

import com.ppi115.cafefe.control.UUIDConverter;
import java.math.BigDecimal;
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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que representa producto del catálogo y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa producto del catálogo y sus relaciones en la cafetería. */
@Entity
@Table(name = "producto", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Producto.findAll", query = "SELECT p FROM Producto p")})
public class Producto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_producto", nullable = false)
    @Convert(converter = UUIDConverter.class) //converter convierte de UUID a UUID
    private UUID idProducto;
    @Basic(optional = false)
    @NotNull
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 1, max = 150)
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;
    @Basic(optional = false)
    @NotNull
    @DecimalMin(value = "0.01", message = "El precio sugerido debe ser mayor a 0")
    @jakarta.validation.constraints.Digits(integer = 6, fraction = 2,
            message = "El precio admite hasta seis enteros y dos decimales")
    @Column(name = "precio_sugerido", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioSugerido;
    @Basic(optional = false)
    @NotNull
    @Column(name = "activo", nullable = false)
    private boolean activo;
    @Size(max = 2147483647)
    @Column(name = "comentarios", length = 2147483647)
    private String comentarios;
    @OneToMany(mappedBy = "idProducto", fetch = FetchType.LAZY)
    private List<ProductoCaracteristica> productoCaracteristicaList;
    @OneToMany(mappedBy = "idProducto", fetch = FetchType.LAZY)
    private List<OrdenProducto> ordenProductoList;
    @OneToMany(mappedBy = "idProducto", fetch = FetchType.LAZY)
    private List<DescuentoProducto> descuentoProductoList;
    @OneToMany(mappedBy = "idProducto", fetch = FetchType.LAZY)
    private List<ProductoTipoProducto> productoTipoProductoList;

    /**
     * Inicializa Producto con los datos recibidos.
     */
    public Producto() {
    }

    /**
     * Inicializa Producto con los datos recibidos.
     * @param idProducto identificador del producto

     */
    public Producto(UUID idProducto) {
        this.idProducto = idProducto;
    }

    /**
     * Inicializa Producto con los datos recibidos.
     * @param idProducto identificador del producto
     * @param nombre nombre usado para identificar el dato en los mensajes
     * @param precioSugerido precio sugerido utilizado por la operación
     * @param activo activo utilizado por la operación

     */
    public Producto(UUID idProducto, String nombre, BigDecimal precioSugerido, boolean activo) {
    this.idProducto = idProducto;
    this.nombre = nombre;
    this.precioSugerido = precioSugerido;
    this.activo = activo;
    }

    /**
     * Obtiene id producto para la vista o la operación actual.
     * @return id producto actual
     */
    public UUID getIdProducto() {
        return idProducto;
    }

    /**
     * Actualiza id producto con el valor recibido.
     * @param idProducto identificador del producto
     */
    public void setIdProducto(UUID idProducto) {
        this.idProducto = idProducto;
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
     * @return true cuando se cumple la condición comprobada
     */
    public boolean getActivo() {
        return activo;
    }

    /**
     * Actualiza activo con el valor recibido.
     * @param activo activo utilizado por la operación
     */
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Obtiene precio sugerido para la vista o la operación actual.
     * @return precio sugerido actual
     */
    public BigDecimal getPrecioSugerido() {
        return precioSugerido;
    }

    /**
     * Actualiza precio sugerido con el valor recibido.
     * @param precioSugerido precio sugerido utilizado por la operación
     */
    public void setPrecioSugerido(BigDecimal precioSugerido) {
        this.precioSugerido = precioSugerido;
    }

    /**
     * Obtiene comentarios para la vista o la operación actual.
     * @return comentarios actual
     */
    public String getComentarios() {
        return comentarios;
    }

    /**
     * Actualiza comentarios con el valor recibido.
     * @param comentarios comentarios utilizado por la operación
     */
    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
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
     * Obtiene orden producto list para la vista o la operación actual.
     * @return orden producto list actual
     */
    public List<OrdenProducto> getOrdenProductoList() {
        return ordenProductoList;
    }

    /**
     * Actualiza orden producto list con el valor recibido.
     * @param ordenProductoList orden producto list utilizado por la operación
     */
    public void setOrdenProductoList(List<OrdenProducto> ordenProductoList) {
        this.ordenProductoList = ordenProductoList;
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
        hash += (idProducto != null ? idProducto.hashCode() : 0);
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
        if (!(object instanceof Producto)) {
            return false;
        }
        Producto other = (Producto) object;
        if ((this.idProducto == null && other.idProducto != null) || (this.idProducto != null && !this.idProducto.equals(other.idProducto))) {
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
        return "com.ppi115.cafefe.entity.Producto[ idProducto=" + idProducto + " ]";
    }
    
}
