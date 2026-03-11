package com.dsw02.empleados.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClaveEmpleadoId implements Serializable {

    @Column(name = "prefijo", nullable = false, length = 4)
    private String prefijo;

    @Column(name = "consecutivo", nullable = false)
    private Long consecutivo;

    public ClaveEmpleadoId() {
    }

    public ClaveEmpleadoId(String prefijo, Long consecutivo) {
        this.prefijo = prefijo;
        this.consecutivo = consecutivo;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }

    public Long getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(Long consecutivo) {
        this.consecutivo = consecutivo;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ClaveEmpleadoId that)) {
            return false;
        }
        return Objects.equals(prefijo, that.prefijo) && Objects.equals(consecutivo, that.consecutivo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefijo, consecutivo);
    }
}
