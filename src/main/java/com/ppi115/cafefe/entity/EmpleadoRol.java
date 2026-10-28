/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ppi115.cafefe.entity;

import com.ppi115.cafefe.control.UUIDConverter;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
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
 * Entidad JPA que representa asignación de rol a empleado y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa asignación de rol a empleado y sus relaciones en la cafetería. */
@Entity
@Table(name = "empleado_rol", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "EmpleadoRol.findAll", query = "SELECT e FROM EmpleadoRol e")})
public class EmpleadoRol implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_empleado_rol", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idEmpleadoRol;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @JoinColumn(name = "id_empleado", referencedColumnName = "id_empleado")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empleado idEmpleado;
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol")
    @ManyToOne(fetch = FetchType.LAZY)
    private Rol idRol;
    @OneToMany(mappedBy = "idEmpleadoRol", fetch = FetchType.LAZY)
    private List<Factura> facturaList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idEmpleadoRol", fetch = FetchType.LAZY)
    private List<Orden> ordenList;

    /**
     * Inicializa EmpleadoRol con los datos recibidos.
     */
    public EmpleadoRol() {
    }

    /**
     * Inicializa EmpleadoRol con los datos recibidos.
     * @param idEmpleadoRol id empleado rol utilizado por la operación

     */
    public EmpleadoRol(UUID idEmpleadoRol) {
        this.idEmpleadoRol = idEmpleadoRol;
    }

    /**
     * Obtiene id empleado rol para la vista o la operación actual.
     * @return id empleado rol actual
     */
    public UUID getIdEmpleadoRol() {
        return idEmpleadoRol;
    }

    /**
     * Actualiza id empleado rol con el valor recibido.
     * @param idEmpleadoRol id empleado rol utilizado por la operación
     */
    public void setIdEmpleadoRol(UUID idEmpleadoRol) {
        this.idEmpleadoRol = idEmpleadoRol;
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
     * Obtiene id empleado para la vista o la operación actual.
     * @return id empleado actual
     */
    public Empleado getIdEmpleado() {
        return idEmpleado;
    }

    /**
     * Actualiza id empleado con el valor recibido.
     * @param idEmpleado identificador del empleado
     */
    public void setIdEmpleado(Empleado idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    /**
     * Obtiene id rol para la vista o la operación actual.
     * @return id rol actual
     */
    public Rol getIdRol() {
        return idRol;
    }

    /**
     * Actualiza id rol con el valor recibido.
     * @param idRol id rol utilizado por la operación
     */
    public void setIdRol(Rol idRol) {
        this.idRol = idRol;
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
     * Obtiene orden list para la vista o la operación actual.
     * @return orden list actual
     */
    public List<Orden> getOrdenList() {
        return ordenList;
    }

    /**
     * Actualiza orden list con el valor recibido.
     * @param ordenList orden list utilizado por la operación
     */
    public void setOrdenList(List<Orden> ordenList) {
        this.ordenList = ordenList;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idEmpleadoRol != null ? idEmpleadoRol.hashCode() : 0);
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
        if (!(object instanceof EmpleadoRol)) {
            return false;
        }
        EmpleadoRol other = (EmpleadoRol) object;
        if ((this.idEmpleadoRol == null && other.idEmpleadoRol != null) || (this.idEmpleadoRol != null && !this.idEmpleadoRol.equals(other.idEmpleadoRol))) {
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
        return "com.ppi115.cafefe.entity.EmpleadoRol[ idEmpleadoRol=" + idEmpleadoRol + " ]";
    }
    
}
