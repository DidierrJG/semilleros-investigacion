package com.semilleros.backend.repository;

import com.semilleros.backend.entity.GrupoInvestigacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoInvestigacionRepository extends JpaRepository<GrupoInvestigacion, Long> {

    boolean existsBySiglaIgnoreCase(String sigla);

    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);

    boolean existsByCodigoMincienciasIgnoreCase(String codigoMinciencias);

    boolean existsByCodigoMincienciasIgnoreCaseAndIdNot(String codigoMinciencias, Long id);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
}
