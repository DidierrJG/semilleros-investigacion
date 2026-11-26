package com.semilleros.backend.storage;

import lombok.Getter;

@Getter
public enum FileCategory {
    LOGOS("logos", "Logos institucionales de semilleros y grupos"),
    INTEGRANTES("integrantes", "Fotografías de integrantes y directores"),
    EVIDENCIAS("evidencias", "Evidencias de actividades y proyectos"),
    DOCUMENTOS("documentos", "Documentos y certificados");

    private final String subDirectory;
    private final String descripcion;

    FileCategory(String subDirectory, String descripcion) {
        this.subDirectory = subDirectory;
        this.descripcion = descripcion;
    }

    public static FileCategory fromString(String category) {
        if (category == null) {
            throw new IllegalArgumentException("La categoría de archivo no puede ser nula");
        }
        for (FileCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(category) || cat.subDirectory.equalsIgnoreCase(category)) {
                return cat;
            }
        }
        throw new IllegalArgumentException("Categoría de archivo desconocida: " + category);
    }
}

