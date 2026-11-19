package com.semilleros.backend.service;

import com.semilleros.backend.dto.CapacitacionRequestDTO;
import com.semilleros.backend.dto.CapacitacionResponseDTO;
import com.semilleros.backend.entity.Capacitacion;
import com.semilleros.backend.entity.Semillero;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.CapacitacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CapacitacionService {

    private final CapacitacionRepository capacitacionRepository;
    private final SemilleroService semilleroService;

    @Transactional(readOnly = true)
    public List<CapacitacionResponseDTO> findAll() {
        return capacitacionRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CapacitacionResponseDTO> findBySemilleroId(Long semilleroId) {
        semilleroService.findEntityById(semilleroId);
        return capacitacionRepository.findBySemilleroId(semilleroId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CapacitacionResponseDTO findById(Long id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    public CapacitacionResponseDTO create(CapacitacionRequestDTO dto) {
        if (dto.getPorcentajeCumplimiento() < 0 || dto.getPorcentajeCumplimiento() > 100) {
            throw new BusinessException("El porcentaje de cumplimiento debe estar entre 0 y 100");
        }
        Semillero semillero = semilleroService.findEntityById(dto.getSemilleroId());

        Capacitacion capacitacion = Capacitacion.builder()
                .semillero(semillero)
                .nombreCapacitacion(dto.getNombreCapacitacion().trim())
                .fecha(dto.getFecha())
                .objetivoCapacitacion(dto.getObjetivoCapacitacion().trim())
                .porcentajeCumplimiento(dto.getPorcentajeCumplimiento())
                .cantidadParticipantes(dto.getCantidadParticipantes())
                .build();

        return toResponseDTO(capacitacionRepository.save(capacitacion));
    }

    @Transactional
    public CapacitacionResponseDTO update(Long id, CapacitacionRequestDTO dto) {
        Capacitacion capacitacion = findEntityById(id);
        if (dto.getPorcentajeCumplimiento() < 0 || dto.getPorcentajeCumplimiento() > 100) {
            throw new BusinessException("El porcentaje de cumplimiento debe estar entre 0 y 100");
        }
        Semillero semillero = semilleroService.findEntityById(dto.getSemilleroId());

        capacitacion.setSemillero(semillero);
        capacitacion.setNombreCapacitacion(dto.getNombreCapacitacion().trim());
        capacitacion.setFecha(dto.getFecha());
        capacitacion.setObjetivoCapacitacion(dto.getObjetivoCapacitacion().trim());
        capacitacion.setPorcentajeCumplimiento(dto.getPorcentajeCumplimiento());
        capacitacion.setCantidadParticipantes(dto.getCantidadParticipantes());

        return toResponseDTO(capacitacionRepository.save(capacitacion));
    }

    @Transactional
    public void delete(Long id) {
        Capacitacion capacitacion = findEntityById(id);
        capacitacionRepository.delete(capacitacion);
    }

    public Capacitacion findEntityById(Long id) {
        return capacitacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Capacitación no encontrada con id: " + id));
    }

    private CapacitacionResponseDTO toResponseDTO(Capacitacion entity) {
        Semillero semillero = entity.getSemillero();
        return CapacitacionResponseDTO.builder()
                .id(entity.getId())
                .semilleroId(semillero != null ? semillero.getId() : null)
                .semilleroNombre(semillero != null ? semillero.getNombre() : null)
                .semilleroSigla(semillero != null ? semillero.getSigla() : null)
                .nombreCapacitacion(entity.getNombreCapacitacion())
                .fecha(entity.getFecha())
                .objetivoCapacitacion(entity.getObjetivoCapacitacion())
                .porcentajeCumplimiento(entity.getPorcentajeCumplimiento())
                .cantidadParticipantes(entity.getCantidadParticipantes())
                .build();
    }
}
