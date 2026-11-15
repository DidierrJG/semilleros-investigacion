package com.semilleros.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de entrada para crear o actualizar un grupo de investigación de la UFPS")
public class GrupoInvestigacionRequestDTO {

    @NotBlank(message = "El nombre del grupo de investigación es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
    @Schema(description = "Nombre oficial del grupo de investigación", example = "Grupo de Investigación y Desarrollo de Ingeniería del Software")
    private String nombre;

    @NotBlank(message = "La sigla del grupo de investigación es obligatoria")
    @Size(max = 30, message = "La sigla no puede superar los 30 caracteres")
    @Schema(description = "Sigla representativa del grupo", example = "GIDISOFT")
    private String sigla;

    @NotBlank(message = "El código Minciencias es obligatorio")
    @Size(max = 30, message = "El código Minciencias no puede superar los 30 caracteres")
    @Schema(description = "Código de registro oficial en Minciencias", example = "COL0006204")
    private String codigoMinciencias;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 30, message = "La categoría no puede superar los 30 caracteres")
    @Schema(description = "Categoría en la última medición de Minciencias (A1, A, B, C, Reconocido)", example = "A1")
    private String categoria;

    @NotBlank(message = "La facultad es obligatoria")
    @Size(max = 150, message = "La facultad no puede superar los 150 caracteres")
    @Schema(description = "Facultad de la UFPS a la que pertenece", example = "Facultad de Ingeniería")
    private String facultad;

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 150, message = "El departamento no puede superar los 150 caracteres")
    @Schema(description = "Departamento académico al que está adscrito", example = "Sistemas e Informática")
    private String departamento;

    @NotBlank(message = "El docente líder o director es obligatorio")
    @Size(max = 180, message = "El nombre del líder no puede superar los 180 caracteres")
    @Schema(description = "Docente investigador líder del grupo", example = "Dr. Carlos Eduardo Martínez")
    private String lider;

    @NotBlank(message = "El correo de contacto es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 180, message = "El correo no puede superar los 180 caracteres")
    @Schema(description = "Correo institucional de contacto", example = "gidisoft@ufps.edu.co")
    private String correo;

    @Size(max = 255, message = "El enlace no puede superar los 255 caracteres")
    @Schema(description = "Enlace al GrupLAC de Minciencias o sitio web", example = "https://scienti.minciencias.gov.co/gruplac/jsp/visualiza/visualizagr.jsp?nro=000000000006204")
    private String sitioWeb;
}
