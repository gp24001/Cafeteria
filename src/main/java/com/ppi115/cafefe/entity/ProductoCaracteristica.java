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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;

/**
 * Entidad JPA que representa valor de una característica de producto y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa valor de una característica de producto y sus relaciones en la cafetería. */
@Entity
@Table(name = "producto_caracteristica", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "ProductoCaracteristica.findAll", query = "SELECT p FROM ProductoCaracteristica p")})
public class ProductoCaracteristica implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_producto_caracteristica", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idProductoCaracteristica;
    @NotBlank(message = "El valor es obligatorio")
    @Size(max = 2147483647)
    @Column(name = "valor", length = 2147483647)
    private String valor;
    @NotNull(message = "La característica es obligatoria")
    @JoinColumn(name = "id_caracteristica", referencedColumnName = "id_caracteristica")
    @ManyToOne(fetch = FetchType.LAZY)
    private Caracteristica idCaracteristica;
    @NotNull(message = "El producto es obligatorio")
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private Producto idProducto;

    /**
     * Inicializa ProductoCaracteristica con los datos recibidos.
     */
    public ProductoCaracteristica() {
    }

    /**
     * Inicializa ProductoCaracteristica con los datos recibidos.
     * @param idProductoCaracteristica id producto caracteristica utilizado por la operación

     */
    public ProductoCaracteristica(UUID idProductoCaracteristica) {
        this.idProductoCaracteristica = idProductoCaracteristica;
    }

    /**
     * Obtiene id producto caracteristica para la vista o la operación actual.
     * @return id producto caracteristica actual
     */
    public UUID getIdProductoCaracteristica() {
        return idProductoCaracteristica;
    }

    /**
     * Actualiza id producto caracteristica con el valor recibido.
     * @param idProductoCaracteristica id producto caracteristica utilizado por la operación
     */
    public void setIdProductoCaracteristica(UUID idProductoCaracteristica) {
        this.idProductoCaracteristica = idProductoCaracteristica;
    }

    /**
     * Obtiene valor para la vista o la operación actual.
     * @return valor actual
     */
    public String getValor() {
        return valor;
    }

    /**
     * Actualiza valor con el valor recibido.
     * @param valor nuevo valor que se asignará
     */
    public void setValor(String valor) {
        this.valor = valor;
    }

    /**
     * Obtiene id caracteristica para la vista o la operación actual.
     * @return id caracteristica actual
     */
    public Caracteristica getIdCaracteristica() {
        return idCaracteristica;
    }

    /**
     * Actualiza id caracteristica con el valor recibido.
     * @param idCaracteristica id caracteristica utilizado por la operación
     */
    public void setIdCaracteristica(Caracteristica idCaracteristica) {
        this.idCaracteristica = idCaracteristica;
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
        hash += (idProductoCaracteristica != null ? idProductoCaracteristica.hashCode() : 0);
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
        if (!(object instanceof ProductoCaracteristica)) {
            return false;
        }
        ProductoCaracteristica other = (ProductoCaracteristica) object;
        if ((this.idProductoCaracteristica == null && other.idProductoCaracteristica != null) || (this.idProductoCaracteristica != null && !this.idProductoCaracteristica.equals(other.idProductoCaracteristica))) {
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
        return "com.ppi115.cafefe.entity.ProductoCaracteristica[ idProductoCaracteristica=" + idProductoCaracteristica + " ]";
    }
    
}
