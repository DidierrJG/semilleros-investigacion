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
@Schema(description = "Datos de respuesta de un departamento")
public class DepartamentoResponseDTO {

    @Schema(description = "Identificador del departamento", example = "1")
    private Long id;

    @Schema(description = "Nombre del departamento", example = "Sistemas e Informática")
    private String nombre;

    @Schema(description = "Facultad a la que pertenece")
    private FacultadSummaryDTO facultad;

    @Schema(description = "Director del departamento")
    private PersonaSummaryDTO director;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resumen de facultad")
    public static class FacultadSummaryDTO {
        private Long id;
        private String nombre;
        private String sigla;
    }
}
