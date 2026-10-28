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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que representa empleado y sus relaciones en la cafetería.
 * @author jazmi
 */
/** Entidad JPA que representa empleado y sus relaciones en la cafetería. */
@Entity
@Table(name = "empleado", catalog = "cafeteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Empleado.findAll", query = "SELECT e FROM Empleado e")})
public class Empleado implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "id_empleado", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID idEmpleado;
    @Size(max = 200)
    @Column(name = "nombre", length = 200)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+$",
    message = "El nombre solo puede contener letras y espacios")
    private String nombre;
    @Size(max = 200)
    @Column(name = "apellido", length = 200)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+$",
    message = "El apellido solo puede contener letras y espacios")
    private String apellido;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @OneToMany(mappedBy = "idEmpleado", fetch = FetchType.LAZY)
    private List<EmpleadoRol> empleadoRolList;

    /**
     * Inicializa Empleado con los datos recibidos.
     */
    public Empleado() {
    }

    /**
     * Inicializa Empleado con los datos recibidos.
     * @param idEmpleado identificador del empleado

     */
    public Empleado(UUID idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    /**
     * Obtiene id empleado para la vista o la operación actual.
     * @return id empleado actual
     */
    public UUID getIdEmpleado() {
        return idEmpleado;
    }

    /**
     * Actualiza id empleado con el valor recibido.
     * @param idEmpleado identificador del empleado
     */
    public void setIdEmpleado(UUID idEmpleado) {
        this.idEmpleado = idEmpleado;
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
     * Obtiene apellido para la vista o la operación actual.
     * @return apellido actual
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Actualiza apellido con el valor recibido.
     * @param apellido apellido utilizado por la operación
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
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
     * Obtiene empleado rol list para la vista o la operación actual.
     * @return empleado rol list actual
     */
    public List<EmpleadoRol> getEmpleadoRolList() {
        return empleadoRolList;
    }

    /**
     * Actualiza empleado rol list con el valor recibido.
     * @param empleadoRolList empleado rol list utilizado por la operación
     */
    public void setEmpleadoRolList(List<EmpleadoRol> empleadoRolList) {
        this.empleadoRolList = empleadoRolList;
    }

    /**
     * Calcula el código hash a partir del identificador de la entidad.
     * @return código hash derivado de la identidad de la entidad
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idEmpleado != null ? idEmpleado.hashCode() : 0);
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
        if (!(object instanceof Empleado)) {
            return false;
        }
        Empleado other = (Empleado) object;
        if ((this.idEmpleado == null && other.idEmpleado != null) || (this.idEmpleado != null && !this.idEmpleado.equals(other.idEmpleado))) {
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
        return "com.ppi115.cafefe.entity.Empleado[ idEmpleado=" + idEmpleado + " ]";
    }
    
}
