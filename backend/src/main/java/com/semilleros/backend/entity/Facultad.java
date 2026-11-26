package com.semilleros.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "facultades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facultad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180, unique = true)
    private String nombre;

    @Column(nullable = false, length = 20, unique = true)
    private String sigla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representante_id")
    private Persona representante;
}
