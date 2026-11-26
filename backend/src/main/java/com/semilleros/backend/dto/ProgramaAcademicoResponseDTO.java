package com.semilleros.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de respuesta de un programa académico")
public class ProgramaAcademicoResponseDTO {

    @Schema(description = "Identificador único del programa", example = "1")
    private Long id;

    @Schema(description = "Nombre del programa académico", example = "Ingeniería de Sistemas")
    private String nombre;

    @Schema(description = "Facultad a la que pertenece el programa")
    private FacultadSummaryDTO facultad;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resumen de facultad en programa")
    public static class FacultadSummaryDTO {
        private Long id;
        private String nombre;
        private String sigla;
    }
}
