package com.semilleros.backend.service;

import com.semilleros.backend.entity.Integrante;
import com.semilleros.backend.entity.Linea;
import com.semilleros.backend.entity.Producto;
import com.semilleros.backend.entity.Semillero;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.IntegranteRepository;
import com.semilleros.backend.repository.ProductoRepository;
import com.semilleros.backend.repository.SemilleroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemilleroExcelService {

    private static final String TEMPLATE_RESOURCE_PATH = "/templates/FORMATO ACTUALIZACION SEMILLERO 2024-1.xlsx";
    private static final String TEMPLATE_FALLBACK_PATH = "backend/templates/FORMATO ACTUALIZACION SEMILLERO 2024-1.xlsx";
    private static final String TEMPLATE_FALLBACK_PATH_2 = "templates/FORMATO ACTUALIZACION SEMILLERO 2024-1.xlsx";

    private final SemilleroRepository semilleroRepository;
    private final IntegranteRepository integranteRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public byte[] generarFormatoActualizacion(Long semilleroId) {
        Semillero semillero = semilleroRepository.findById(semilleroId)
                .orElseThrow(() -> new ResourceNotFoundException("Semillero no encontrado con id: " + semilleroId));

        List<Integrante> integrantes = integranteRepository.findBySemilleroId(semilleroId);
        List<Producto> productos = productoRepository.findBySemilleroId(semilleroId);

        try (InputStream templateStream = openTemplateInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(templateStream);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            llenarInformacionBasica(workbook.getSheetAt(0), semillero);
            llenarInformacionComplementaria(workbook.getSheetAt(1), semillero, productos);
            llenarInformacionDirector(workbook.getSheetAt(2), semillero);
            llenarInformacionEstudiantes(workbook.getSheetAt(3), integrantes);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error al generar el formato Excel para el semillero {}", semilleroId, e);
            throw new BusinessException("Error al procesar la plantilla Excel: " + e.getMessage());
        }
    }

    private InputStream openTemplateInputStream() throws IOException {
        InputStream stream = getClass().getResourceAsStream(TEMPLATE_RESOURCE_PATH);
        if (stream != null) {
            return stream;
        }

        File file = new File(TEMPLATE_FALLBACK_PATH);
        if (file.exists()) {
            return new FileInputStream(file);
        }

        File file2 = new File(TEMPLATE_FALLBACK_PATH_2);
        if (file2.exists()) {
            return new FileInputStream(file2);
        }

        throw new BusinessException("No se encontró la plantilla de Excel en el servidor");
    }

    private void llenarInformacionBasica(Sheet sheet, Semillero semillero) {
        // C7: Nombre del Semillero (fila 6, columna 2)
        setCellValue(sheet, 6, 2, semillero.getNombre());

        // C8: Sigla (fila 7, columna 2)
        setCellValue(sheet, 7, 2, semillero.getSigla());

        // C9: Grupo de Investigación (fila 8, columna 2)
        String grupoNombre = semillero.getGrupoInvestigacion() != null
                ? semillero.getGrupoInvestigacion().getNombre()
                : "";
        setCellValue(sheet, 8, 2, grupoNombre);

        // C11: Marcador para área de investigación "Ingenierías" (fila 10, columna 2)
        setCellValue(sheet, 10, 2, "X");
    }

    private void llenarInformacionComplementaria(Sheet sheet, Semillero semillero, List<Producto> productos) {
        // C8: Líneas de investigación (fila 7, columna 2)
        String lineasStr = (semillero.getLineas() != null && !semillero.getLineas().isEmpty())
                ? semillero.getLineas().stream()
                        .map(Linea::getNombre)
                        .collect(Collectors.joining(", "))
                : "";
        setCellValue(sheet, 7, 2, lineasStr);

        // C9: Misión (fila 8, columna 2)
        setCellValue(sheet, 8, 2, semillero.getMision());

        // C10: Visión (fila 9, columna 2)
        setCellValue(sheet, 9, 2, semillero.getVision());

        // C11: Objetivo General (fila 10, columna 2)
        setCellValue(sheet, 10, 2, semillero.getObjetivoGeneral());

        // C15: Publicaciones o Productos (fila 14, columna 2)
        String productosStr;
        if (productos.isEmpty()) {
            productosStr = "Sin productos o publicaciones registradas a la fecha.";
        } else {
            productosStr = productos.stream()
                    .map(p -> "- [" + (p.getTipo() != null ? p.getTipo().getNombre() : "Producto") + "] "
                            + p.getNombre()
                            + (p.getDescripcion() != null && !p.getDescripcion().isBlank() ? ": " + p.getDescripcion() : ""))
                    .collect(Collectors.joining("\n"));
        }
        setCellValue(sheet, 14, 2, productosStr);
    }

    private void llenarInformacionDirector(Sheet sheet, Semillero semillero) {
        // C8: Nombre Completo (fila 7, columna 2)
        String directorNombre = semillero.getDirector() != null ? semillero.getDirector().getNombre() : "";
        setCellValue(sheet, 7, 2, directorNombre);

        // C12: Departamento al que pertenece (fila 11, columna 2)
        String departamento = semillero.getGrupoInvestigacion() != null
                ? semillero.getGrupoInvestigacion().getDepartamento()
                : "";
        setCellValue(sheet, 11, 2, departamento);

        // C17: Correo electrónico (fila 16, columna 2)
        String directorCorreo = semillero.getDirector() != null ? semillero.getDirector().getCorreo() : "";
        setCellValue(sheet, 16, 2, directorCorreo);
    }

    private void llenarInformacionEstudiantes(Sheet sheet, List<Integrante> integrantes) {
        // Ordenar estudiantes: Líder primero, luego alfabéticamente por nombre
        integrantes.sort(
                Comparator.comparing((Integrante i) -> Boolean.TRUE.equals(i.getEsLider()), Comparator.reverseOrder())
                        .thenComparing(Integrante::getNombre, String.CASE_INSENSITIVE_ORDER)
        );

        // Fila 8 (índice 7) es el primer estudiante en la plantilla
        // Filas 8 a 34 (índices 7 a 33, total 27 filas) ya vienen preformateadas en la plantilla
        Row sampleRow = sheet.getRow(7);
        if (sampleRow == null) {
            sampleRow = sheet.createRow(7);
        }

        int totalIntegrantes = integrantes.size();
        for (int i = 0; i < totalIntegrantes; i++) {
            Integrante integrante = integrantes.get(i);
            int rowIndex = 7 + i;

            Row row;
            if (rowIndex <= 33) {
                row = sheet.getRow(rowIndex);
                if (row == null) {
                    row = sheet.createRow(rowIndex);
                }
            } else {
                row = sheet.createRow(rowIndex);
                row.setHeight(sampleRow.getHeight());
                for (int col = 0; col < 10; col++) {
                    Cell newCell = row.createCell(col);
                    Cell sampleCell = sampleRow.getCell(col);
                    if (sampleCell != null && sampleCell.getCellStyle() != null) {
                        newCell.setCellStyle(sampleCell.getCellStyle());
                    }
                }
            }

            // Col 0: No. (numérico)
            Cell cellNo = getOrCreateCell(row, 0);
            cellNo.setCellValue(i + 1);

            // Col 1: NOMBRE COMPLETO
            setCellValue(row, 1, integrante.getNombre());

            // Col 2: IDENTIFICACIÓN
            setCellValue(row, 2, integrante.getIdentificacion());

            // Col 3: PROGRAMA ACADÉMICO
            String programa = integrante.getPrograma() != null ? integrante.getPrograma().getNombre() : "";
            setCellValue(row, 3, programa);

            // Col 4: CÓDIGO
            setCellValue(row, 4, integrante.getCodigo());

            // Col 5: SEMESTRE ACTUAL
            if (integrante.getSemestreActual() != null) {
                Cell cellSem = getOrCreateCell(row, 5);
                cellSem.setCellValue(integrante.getSemestreActual());
            } else {
                setCellValue(row, 5, "");
            }

            // Col 6: EDAD
            if (integrante.getEdad() != null) {
                Cell cellEdad = getOrCreateCell(row, 6);
                cellEdad.setCellValue(integrante.getEdad());
            } else {
                setCellValue(row, 6, "");
            }

            // Col 7: DIRECCIÓN
            setCellValue(row, 7, integrante.getDireccion());

            // Col 8: E-MAIL
            setCellValue(row, 8, integrante.getEmail());

            // Col 9: CELULAR
            setCellValue(row, 9, integrante.getCelular());
        }

        // Limpiar filas preformateadas no utilizadas para no dejar números huérfanos
        for (int r = 7 + totalIntegrantes; r <= 33; r++) {
            Row row = sheet.getRow(r);
            if (row != null) {
                for (int col = 0; col < 10; col++) {
                    Cell cell = row.getCell(col);
                    if (cell != null) {
                        cell.setBlank();
                    }
                }
            }
        }
    }

    private Row getOrCreateRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        return row;
    }

    private Cell getOrCreateCell(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        return cell;
    }

    private void setCellValue(Sheet sheet, int rowIndex, int colIndex, String value) {
        Row row = getOrCreateRow(sheet, rowIndex);
        setCellValue(row, colIndex, value);
    }

    private void setCellValue(Row row, int colIndex, String value) {
        Cell cell = getOrCreateCell(row, colIndex);
        cell.setCellValue(value != null ? value : "");
    }
}
