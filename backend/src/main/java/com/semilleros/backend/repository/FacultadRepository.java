package com.semilleros.backend.repository;

import com.semilleros.backend.entity.Facultad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultadRepository extends JpaRepository<Facultad, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
    boolean existsBySiglaIgnoreCase(String sigla);
    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);
    Optional<Facultad> findBySiglaIgnoreCase(String sigla);
}
