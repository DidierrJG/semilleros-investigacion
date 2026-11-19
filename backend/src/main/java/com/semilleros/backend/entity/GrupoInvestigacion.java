package com.semilleros.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grupos_investigacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoInvestigacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, unique = true, length = 30)
    private String sigla;

    @Column(name = "codigo_minciencias", nullable = false, unique = true, length = 30)
    private String codigoMinciencias;

    @Column(nullable = false, length = 30)
    private String categoria;

    @Column(nullable = false, length = 150)
    private String facultad;

    @Column(nullable = false, length = 150)
    private String departamento;

    @Column(nullable = false, length = 180)
    private String lider;

    @Column(nullable = false, unique = true, length = 180)
    private String correo;

    @Column(name = "sitio_web", length = 255)
    private String sitioWeb;
}
