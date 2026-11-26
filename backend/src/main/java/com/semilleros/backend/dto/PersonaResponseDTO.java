package com.semilleros.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de respuesta detallados de una persona")
public class PersonaResponseDTO {

    @Schema(description = "Identificador único de la persona", example = "1")
    private Long id;

    @Schema(description = "Número de identificación", example = "10901001")
    private String identificacion;

    @Schema(description = "Tipo de documento", example = "CC")
    private String tipoDocumento;

    @Schema(description = "Nombres", example = "Ana María")
    private String nombres;

    @Schema(description = "Apellidos", example = "Pérez")
    private String apellidos;

    @Schema(description = "Nombre completo unificado", example = "Ana María Pérez")
    private String nombreCompleto;

    @Schema(description = "Correo electrónico", example = "ana.perez@universidad.edu.co")
    private String correo;

    @Schema(description = "Teléfono de contacto", example = "3001234567")
    private String telefono;

    @Schema(description = "Enlace a CvLAC", example = "https://scienti.minciencias.gov.co/cvlac/...")
    private String cvlacUrl;

    @Schema(description = "Estado activo", example = "true")
    private Boolean activo;

    @Schema(description = "Departamento al que está adscrita la persona")
    private DepartamentoSummaryDTO departamento;

    @Schema(description = "Roles asignados ante la investigación")
    private List<RolResponseDTO> roles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resumen de departamento para la persona")
    public static class DepartamentoSummaryDTO {
        private Long id;
        private String nombre;
        private Long facultadId;
        private String facultadNombre;
    }
}
