package com.semilleros.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de entrada para registrar o actualizar una persona")
public class PersonaRequestDTO {

    @NotBlank(message = "El número de identificación es obligatorio")
    @Size(max = 20, message = "La identificación no puede superar 20 caracteres")
    @Schema(description = "Documento de identidad", example = "10901001")
    private String identificacion;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 20, message = "El tipo de documento no puede superar 20 caracteres")
    @Schema(description = "Tipo de documento (CC, TI, CE, PASAPORTE)", example = "CC")
    private String tipoDocumento;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
    @Schema(description = "Nombres de la persona", example = "Ana María")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    @Schema(description = "Apellidos de la persona", example = "Pérez")
    private String apellidos;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no es válido")
    @Size(max = 180, message = "El correo no puede superar 180 caracteres")
    @Schema(description = "Correo electrónico institucional", example = "ana.perez@universidad.edu.co")
    private String correo;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    @Schema(description = "Teléfono o celular de contacto", example = "3001234567")
    private String telefono;

    @Size(max = 300, message = "La URL de CvLAC no puede superar 300 caracteres")
    @Schema(description = "Enlace al perfil de CvLAC Minciencias", example = "https://scienti.minciencias.gov.co/cvlac/...")
    private String cvlacUrl;

    @Schema(description = "ID del departamento académico al que está adscrita la persona", example = "1")
    private Long departamentoId;

    @Schema(description = "Códigos de los roles asignados ante la investigación", example = "[\"DIRECTOR_SEMILLERO\", \"DIRECTOR_DEPARTAMENTO\"]")
    private Set<String> rolCodigos;

    @Builder.Default
    @Schema(description = "Estado activo de la persona en el sistema", example = "true")
    private Boolean activo = true;
}
