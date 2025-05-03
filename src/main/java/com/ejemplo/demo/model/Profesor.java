package com.ejemplo.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Profesor {

    @Id
    private Long id;
    private String nombre;

    public Profesor() {
    }

    public Profesor(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
