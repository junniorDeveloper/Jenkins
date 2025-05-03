package com.ejemplo.demo.service;

import com.ejemplo.demo.model.Profesor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProfesorService {

    public List<Profesor> obtenerTodos() {
        return Arrays.asList(
                new Profesor(1L, "Juan Pérez"),
                new Profesor(2L, "Ana Gómez")
        );
    }

    public Profesor obtenerPorId(Long id) {
        return obtenerTodos().stream()
                .filter(profesor -> profesor.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
