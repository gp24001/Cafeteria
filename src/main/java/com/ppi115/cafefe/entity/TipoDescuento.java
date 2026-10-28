
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que representa tipo y límite de descuento y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa tipo y límite de descuento y sus relaciones en la cafetería. */
@Entity
@Table(name = "tipo_descuento", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "TipoDescuento.findAll", query = "SELECT t FROM TipoDescuento t")})
public class TipoDescuento implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_tipo_descuento", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idTipoDescuento;
    @Basic(optional = false)
    @NotNull
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 3, max = 150, message = "El nombre debe tener al menos 3 caracteres")
@Column(name = "nombre", nullable = false, length = 150)
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @NotNull(message = "El límite de descuento es obligatorio")
    @Min(value = 1, message = "El descuento debe ser mayor o igual a 1")
    @Max(value = 100, message = "El descuento no puede ser mayor a 100")
    @Column(name = "descuento_maximo")
    private Integer descuentoMaximo;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @OneToMany(mappedBy = "idTipoDescuento", fetch = FetchType.LAZY)
    private List<Descuento> descuentoList;

    /**
     * Inicializa TipoDescuento con los datos recibidos.
     */
    public TipoDescuento() {
    }

    /**
     * Inicializa TipoDescuento con los datos recibidos.
     * @param idTipoDescuento id tipo descuento utilizado por la operación

     */
    public TipoDescuento(UUID idTipoDescuento) {
        this.idTipoDescuento = idTipoDescuento;
    }

    /**
     * Inicializa TipoDescuento con los datos recibidos.
     * @param idTipoDescuento id tipo descuento utilizado por la operación
     * @param nombre nombre usado para identificar el dato en los mensajes

     */
    public TipoDescuento(UUID idTipoDescuento, String nombre) {
        this.idTipoDescuento = idTipoDescuento;
        this.nombre = nombre;
    }

    /**
     * Obtiene id tipo descuento para la vista o la operación actual.
     * @return id tipo descuento actual
     */
    public UUID getIdTipoDescuento() {
        return idTipoDescuento;
    }

    /**
     * Actualiza id tipo descuento con el valor recibido.
     * @param idTipoDescuento id tipo descuento utilizado por la operación
     */
    public void setIdTipoDescuento(UUID idTipoDescuento) {
        this.idTipoDescuento = idTipoDescuento;
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
     * Obtiene descuento maximo para la vista o la operación actual.
     * @return descuento maximo actual
     */
    public Integer getDescuentoMaximo() {
        return descuentoMaximo;
    }

    /**
     * Actualiza descuento maximo con el valor recibido.
     * @param descuentoMaximo descuento maximo utilizado por la operación
     */
    public void setDescuentoMaximo(Integer descuentoMaximo) {
        this.descuentoMaximo = descuentoMaximo;
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
     * Obtiene descuento list para la vista o la operación actual.
     * @return descuento list actual
     */
    public List<Descuento> getDescuentoList() {
        return descuentoList;
    }

    /**
     * Actualiza descuento list con el valor recibido.
     * @param descuentoList descuento list utilizado por la operación
     */
    public void setDescuentoList(List<Descuento> descuentoList) {
        this.descuentoList = descuentoList;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipoDescuento != null ? idTipoDescuento.hashCode() : 0);
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
        if (!(object instanceof TipoDescuento)) {
            return false;
        }
        TipoDescuento other = (TipoDescuento) object;
        if ((this.idTipoDescuento == null && other.idTipoDescuento != null) || (this.idTipoDescuento != null && !this.idTipoDescuento.equals(other.idTipoDescuento))) {
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
        return "com.ppi115.cafefe.entity.TipoDescuento[ idTipoDescuento=" + idTipoDescuento + " ]";
    }
    
}
