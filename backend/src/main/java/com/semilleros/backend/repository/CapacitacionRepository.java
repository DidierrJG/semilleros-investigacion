package com.semilleros.backend.repository;

import com.semilleros.backend.entity.Capacitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapacitacionRepository extends JpaRepository<Capacitacion, Long> {

    List<Capacitacion> findBySemilleroId(Long semilleroId);

    boolean existsBySemilleroId(Long semilleroId);
}
