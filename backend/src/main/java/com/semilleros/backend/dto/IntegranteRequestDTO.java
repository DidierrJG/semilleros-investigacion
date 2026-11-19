package com.semilleros.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de entrada para crear o actualizar un integrante")
public class IntegranteRequestDTO {

    @NotNull(message = "El ID del semillero es obligatorio")
    @Schema(description = "Identificador del semillero al que pertenece", example = "1")
    private Long semilleroId;

    @NotBlank(message = "El código del integrante es obligatorio")
    @Size(max = 40, message = "El código no puede superar los 40 caracteres")
    @Schema(description = "Código estudiantil o institucional del integrante", example = "2024102001")
    private String codigo;

    @NotBlank(message = "El nombre del integrante es obligatorio")
    @Size(max = 160, message = "El nombre no puede superar los 160 caracteres")
    @Schema(description = "Nombre completo del integrante", example = "Carlos Andrés Gómez")
    private String nombre;

    @NotBlank(message = "El programa académico es obligatorio")
    @Size(max = 160, message = "El programa no puede superar los 160 caracteres")
    @Schema(description = "Programa académico del integrante", example = "Ingeniería de Sistemas")
    private String programa;

    @NotNull(message = "Debe indicar si el integrante es líder")
    @Schema(description = "Indica si el integrante es líder del semillero", example = "false")
    private Boolean esLider;

    @NotBlank(message = "La identificación es obligatoria")
    @Size(max = 20, message = "La identificación no puede superar los 20 caracteres")
    @Pattern(regexp = "\\d+", message = "La identificación solo debe contener dígitos")
    @Schema(description = "Número de identificación (tarjeta de identidad / cédula)", example = "1020304050")
    private String identificacion;

    @NotNull(message = "El semestre actual es obligatorio")
    @Min(value = 1, message = "El semestre debe ser al menos 1")
    @Max(value = 12, message = "El semestre no puede superar 12")
    @Schema(description = "Semestre actual cursado", example = "4")
    private Integer semestreActual;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 14, message = "La edad mínima es 14 años")
    @Max(value = 99, message = "La edad no puede superar 99 años")
    @Schema(description = "Edad del integrante", example = "20")
    private Integer edad;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
    @Schema(description = "Dirección de residencia", example = "Calle 123 # 45-67, Bogotá")
    private String direccion;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 160, message = "El email no puede superar los 160 caracteres")
    @Schema(description = "Correo electrónico", example = "carlos.gomez@universidad.edu.co")
    private String email;

    @NotBlank(message = "El celular es obligatorio")
    @Size(max = 20, message = "El celular no puede superar los 20 caracteres")
    @Pattern(regexp = "^[0-9+\\-() ]+$", message = "El celular solo puede contener dígitos, +, -, () y espacios")
    @Schema(description = "Número de celular", example = "3001234567")
    private String celular;
}

