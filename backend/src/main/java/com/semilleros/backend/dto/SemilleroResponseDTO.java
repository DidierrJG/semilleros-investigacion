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
@Schema(description = "Datos de respuesta de un semillero de investigación")
public class SemilleroResponseDTO {

    @Schema(description = "Identificador único del semillero", example = "1")
    private Long id;

    @Schema(description = "Nombre del semillero", example = "Semillero de Inteligencia Artificial Aplicada")
    private String nombre;

    @Schema(description = "Sigla única del semillero", example = "SIAA")
    private String sigla;

    @Schema(description = "Grupo de investigación de la UFPS al que está adscrito el semillero")
    private GrupoInvestigacionResponseDTO grupoInvestigacion;

    @Schema(description = "Misión del semillero")
    private String mision;

    @Schema(description = "Visión del semillero")
    private String vision;

    @Schema(description = "Objetivo general del semillero")
    private String objetivoGeneral;

    @Schema(description = "URL o ruta de acceso al logo del semillero", example = "/api/semilleros/1/logo")
    private String logoUrl;

    @Schema(description = "Información del director responsable")
    private DirectorResponseDTO director;

    @Schema(description = "URL o ruta relativa de la foto del semillero", example = "/uploads/semilleros/semillero_1_abc.jpg")
    private String fotoUrl;

    @Schema(description = "Líneas de investigación asociadas al semillero")
    private List<LineaResponseDTO> lineas;
}
