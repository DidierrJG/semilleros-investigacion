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
@Schema(description = "Datos de respuesta de un integrante")
public class IntegranteResponseDTO {

    @Schema(description = "Identificador único del integrante", example = "1")
    private Long id;

    @Schema(description = "ID del semillero al que pertenece", example = "1")
    private Long semilleroId;

    @Schema(description = "Nombre del semillero", example = "Semillero de Inteligencia Artificial Aplicada")
    private String semilleroNombre;

    @Schema(description = "Sigla del semillero", example = "SIAA")
    private String semilleroSigla;

    @Schema(description = "Código del integrante", example = "2024102001")
    private String codigo;

    @Schema(description = "Nombre completo del integrante", example = "Carlos Andrés Gómez")
    private String nombre;

    @Schema(description = "ID del programa académico", example = "1")
    private Long programaId;

    @Schema(description = "Nombre del programa académico", example = "Ingeniería de Sistemas")
    private String programaNombre;

    @Schema(description = "Indica si es líder del semillero", example = "false")
    private Boolean esLider;

    @Schema(description = "Número de identificación (tarjeta de identidad / cédula)", example = "1020304050")
    private String identificacion;

    @Schema(description = "Semestre actual cursado", example = "4")
    private Integer semestreActual;

    @Schema(description = "Edad del integrante", example = "20")
    private Integer edad;

    @Schema(description = "Dirección de residencia", example = "Calle 123 # 45-67, Bogotá")
    private String direccion;

    @Schema(description = "Correo electrónico", example = "carlos.gomez@universidad.edu.co")
    private String email;

    @Schema(description = "Número de celular", example = "3001234567")
    private String celular;
}

