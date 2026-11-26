package com.semilleros.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "personas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String identificacion;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    @Builder.Default
    private String tipoDocumento = "CC";

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, length = 180, unique = true)
    private String correo;

    @Column(length = 20)
    private String telefono;

    @Column(name = "cvlac_url", length = 300)
    private String cvlacUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "persona_roles",
            joinColumns = @JoinColumn(name = "persona_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();

    public String getNombreCompleto() {
        return (nombres + " " + apellidos).trim();
    }
}
