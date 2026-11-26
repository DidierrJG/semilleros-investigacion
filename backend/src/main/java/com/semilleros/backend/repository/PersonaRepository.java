package com.semilleros.backend.repository;

import com.semilleros.backend.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    boolean existsByIdentificacion(String identificacion);
    boolean existsByIdentificacionAndIdNot(String identificacion, Long id);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
    Optional<Persona> findByIdentificacion(String identificacion);
    Optional<Persona> findByCorreoIgnoreCase(String correo);
    List<Persona> findByDepartamentoId(Long departamentoId);

    @Query("SELECT p FROM Persona p JOIN p.roles r WHERE r.codigo = :rolCodigo ORDER BY p.nombres, p.apellidos")
    List<Persona> findByRolCodigo(@Param("rolCodigo") String rolCodigo);
}
