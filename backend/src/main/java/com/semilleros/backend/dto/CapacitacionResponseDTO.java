package com.semilleros.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de salida de una capacitación de un semillero")
public class CapacitacionResponseDTO {

    @Schema(description = "Identificador único de la capacitación", example = "1")
    private Long id;

    @Schema(description = "ID del semillero", example = "1")
    private Long semilleroId;

    @Schema(description = "Nombre del semillero", example = "Semillero de Inteligencia Artificial Aplicada")
    private String semilleroNombre;

    @Schema(description = "Sigla del semillero", example = "SIAA")
    private String semilleroSigla;

    @Schema(description = "Nombre de la capacitación", example = "Taller de Redes Neuronales y Deep Learning con PyTorch")
    private String nombreCapacitacion;

    @Schema(description = "Fecha de la capacitación", example = "2024-03-20")
    private LocalDate fecha;

    @Schema(description = "Objetivo de la capacitación", example = "Capacitar a los semilleristas en el diseño y entrenamiento de redes neuronales.")
    private String objetivoCapacitacion;

    @Schema(description = "Porcentaje de cumplimiento (0 - 100%)", example = "100.0")
    private Double porcentajeCumplimiento;

    @Schema(description = "Cantidad de participantes", example = "28")
    private Integer cantidadParticipantes;
}
