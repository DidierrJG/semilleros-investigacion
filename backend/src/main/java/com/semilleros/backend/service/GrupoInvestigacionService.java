package com.semilleros.backend.service;

import com.semilleros.backend.dto.GrupoInvestigacionRequestDTO;
import com.semilleros.backend.dto.GrupoInvestigacionResponseDTO;
import com.semilleros.backend.entity.GrupoInvestigacion;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.GrupoInvestigacionRepository;
import com.semilleros.backend.repository.SemilleroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrupoInvestigacionService {

    private final GrupoInvestigacionRepository grupoInvestigacionRepository;
    private final SemilleroRepository semilleroRepository;

    @Transactional(readOnly = true)
    public List<GrupoInvestigacionResponseDTO> findAll() {
        return grupoInvestigacionRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public GrupoInvestigacionResponseDTO findById(Long id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    public GrupoInvestigacionResponseDTO create(GrupoInvestigacionRequestDTO request) {
        String sigla = request.getSigla().trim();
        if (grupoInvestigacionRepository.existsBySiglaIgnoreCase(sigla)) {
            throw new BusinessException("Ya existe un grupo de investigación registrado con la sigla: " + sigla);
        }

        String codigoMinciencias = request.getCodigoMinciencias().trim();
        if (grupoInvestigacionRepository.existsByCodigoMincienciasIgnoreCase(codigoMinciencias)) {
            throw new BusinessException("Ya existe un grupo de investigación registrado con el código Minciencias: " + codigoMinciencias);
        }

        String correo = request.getCorreo().trim();
        if (grupoInvestigacionRepository.existsByCorreoIgnoreCase(correo)) {
            throw new BusinessException("Ya existe un grupo de investigación registrado con el correo: " + correo);
        }

        GrupoInvestigacion grupo = GrupoInvestigacion.builder()
                .nombre(request.getNombre().trim())
                .sigla(sigla)
                .codigoMinciencias(codigoMinciencias)
                .categoria(request.getCategoria().trim())
                .facultad(request.getFacultad().trim())
                .departamento(request.getDepartamento().trim())
                .lider(request.getLider().trim())
                .correo(correo)
                .sitioWeb(request.getSitioWeb() != null ? request.getSitioWeb().trim() : null)
                .build();

        return toResponseDTO(grupoInvestigacionRepository.save(grupo));
    }

    @Transactional
    public GrupoInvestigacionResponseDTO update(Long id, GrupoInvestigacionRequestDTO request) {
        GrupoInvestigacion grupo = findEntityById(id);

        String sigla = request.getSigla().trim();
        if (grupoInvestigacionRepository.existsBySiglaIgnoreCaseAndIdNot(sigla, id)) {
            throw new BusinessException("Ya existe otro grupo de investigación registrado con la sigla: " + sigla);
        }

        String codigoMinciencias = request.getCodigoMinciencias().trim();
        if (grupoInvestigacionRepository.existsByCodigoMincienciasIgnoreCaseAndIdNot(codigoMinciencias, id)) {
            throw new BusinessException("Ya existe otro grupo de investigación registrado con el código Minciencias: " + codigoMinciencias);
        }

        String correo = request.getCorreo().trim();
        if (grupoInvestigacionRepository.existsByCorreoIgnoreCaseAndIdNot(correo, id)) {
            throw new BusinessException("Ya existe otro grupo de investigación registrado con el correo: " + correo);
        }

        grupo.setNombre(request.getNombre().trim());
        grupo.setSigla(sigla);
        grupo.setCodigoMinciencias(codigoMinciencias);
        grupo.setCategoria(request.getCategoria().trim());
        grupo.setFacultad(request.getFacultad().trim());
        grupo.setDepartamento(request.getDepartamento().trim());
        grupo.setLider(request.getLider().trim());
        grupo.setCorreo(correo);
        grupo.setSitioWeb(request.getSitioWeb() != null ? request.getSitioWeb().trim() : null);

        return toResponseDTO(grupoInvestigacionRepository.save(grupo));
    }

    @Transactional
    public void delete(Long id) {
        GrupoInvestigacion grupo = findEntityById(id);
        if (semilleroRepository.existsByGrupoInvestigacionId(id)) {
            throw new BusinessException("No se puede eliminar el grupo de investigación porque tiene semilleros asociados");
        }
        grupoInvestigacionRepository.delete(grupo);
    }

    public GrupoInvestigacion findEntityById(Long id) {
        return grupoInvestigacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de investigación no encontrado con id: " + id));
    }

    public GrupoInvestigacionResponseDTO toResponseDTO(GrupoInvestigacion grupo) {
        return GrupoInvestigacionResponseDTO.builder()
                .id(grupo.getId())
                .nombre(grupo.getNombre())
                .sigla(grupo.getSigla())
                .codigoMinciencias(grupo.getCodigoMinciencias())
                .categoria(grupo.getCategoria())
                .facultad(grupo.getFacultad())
                .departamento(grupo.getDepartamento())
                .lider(grupo.getLider())
                .correo(grupo.getCorreo())
                .sitioWeb(grupo.getSitioWeb())
                .build();
    }
}
