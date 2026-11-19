package com.semilleros.backend.repository;

import com.semilleros.backend.entity.ProgramaAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramaAcademicoRepository extends JpaRepository<ProgramaAcademico, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    boolean existsByIdNot(Long id);
}
