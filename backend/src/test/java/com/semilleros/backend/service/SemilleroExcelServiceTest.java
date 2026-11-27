package com.semilleros.backend.service;

import com.semilleros.backend.entity.Director;
import com.semilleros.backend.entity.GrupoInvestigacion;
import com.semilleros.backend.entity.Integrante;
import com.semilleros.backend.entity.Linea;
import com.semilleros.backend.entity.Producto;
import com.semilleros.backend.entity.ProgramaAcademico;
import com.semilleros.backend.entity.Semillero;
import com.semilleros.backend.entity.TipoProducto;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.IntegranteRepository;
import com.semilleros.backend.repository.ProductoRepository;
import com.semilleros.backend.repository.SemilleroRepository;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemilleroExcelServiceTest {

    @Mock
    private SemilleroRepository semilleroRepository;

    @Mock
    private IntegranteRepository integranteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private SemilleroExcelService semilleroExcelService;

    @Test
    void generarFormatoActualizacion_shouldGenerateValidExcel_whenSemilleroExists() throws IOException {
        Long semilleroId = 1L;

        GrupoInvestigacion grupo = GrupoInvestigacion.builder()
                .id(10L)
                .nombre("Grupo de Software Inteligente")
                .sigla("GSI")
                .departamento("Ingeniería de Sistemas e Informática")
                .build();

        Director director = Director.builder()
                .id(20L)
                .nombre("Dra. María Gómez")
                .correo("mariagomez@ufps.edu.co")
                .build();

        Linea linea1 = Linea.builder().id(1L).nombre("Inteligencia Artificial").build();
        Linea linea2 = Linea.builder().id(2L).nombre("Ingeniería de Software").build();

        Semillero semillero = Semillero.builder()
                .id(semilleroId)
                .nombre("Semillero de Computación y Datos Avanzados")
                .sigla("SCDA")
                .mision("Fomentar la investigación estudiantil")
                .vision("Ser un referente regional")
                .objetivoGeneral("Desarrollar capacidades de I+D")
                .grupoInvestigacion(grupo)
                .director(director)
                .lineas(Set.of(linea1, linea2))
                .build();

        ProgramaAcademico programa = ProgramaAcademico.builder()
                .id(1L)
                .nombre("Ingeniería de Sistemas")
                .build();

        Integrante integranteRegular = Integrante.builder()
                .id(101L)
                .nombre("Ana Benítez")
                .identificacion("1090123456")
                .codigo("1151901")
                .semestreActual(7)
                .fechaNacimiento(LocalDate.of(2002, 5, 10))
                .direccion("Av. 0 # 10-20")
                .email("ana@ufps.edu.co")
                .celular("3001234567")
                .esLider(false)
                .programa(programa)
                .semillero(semillero)
                .build();

        Integrante integranteLider = Integrante.builder()
                .id(102L)
                .nombre("Carlos Pérez")
                .identificacion("1090654321")
                .codigo("1151802")
                .semestreActual(9)
                .fechaNacimiento(LocalDate.of(2000, 8, 15))
                .direccion("Calle 5 # 4-32")
                .email("carlos@ufps.edu.co")
                .celular("3109876543")
                .esLider(true)
                .programa(programa)
                .semillero(semillero)
                .build();

        List<Integrante> integrantes = new ArrayList<>(List.of(integranteRegular, integranteLider));

        Producto producto = Producto.builder()
                .id(201L)
                .nombre("Modelo predictivo de deserción estudiantil")
                .descripcion("Software de predicción con Random Forest")
                .tipo(TipoProducto.SOFTWARE)
                .semillero(semillero)
                .build();

        when(semilleroRepository.findById(semilleroId)).thenReturn(Optional.of(semillero));
        when(integranteRepository.findBySemilleroId(semilleroId)).thenReturn(integrantes);
        when(productoRepository.findBySemilleroId(semilleroId)).thenReturn(List.of(producto));

        byte[] bytes = semilleroExcelService.generarFormatoActualizacion(semilleroId);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(4, workbook.getNumberOfSheets());

            // Sheet 1: Información Básica
            Sheet sheet1 = workbook.getSheetAt(0);
            assertEquals("Semillero de Computación y Datos Avanzados", sheet1.getRow(6).getCell(2).getStringCellValue());
            assertEquals("SCDA", sheet1.getRow(7).getCell(2).getStringCellValue());
            assertEquals("Grupo de Software Inteligente", sheet1.getRow(8).getCell(2).getStringCellValue());
            assertEquals("X", sheet1.getRow(10).getCell(2).getStringCellValue());

            // Sheet 2: Información Complementaria
            Sheet sheet2 = workbook.getSheetAt(1);
            String lineasVal = sheet2.getRow(7).getCell(2).getStringCellValue();
            assertTrue(lineasVal.contains("Inteligencia Artificial") || lineasVal.contains("Ingeniería de Software"));
            assertEquals("Fomentar la investigación estudiantil", sheet2.getRow(8).getCell(2).getStringCellValue());
            assertEquals("Ser un referente regional", sheet2.getRow(9).getCell(2).getStringCellValue());
            assertEquals("Desarrollar capacidades de I+D", sheet2.getRow(10).getCell(2).getStringCellValue());
            assertTrue(sheet2.getRow(14).getCell(2).getStringCellValue().contains("Modelo predictivo"));

            // Sheet 3: Información Director
            Sheet sheet3 = workbook.getSheetAt(2);
            assertEquals("Dra. María Gómez", sheet3.getRow(7).getCell(2).getStringCellValue());
            assertEquals("Ingeniería de Sistemas e Informática", sheet3.getRow(11).getCell(2).getStringCellValue());
            assertEquals("mariagomez@ufps.edu.co", sheet3.getRow(16).getCell(2).getStringCellValue());

            // Sheet 4: Información Estudiantes (Líder debe estar en la fila 8 / índice 7)
            Sheet sheet4 = workbook.getSheetAt(3);
            assertEquals(1.0, sheet4.getRow(7).getCell(0).getNumericCellValue());
            assertEquals("Carlos Pérez", sheet4.getRow(7).getCell(1).getStringCellValue());
            assertEquals("1090654321", sheet4.getRow(7).getCell(2).getStringCellValue());
            assertEquals("Ingeniería de Sistemas", sheet4.getRow(7).getCell(3).getStringCellValue());
            assertEquals("1151802", sheet4.getRow(7).getCell(4).getStringCellValue());
            assertEquals(9.0, sheet4.getRow(7).getCell(5).getNumericCellValue());

            // Fila 9 / índice 8 debe ser Ana Benítez
            assertEquals(2.0, sheet4.getRow(8).getCell(0).getNumericCellValue());
            assertEquals("Ana Benítez", sheet4.getRow(8).getCell(1).getStringCellValue());
        }
    }

    @Test
    void generarFormatoActualizacion_shouldThrowResourceNotFound_whenSemilleroDoesNotExist() {
        when(semilleroRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                semilleroExcelService.generarFormatoActualizacion(999L));
    }
}
