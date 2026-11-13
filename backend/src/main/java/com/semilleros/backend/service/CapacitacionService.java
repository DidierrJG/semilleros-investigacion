package com.semilleros.backend.service;

import com.semilleros.backend.dto.CapacitacionRequestDTO;
import com.semilleros.backend.dto.CapacitacionResponseDTO;
import com.semilleros.backend.entity.Capacitacion;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.entity.Semillero;
import com.semilleros.backend.repository.SemilleroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CapacitacionService {

    private final CapacitacionRepository repository;

    @Transactional
    public CapacitacionResponseDTO create(CapacitacionRequestDTO dto) {
        // Simple validation: porcentajeCumplimiento between 0 and 100
        if (dto.getPorcentajeCumplimiento() < 0 || dto.getPorcentajeCumplimiento() > 100) {
            throw new BusinessException("El porcentaje de cumplimiento debe estar entre 0 y 100");
        }
        Capacitacion entity = mapToEntity(dto);
        Capacitacion saved = repository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CapacitacionResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CapacitacionResponseDTO findById(Long id) {
        Capacitacion entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Capacitacion no encontrada con id " + id));
        return mapToResponse(entity);
    }

    @Transactional
    public CapacitacionResponseDTO update(Long id, CapacitacionRequestDTO dto) {
        Capacitacion existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Capacitacion no encontrada con id " + id));
        if (dto.getPorcentajeCumplimiento() < 0 || dto.getPorcentajeCumplimiento() > 100) {
            throw new BusinessException("El porcentaje de cumplimiento debe estar entre 0 y 100");
        }
        // Update fields
        existing.setNombreCapacitacion(dto.getNombreCapacitacion());
        existing.setFecha(dto.getFecha());
        existing.setObjetivoCapacitacion(dto.getObjetivoCapacitacion());
        existing.setPorcentajeCumplimiento(dto.getPorcentajeCumplimiento());
        existing.setCantidadParticipantes(dto.getCantidadParticipantes());
        Capacitacion saved = repository.save(existing);
        return mapToResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Capacitacion existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Capacitacion no encontrada con id " + id));
        repository.delete(existing);
    }

    // ---------- Mapper helpers ----------
    private Capacitacion mapToEntity(CapacitacionRequestDTO dto) {
        return Capacitacion.builder()
                .nombreCapacitacion(dto.getNombreCapacitacion())
                .fecha(dto.getFecha())
                .objetivoCapacitacion(dto.getObjetivoCapacitacion())
                .porcentajeCumplimiento(dto.getPorcentajeCumplimiento())
                .cantidadParticipantes(dto.getCantidadParticipantes())
                .build();
    }

    private CapacitacionResponseDTO mapToResponse(Capacitacion entity) {
        CapacitacionResponseDTO resp = new CapacitacionResponseDTO();
        resp.setId(entity.getId());
        resp.setNombreCapacitacion(entity.getNombreCapacitacion());
        resp.setFecha(entity.getFecha());
        resp.setObjetivoCapacitacion(entity.getObjetivoCapacitacion());
        resp.setPorcentajeCumplimiento(entity.getPorcentajeCumplimiento());
        resp.setCantidadParticipantes(entity.getCantidadParticipantes());
        return resp;
    }
}

