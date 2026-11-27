package com.semilleros.backend.repository;

import com.semilleros.backend.entity.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    List<Departamento> findByFacultadId(Long facultadId);
    boolean existsByNombreIgnoreCaseAndFacultadId(String nombre, Long facultadId);
    boolean existsByNombreIgnoreCaseAndFacultadIdAndIdNot(String nombre, Long facultadId, Long id);
}
