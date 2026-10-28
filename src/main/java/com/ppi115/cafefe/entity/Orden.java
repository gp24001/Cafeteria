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
 * Entidad JPA que representa orden de venta y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa orden de venta y sus relaciones en la cafetería. */
@Entity
@Table(name = "orden", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Orden.findAll", query = "SELECT o FROM Orden o")})
public class Orden implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_orden", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idOrden;
    @Column(name = "fecha_orden")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaOrden;
    @Size(max = 2147483647)
    @Column(name = "observacion", length = 2147483647)
    private String observacion;
    @OneToMany(mappedBy = "idOrden", fetch = FetchType.LAZY)
    private List<OrdenProducto> ordenProductoList;
    @JoinColumn(name = "id_empleado_rol", referencedColumnName = "id_empleado_rol", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private EmpleadoRol idEmpleadoRol;

    /**
     * Inicializa Orden con los datos recibidos.
     */
    public Orden() {
    }

    /**
     * Inicializa Orden con los datos recibidos.
     * @param idOrden identificador de la orden

     */
    public Orden(UUID idOrden) {
        this.idOrden = idOrden;
    }

    /**
     * Obtiene id orden para la vista o la operación actual.
     * @return id orden actual
     */
    public UUID getIdOrden() {
        return idOrden;
    }

    /**
     * Actualiza id orden con el valor recibido.
     * @param idOrden identificador de la orden
     */
    public void setIdOrden(UUID idOrden) {
        this.idOrden = idOrden;
    }

    /**
     * Obtiene fecha orden para la vista o la operación actual.
     * @return fecha orden actual
     */
    public Date getFechaOrden() {
        return fechaOrden;
    }

    /**
     * Actualiza fecha orden con el valor recibido.
     * @param fechaOrden fecha orden utilizado por la operación
     */
    public void setFechaOrden(Date fechaOrden) {
        this.fechaOrden = fechaOrden;
    }

    /**
     * Obtiene observacion para la vista o la operación actual.
     * @return observacion actual
     */
    public String getObservacion() {
        return observacion;
    }

    /**
     * Actualiza observacion con el valor recibido.
     * @param observacion observacion utilizado por la operación
     */
    public void setObservacion(String observacion) {
        this.observacion = observacion;
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
     * Obtiene id empleado rol para la vista o la operación actual.
     * @return id empleado rol actual
     */
    public EmpleadoRol getIdEmpleadoRol() {
        return idEmpleadoRol;
    }

    /**
     * Actualiza id empleado rol con el valor recibido.
     * @param idEmpleadoRol id empleado rol utilizado por la operación
     */
    public void setIdEmpleadoRol(EmpleadoRol idEmpleadoRol) {
        this.idEmpleadoRol = idEmpleadoRol;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOrden != null ? idOrden.hashCode() : 0);
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
        if (!(object instanceof Orden)) {
            return false;
        }
        Orden other = (Orden) object;
        return !((this.idOrden == null && other.idOrden != null) || (this.idOrden != null && !this.idOrden.equals(other.idOrden)));
    }

    /**
     * Devuelve una representación textual que permite identificar la entidad.
     * @return texto con el tipo y el identificador de la entidad
     */
    @Override
    public String toString() {
        return "com.ppi115.cafefe.entity.Orden[ idOrden=" + idOrden + " ]";
    }
    
}
