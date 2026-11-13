package com.semilleros.backend.repository;

import com.semilleros.backend.entity.Capacitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CapacitacionRepository extends JpaRepository<Capacitacion, Long> {
    // Additional query methods can be declared here if needed
}

