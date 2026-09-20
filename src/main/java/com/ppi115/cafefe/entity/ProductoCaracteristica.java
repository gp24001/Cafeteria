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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author jazmi
 */
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
    @Size(max = 2147483647)
    @Column(name = "valor", length = 2147483647)
    private String valor;
    @JoinColumn(name = "id_caracteristica", referencedColumnName = "id_caracteristica")
    @ManyToOne(fetch = FetchType.LAZY)
    private Caracteristica idCaracteristica;
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private Producto idProducto;

    public ProductoCaracteristica() {
    }

    public ProductoCaracteristica(UUID idProductoCaracteristica) {
        this.idProductoCaracteristica = idProductoCaracteristica;
    }

    public UUID getIdProductoCaracteristica() {
        return idProductoCaracteristica;
    }

    public void setIdProductoCaracteristica(UUID idProductoCaracteristica) {
        this.idProductoCaracteristica = idProductoCaracteristica;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Caracteristica getIdCaracteristica() {
        return idCaracteristica;
    }

    public void setIdCaracteristica(Caracteristica idCaracteristica) {
        this.idCaracteristica = idCaracteristica;
    }

    public Producto getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Producto idProducto) {
        this.idProducto = idProducto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProductoCaracteristica != null ? idProductoCaracteristica.hashCode() : 0);
        return hash;
    }

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

    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.ProductoCaracteristica[ idProductoCaracteristica=" + idProductoCaracteristica + " ]";
    }
    
}
