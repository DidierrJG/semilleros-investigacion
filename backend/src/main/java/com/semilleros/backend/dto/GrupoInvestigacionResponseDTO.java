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
@Schema(description = "Datos de respuesta de un grupo de investigación de la UFPS")
public class GrupoInvestigacionResponseDTO {

    @Schema(description = "Identificador único del grupo de investigación", example = "1")
    private Long id;

    @Schema(description = "Nombre oficial del grupo de investigación", example = "Grupo de Investigación y Desarrollo de Ingeniería del Software")
    private String nombre;

    @Schema(description = "Sigla representativa del grupo", example = "GIDISOFT")
    private String sigla;

    @Schema(description = "Código de registro en Minciencias", example = "COL0006204")
    private String codigoMinciencias;

    @Schema(description = "Categoría Minciencias", example = "A1")
    private String categoria;

    @Schema(description = "Facultad institucional de adscripción", example = "Facultad de Ingeniería")
    private String facultad;

    @Schema(description = "Departamento académico", example = "Sistemas e Informática")
    private String departamento;

    @Schema(description = "Docente investigador líder o director", example = "Dr. Carlos Eduardo Martínez")
    private String lider;

    @Schema(description = "Correo electrónico institucional", example = "gidisoft@ufps.edu.co")
    private String correo;

    @Schema(description = "Enlace al GrupLAC de Minciencias o sitio web", example = "https://scienti.minciencias.gov.co/gruplac/jsp/visualiza/visualizagr.jsp?nro=000000000006204")
    private String sitioWeb;
}
