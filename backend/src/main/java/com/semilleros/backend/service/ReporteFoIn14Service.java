package com.semilleros.backend.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.semilleros.backend.entity.Actividad;
import com.semilleros.backend.entity.Capacitacion;
import com.semilleros.backend.entity.Integrante;
import com.semilleros.backend.entity.Producto;
import com.semilleros.backend.entity.Semillero;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.ActividadRepository;
import com.semilleros.backend.repository.CapacitacionRepository;
import com.semilleros.backend.repository.IntegranteRepository;
import com.semilleros.backend.repository.ProductoRepository;
import com.semilleros.backend.repository.SemilleroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de generar el reporte institucional en PDF correspondiente al
 * formato FO-IN-14 INFORME GESTIÓN SEM INV V2 (Informe de Gestión de Semilleros de Investigación)
 * de la Universidad Francisco de Paula Santander (UFPS).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteFoIn14Service {

    private final SemilleroRepository semilleroRepository;
    private final CapacitacionRepository capacitacionRepository;
    private final ActividadRepository actividadRepository;
    private final ProductoRepository productoRepository;
    private final IntegranteRepository integranteRepository;

    // Paleta de colores institucionales UFPS
    private static final Color RED_UFPS = new Color(170, 25, 22);       // #AA1916 Rojo Institucional
    private static final Color GRAY_HEADER = new Color(217, 217, 217);   // #D9D9D9 Encabezados de tabla
    private static final Color GRAY_LABEL = new Color(238, 238, 238);    // #EEEEEE Celdas de etiquetas
    private static final Color BORDER_COLOR = Color.BLACK;
    private static final Color INSTRUCTION_BG = new Color(245, 248, 252); // Fondo instrucciones suave
    private static final Color PEACH_FIRMA = new Color(254, 246, 243);   // Tono suave para recuadro de firma

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Genera el PDF del formato FO-IN-14 para el semillero y periodo especificado.
     *
     * @param semilleroId Identificador único del semillero.
     * @param periodo     Periodo académico (ej. "2024-1", "2025-1"). Opcional.
     * @return Arreglo de bytes con el contenido binario del PDF.
     */
    @Transactional(readOnly = true)
    public byte[] generarReporteFoIn14(Long semilleroId, String periodo) {
        Semillero semillero = semilleroRepository.findById(semilleroId)
                .orElseThrow(() -> new ResourceNotFoundException("Semillero no encontrado con ID: " + semilleroId));

        List<Capacitacion> capacitaciones = capacitacionRepository.findBySemilleroId(semilleroId);
        List<Actividad> actividades = actividadRepository.findBySemilleroId(semilleroId);
        List<Producto> productos = productoRepository.findBySemilleroId(semilleroId);
        List<Integrante> integrantes = integranteRepository.findBySemilleroId(semilleroId);

        byte[] logoBytes = loadUfpsLogoBytes();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Documento tamaño Carta vertical (Portrait) con márgenes que reservan espacio para encabezado y pie
            Document document = new Document(PageSize.LETTER, 28f, 28f, 105f, 38f);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Manejador de eventos para encabezado institucional y pie de página en cada página
            HeaderFooterPageEvent eventHelper = new HeaderFooterPageEvent(logoBytes);
            writer.setPageEvent(eventHelper);

            document.open();

            // 1. Tabla de Información General del Semillero
            document.add(buildInfoGeneralTable(semillero));

            // Nota introductoria previa a las tablas
            Paragraph pPlanAccion = new Paragraph(
                    "Teniendo en cuenta el plan de acción presentado para desarrollar en el semestre académico actual, " +
                    "indique frente a cada producto el porcentaje de cumplimiento.",
                    new Font(Font.HELVETICA, 7.5f, Font.NORMAL, Color.BLACK)
            );
            pPlanAccion.setSpacingBefore(6f);
            pPlanAccion.setSpacingAfter(6f);
            document.add(pPlanAccion);

            // 2. Sección 1: Proyectos de Investigación
            document.add(buildSectionHeaderTable("1. Proyectos de Investigación"));
            document.add(buildProyectosTable(productos));

            // Espacio entre secciones
            Paragraph spacer1 = new Paragraph(" ", new Font(Font.HELVETICA, 4f));
            spacer1.setSpacingBefore(2f);
            spacer1.setSpacingAfter(2f);
            document.add(spacer1);

            // 3. Sección 2: Capacitaciones
            document.add(buildSectionHeaderTable("2. Capacitaciones"));
            document.add(buildCapacitacionesTable(capacitaciones));

            Paragraph spacer2 = new Paragraph(" ", new Font(Font.HELVETICA, 4f));
            spacer2.setSpacingBefore(2f);
            spacer2.setSpacingAfter(2f);
            document.add(spacer2);

            // 4. Sección 3: Otras Actividades de Investigación
            document.add(buildSectionHeaderTable("3. Otras Actividades de Investigación (*)"));
            document.add(buildActividadesTable(actividades));

            // Nota de pie de sección 3
            Paragraph pNotaActividades = new Paragraph(
                    "(*) Actividades de investigación: Espacios de socialización, Foros, Reuniones, Elaboración de documentos, entre otros.",
                    new Font(Font.HELVETICA, 7f, Font.BOLD, Color.BLACK)
            );
            pNotaActividades.setSpacingBefore(3f);
            pNotaActividades.setSpacingAfter(8f);
            document.add(pNotaActividades);

            // 5. Sección: Productos obtenidos en el semestre académico
            Paragraph pIntroProductos = new Paragraph(
                    "A continuación, describa los productos obtenidos por el grupo de investigación en el semestre actual, " +
                    "según lo establecido en el Acuerdo que adopta el Sistema de Investigación de la Universidad Francisco de Paula Santander.",
                    new Font(Font.HELVETICA, 7.5f, Font.NORMAL, Color.BLACK)
            );
            pIntroProductos.setSpacingBefore(4f);
            pIntroProductos.setSpacingAfter(6f);
            document.add(pIntroProductos);

            document.add(buildProductosObtenidosTable(semillero, capacitaciones, productos, periodo));

            // Nota sobre soportes de productos
            Paragraph pNotaSoportes = new Paragraph(
                    "NOTA:   Los productos a evaluar son los establecidos Acuerdo que adopta el Sistema de Investigación de la Universidad " +
                    "Francisco de Paula Santander. Se solicita anexar los soportes de los productos registrados en el periodo académico, " +
                    "solo se tendrá en cuenta para el reconocimiento de las horas de Investigación los productos que presenten su respectivo " +
                    "soporte (Ejemplo: Listas de Asistencia a las Capacitaciones, evidencias fotográficas).",
                    new Font(Font.HELVETICA, 6.8f, Font.NORMAL, Color.BLACK)
            );
            pNotaSoportes.setAlignment(Element.ALIGN_JUSTIFIED);
            pNotaSoportes.setSpacingBefore(4f);
            pNotaSoportes.setSpacingAfter(8f);
            document.add(pNotaSoportes);

            // 6. Sección 4: Relación de estudiantes
            document.add(buildSectionHeaderTable(
                    "4. Relación estudiantes del semillero en el semestre académico actual " +
                    "(Se deberá relacionar los estudiantes con al menos 80% de participación en las reuniones de semillero)"
            ));

            String cantidadEstudiantesStr = integrantes.isEmpty() ? "___" : String.valueOf(integrantes.size());
            Paragraph pEstudiantes = new Paragraph(
                    "Como se muestra en el anexo FO-IN-12 ESTUDIANTES DE SEMILLEROS DE INVESTIGACION QUE CUMPLIERON CON REQUERIMIENTO " +
                    "DE PARTICIPACIÓN, la cantidad de estudiantes que cumplieron con al menos el 80% de participación en las reuniones " +
                    "de semillero, corresponde a " + cantidadEstudiantesStr + " (indicar número de estudiantes).",
                    new Font(Font.HELVETICA, 7.5f, Font.NORMAL, Color.BLACK)
            );
            pEstudiantes.setSpacingBefore(5f);
            pEstudiantes.setSpacingAfter(10f);
            document.add(pEstudiantes);

            // 7. Sección de Firmas (Conservar juntas para evitar particiones incómodas)
            document.add(buildFirmasTable(semillero));

            document.close();

            log.info("Reporte FO-IN-14 generado exitosamente para semillero ID: {} ({})", semilleroId, semillero.getSigla());
            return baos.toByteArray();
        } catch (DocumentException e) {
            log.error("Error al construir el documento PDF FO-IN-14", e);
            throw new RuntimeException("Error al generar el archivo PDF del reporte FO-IN-14: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado en generación de reporte FO-IN-14", e);
            throw new RuntimeException("Error inesperado al generar reporte FO-IN-14: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // CONSTRUCCIÓN DE TABLAS Y COMPONENTES VISUALES
    // =========================================================================

    /**
     * Construye la tabla con la información general del semillero.
     */
    private PdfPTable buildInfoGeneralTable(Semillero semillero) {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        try {
            table.setWidths(new float[]{24f, 26f, 22f, 28f});
        } catch (DocumentException ignored) {}

        Font fontLabel = new Font(Font.HELVETICA, 7.5f, Font.BOLD, Color.BLACK);
        Font fontValue = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, Color.BLACK);
        Font fontValueBold = new Font(Font.HELVETICA, 8f, Font.BOLD, Color.BLACK);

        // Fila 1: SEMILLERO DE INVESTIGACIÓN (Etiqueta col 1, Valor cols 2-4)
        PdfPCell cSemilleroLabel = createCell("SEMILLERO DE INVESTIGACIÓN", fontLabel, GRAY_LABEL, Element.ALIGN_LEFT, 5f);
        table.addCell(cSemilleroLabel);

        String semilleroNombre = semillero.getNombre() != null ? semillero.getNombre().toUpperCase() : "";
        if (semillero.getSigla() != null && !semillero.getSigla().isBlank()) {
            semilleroNombre += " (" + semillero.getSigla() + ")";
        }
        PdfPCell cSemilleroVal = createCell(semilleroNombre, fontValueBold, Color.WHITE, Element.ALIGN_LEFT, 5f);
        cSemilleroVal.setColspan(3);
        table.addCell(cSemilleroVal);

        // Fila 2: DIRECTOR (cols 1 y cols 2-4)
        table.addCell(createCell("DIRECTOR", fontLabel, GRAY_LABEL, Element.ALIGN_LEFT, 4f));
        String directorNombre = semillero.getDirector() != null ? semillero.getDirector().getNombre() : "";
        PdfPCell cDirVal = createCell(directorNombre, fontValue, Color.WHITE, Element.ALIGN_LEFT, 4f);
        cDirVal.setColspan(3);
        table.addCell(cDirVal);

        // Fila 3: CELULAR / CORREO ELECTRÓNICO
        table.addCell(createCell("CELULAR", fontLabel, GRAY_LABEL, Element.ALIGN_LEFT, 4f));
        table.addCell(createCell("", fontValue, Color.WHITE, Element.ALIGN_LEFT, 4f)); // Campo vacío si no está disponible

        table.addCell(createCell("CORREO ELECTRÓNICO:", fontLabel, GRAY_LABEL, Element.ALIGN_LEFT, 4f));
        String directorCorreo = semillero.getDirector() != null ? semillero.getDirector().getCorreo() : "";
        table.addCell(createCell(directorCorreo, fontValue, Color.WHITE, Element.ALIGN_LEFT, 4f));

        // Fila 4: GRUPO DE INVESTIGACIÓN / DIRECTOR
        table.addCell(createCell("GRUPO DE INVESTIGACIÓN", fontLabel, GRAY_LABEL, Element.ALIGN_LEFT, 4f));
        String grupoNombre = semillero.getGrupoInvestigacion() != null ? semillero.getGrupoInvestigacion().getNombre() : "";
        table.addCell(createCell(grupoNombre, fontValue, Color.WHITE, Element.ALIGN_LEFT, 4f));

        table.addCell(createCell("DIRECTOR:", fontLabel, GRAY_LABEL, Element.ALIGN_LEFT, 4f));
        String grupoLider = semillero.getGrupoInvestigacion() != null ? semillero.getGrupoInvestigacion().getLider() : "";
        table.addCell(createCell(grupoLider, fontValue, Color.WHITE, Element.ALIGN_LEFT, 4f));

        // Fila 5: FACULTAD
        table.addCell(createCell("FACULTAD", fontLabel, GRAY_LABEL, Element.ALIGN_LEFT, 4f));
        String facultad = semillero.getGrupoInvestigacion() != null && semillero.getGrupoInvestigacion().getFacultad() != null
                ? semillero.getGrupoInvestigacion().getFacultad() : "Elija un elemento.";
        PdfPCell cFacultadVal = createCell(facultad, fontValue, Color.WHITE, Element.ALIGN_LEFT, 4f);
        cFacultadVal.setColspan(3);
        table.addCell(cFacultadVal);

        // Fila 6: DEPARTAMENTO
        table.addCell(createCell("DEPARTAMENTO", fontLabel, GRAY_LABEL, Element.ALIGN_LEFT, 4f));
        String departamento = semillero.getGrupoInvestigacion() != null && semillero.getGrupoInvestigacion().getDepartamento() != null
                ? semillero.getGrupoInvestigacion().getDepartamento() : "Elija un elemento.";
        PdfPCell cDeptoVal = createCell(departamento, fontValue, Color.WHITE, Element.ALIGN_LEFT, 4f);
        cDeptoVal.setColspan(3);
        table.addCell(cDeptoVal);

        return table;
    }

    /**
     * Barra roja de título de sección.
     */
    private PdfPTable buildSectionHeaderTable(String title) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100f);

        PdfPCell cell = new PdfPCell(new Phrase(title, new Font(Font.HELVETICA, 8.5f, Font.BOLD, Color.WHITE)));
        cell.setBackgroundColor(RED_UFPS);
        cell.setBorderColor(BORDER_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3.5f);
        table.addCell(cell);

        return table;
    }

    /**
     * Tabla Sección 1: Proyectos de Investigación.
     */
    private PdfPTable buildProyectosTable(List<Producto> productos) {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        try {
            table.setWidths(new float[]{32f, 30f, 16f, 22f});
        } catch (DocumentException ignored) {}
        table.setHeaderRows(1);

        Font fontHeader = new Font(Font.HELVETICA, 7.5f, Font.BOLD, Color.BLACK);
        Font fontCell = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, Color.BLACK);

        table.addCell(createCell("Nombre Proyecto", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));
        table.addCell(createCell("Actividades", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));
        table.addCell(createCell("% de Cumplimiento", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));
        table.addCell(createCell("Ejecutor (Autores)", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));

        // Filtrar productos relacionados a proyectos si existen
        int rowsCount = 0;
        if (productos != null) {
            for (Producto p : productos) {
                if (p.getTipo() != null && p.getTipo().name().contains("PROYECTO")) {
                    String autores = (p.getIntegrantes() != null)
                            ? p.getIntegrantes().stream().map(Integrante::getNombre).collect(Collectors.joining(", "))
                            : "";
                    table.addCell(createCell(p.getNombre(), fontCell, Color.WHITE, Element.ALIGN_LEFT, 3.5f));
                    table.addCell(createCell(p.getDescripcion() != null ? p.getDescripcion() : "", fontCell, Color.WHITE, Element.ALIGN_LEFT, 3.5f));
                    table.addCell(createCell("100%", fontCell, Color.WHITE, Element.ALIGN_CENTER, 3.5f));
                    table.addCell(createCell(autores, fontCell, Color.WHITE, Element.ALIGN_LEFT, 3.5f));
                    rowsCount++;
                }
            }
        }

        // Si no hay proyectos suficientes, completar con filas vacías para preservar el formato institucional
        int targetRows = Math.max(4, rowsCount);
        for (int i = rowsCount; i < targetRows; i++) {
            table.addCell(createEmptyCell(18f));
            table.addCell(createEmptyCell(18f));
            table.addCell(createEmptyCell(18f));
            table.addCell(createEmptyCell(18f));
        }

        return table;
    }

    /**
     * Tabla Sección 2: Capacitaciones.
     */
    private PdfPTable buildCapacitacionesTable(List<Capacitacion> capacitaciones) {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        try {
            table.setWidths(new float[]{30f, 38f, 16f, 16f});
        } catch (DocumentException ignored) {}
        table.setHeaderRows(1);

        Font fontHeader = new Font(Font.HELVETICA, 7.5f, Font.BOLD, Color.BLACK);
        Font fontCell = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, Color.BLACK);
        Font fontCellMuted = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, Color.GRAY);

        table.addCell(createCell("Nombre de la Capacitación", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));
        table.addCell(createCell("Objetivo", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));
        table.addCell(createCell("Fecha de realización", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));
        table.addCell(createCell("% de Cumplimiento", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));

        int rowsCount = 0;
        if (capacitaciones != null) {
            for (Capacitacion c : capacitaciones) {
                String fechaStr = c.getFecha() != null ? c.getFecha().format(DATE_FORMATTER) : "";
                String pctStr = c.getPorcentajeCumplimiento() != null
                        ? String.format("%.0f%%", c.getPorcentajeCumplimiento()) : "";

                table.addCell(createCell(c.getNombreCapacitacion(), fontCell, Color.WHITE, Element.ALIGN_LEFT, 3.5f));
                table.addCell(createCell(c.getObjetivoCapacitacion() != null ? c.getObjetivoCapacitacion() : "", fontCell, Color.WHITE, Element.ALIGN_LEFT, 3.5f));
                table.addCell(createCell(fechaStr, fontCell, Color.WHITE, Element.ALIGN_CENTER, 3.5f));
                table.addCell(createCell(pctStr, fontCell, Color.WHITE, Element.ALIGN_CENTER, 3.5f));
                rowsCount++;
            }
        }

        // Completar hasta al menos 4 filas como en el formato original
        int targetRows = Math.max(4, rowsCount);
        for (int i = rowsCount; i < targetRows; i++) {
            table.addCell(createEmptyCell(18f));
            table.addCell(createEmptyCell(18f));
            table.addCell(createCell("fecha.", fontCellMuted, Color.WHITE, Element.ALIGN_CENTER, 3.5f));
            table.addCell(createEmptyCell(18f));
        }

        return table;
    }

    /**
     * Tabla Sección 3: Otras Actividades de Investigación.
     */
    private PdfPTable buildActividadesTable(List<Actividad> actividades) {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100f);
        try {
            table.setWidths(new float[]{50f, 32f, 18f});
        } catch (DocumentException ignored) {}
        table.setHeaderRows(1);

        Font fontHeader = new Font(Font.HELVETICA, 7.5f, Font.BOLD, Color.BLACK);
        Font fontCell = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, Color.BLACK);

        table.addCell(createCell("Nombre", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));
        table.addCell(createCell("Tipo de Actividad", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));
        table.addCell(createCell("% de Cumplimiento", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));

        int rowsCount = 0;
        if (actividades != null) {
            for (Actividad a : actividades) {
                String tipoStr = a.getTipoActividad() != null ? a.getTipoActividad().getNombre() : "";
                String pctStr = a.getPorcentajeCumplimiento() != null ? a.getPorcentajeCumplimiento() + "%" : "";

                table.addCell(createCell(a.getNombre(), fontCell, Color.WHITE, Element.ALIGN_LEFT, 3.5f));
                table.addCell(createCell(tipoStr, fontCell, Color.WHITE, Element.ALIGN_LEFT, 3.5f));
                table.addCell(createCell(pctStr, fontCell, Color.WHITE, Element.ALIGN_CENTER, 3.5f));
                rowsCount++;
            }
        }

        int targetRows = Math.max(4, rowsCount);
        for (int i = rowsCount; i < targetRows; i++) {
            table.addCell(createEmptyCell(18f));
            table.addCell(createEmptyCell(18f));
            table.addCell(createEmptyCell(18f));
        }

        return table;
    }

    /**
     * Tabla de Productos obtenidos en el semestre académico con las 4 categorías institucionales.
     */
    private PdfPTable buildProductosObtenidosTable(
            Semillero semillero,
            List<Capacitacion> capacitaciones,
            List<Producto> productos,
            String periodo
    ) {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        try {
            table.setWidths(new float[]{25f, 38f, 24f, 13f});
        } catch (DocumentException ignored) {}
        table.setHeaderRows(1);

        Font fontHeader = new Font(Font.HELVETICA, 7.5f, Font.BOLD, Color.BLACK);
        Font fontTitleCat = new Font(Font.HELVETICA, 7.2f, Font.NORMAL, Color.BLACK);
        Font fontInstruccion = new Font(Font.HELVETICA, 7f, Font.BOLD, new Color(40, 40, 40));
        Font fontCell = new Font(Font.HELVETICA, 7.2f, Font.NORMAL, Color.BLACK);
        Font fontCellMuted = new Font(Font.HELVETICA, 7.2f, Font.NORMAL, Color.GRAY);

        // Encabezados
        table.addCell(createCell("PRODUCTO", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));
        table.addCell(createCell("DESCRIPCIÓN", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));
        table.addCell(createCell("RESPONSABLE", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));
        table.addCell(createCell("FECHA", fontHeader, GRAY_HEADER, Element.ALIGN_CENTER, 3.5f));

        // Categoría 1: Proyectos terminados y/o ejecución
        table.addCell(createCell(
                "Proyectos terminados y/o ejecución, avalado financiación interna(FINU) o externa",
                fontTitleCat, Color.WHITE, Element.ALIGN_LEFT, 4f
        ));
        table.addCell(createCell(
                "Instrucciones: Se debe escribir el nombre de él(los) proyecto(s), en ejecución o terminado(s).",
                fontInstruccion, INSTRUCTION_BG, Element.ALIGN_LEFT, 4f
        ));
        table.addCell(createEmptyCell(18f));
        table.addCell(createCell("fecha.", fontCellMuted, Color.WHITE, Element.ALIGN_CENTER, 4f));

        // Categoría 2: Capacitaciones (4) correspondientes a las áreas formación
        table.addCell(createCell(
                "Capacitaciones (4) correspondientes a las áreas formación",
                fontTitleCat, Color.WHITE, Element.ALIGN_LEFT, 4f
        ));
        String descCapacitaciones = "Instrucciones: Se debe escribir el nombre de las 4 capacitaciones realizadas, correspondientes a las áreas de formación.";
        if (capacitaciones != null && !capacitaciones.isEmpty()) {
            String nombresCap = capacitaciones.stream()
                    .map(c -> "• " + c.getNombreCapacitacion())
                    .collect(Collectors.joining("\n"));
            descCapacitaciones += "\n" + nombresCap;
        }
        table.addCell(createCell(descCapacitaciones, fontInstruccion, INSTRUCTION_BG, Element.ALIGN_LEFT, 4f));
        String dirNombre = semillero.getDirector() != null ? semillero.getDirector().getNombre() : "";
        table.addCell(createCell(dirNombre, fontCell, Color.WHITE, Element.ALIGN_LEFT, 4f));
        String fechaUltimaCap = (!capacitaciones.isEmpty() && capacitaciones.get(0).getFecha() != null)
                ? capacitaciones.get(0).getFecha().format(DATE_FORMATTER) : "fecha.";
        table.addCell(createCell(fechaUltimaCap, fontCellMuted, Color.WHITE, Element.ALIGN_CENTER, 4f));

        // Categoría 3: Participación en capacitaciones y espacios de socialización de la VIE
        table.addCell(createCell(
                "Participación en capacitaciones y espacios de socialización de la Vicerrectoría Asistente de Investigación y Extensión",
                fontTitleCat, Color.WHITE, Element.ALIGN_LEFT, 4f
        ));
        table.addCell(createCell(
                "Instrucciones: Se debe escribir el nombre de la(s) capacitación(es) o espacio(s) de socialización de la Vicerrectoría Asistente de Investigación y Extensión.",
                fontInstruccion, INSTRUCTION_BG, Element.ALIGN_LEFT, 4f
        ));
        table.addCell(createEmptyCell(18f));
        table.addCell(createCell("fecha.", fontCellMuted, Color.WHITE, Element.ALIGN_CENTER, 4f));

        // Categoría 4: Otros productos
        table.addCell(createCell("Otros productos", fontTitleCat, Color.WHITE, Element.ALIGN_LEFT, 4f));
        String descOtros = "Instrucciones: Se debe escribir el nombre de el(los) producto(s)";
        String autoresOtros = "";
        if (productos != null && !productos.isEmpty()) {
            StringBuilder sbProd = new StringBuilder(descOtros);
            for (Producto p : productos) {
                sbProd.append("\n• ").append(p.getNombre()).append(": ").append(p.getDescripcion());
                if (p.getIntegrantes() != null && !p.getIntegrantes().isEmpty()) {
                    autoresOtros = p.getIntegrantes().stream().map(Integrante::getNombre).collect(Collectors.joining(", "));
                }
            }
            descOtros = sbProd.toString();
        }
        table.addCell(createCell(descOtros, fontInstruccion, INSTRUCTION_BG, Element.ALIGN_LEFT, 4f));
        table.addCell(createCell(autoresOtros, fontCell, Color.WHITE, Element.ALIGN_LEFT, 4f));
        table.addCell(createCell("fecha.", fontCellMuted, Color.WHITE, Element.ALIGN_CENTER, 4f));

        return table;
    }

    /**
     * Construye la cuadrícula de 4 bloques de firmas oficiales UFPS.
     */
    private PdfPTable buildFirmasTable(Semillero semillero) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100f);
        try {
            table.setWidths(new float[]{50f, 50f});
        } catch (DocumentException ignored) {}
        table.setKeepTogether(true);

        String directorSemillero = semillero.getDirector() != null ? semillero.getDirector().getNombre() : "";
        String directorGrupo = semillero.getGrupoInvestigacion() != null ? semillero.getGrupoInvestigacion().getLider() : "";
        String directorDepto = semillero.getGrupoInvestigacion() != null && semillero.getGrupoInvestigacion().getDepartamento() != null
                ? "Dpto. " + semillero.getGrupoInvestigacion().getDepartamento() : "";

        // Fila 1: Elaborado Por (Director Semillero) / Revisado Por (Director Grupo)
        PdfPCell box1 = buildSingleFirmaBox("ELABORADO POR", "Director Semillero de Investigación", directorSemillero);
        PdfPCell box2 = buildSingleFirmaBox("REVISADO POR", "Director Grupo de Investigación", directorGrupo);
        table.addCell(box1);
        table.addCell(box2);

        // Fila 2: Revisado Por (Director Departamento) / VoBo. Representante CCIE
        PdfPCell box3 = buildSingleFirmaBox("REVISADO POR", "Director de Departamento", directorDepto);
        PdfPCell box4 = buildSingleFirmaBox("REVISADO POR", "VoBo. Representante de la Facultad ante el CCIE :", "");
        table.addCell(box3);
        table.addCell(box4);

        return table;
    }

    /**
     * Construye un cuadro individual de firma compuesto con encabezado rojo, subencabezado gris y campos NOMBRE/FIRMA.
     */
    private PdfPCell buildSingleFirmaBox(String headerText, String roleText, String nombreText) {
        PdfPTable sub = new PdfPTable(2);
        sub.setWidthPercentage(100f);
        try {
            sub.setWidths(new float[]{22f, 78f});
        } catch (DocumentException ignored) {}

        Font fontHead = new Font(Font.HELVETICA, 7.5f, Font.BOLD, Color.WHITE);
        Font fontRole = new Font(Font.HELVETICA, 7.5f, Font.BOLD, Color.BLACK);
        Font fontLabel = new Font(Font.HELVETICA, 7f, Font.BOLD, Color.BLACK);
        Font fontValue = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, Color.BLACK);

        // Encabezado Rojo
        PdfPCell cHead = new PdfPCell(new Phrase(headerText, fontHead));
        cHead.setBackgroundColor(RED_UFPS);
        cHead.setHorizontalAlignment(Element.ALIGN_CENTER);
        cHead.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cHead.setPadding(3f);
        cHead.setColspan(2);
        cHead.setBorderColor(BORDER_COLOR);
        sub.addCell(cHead);

        // Subencabezado Gris con Rol
        PdfPCell cRole = new PdfPCell(new Phrase(roleText, fontRole));
        cRole.setBackgroundColor(GRAY_HEADER);
        cRole.setHorizontalAlignment(Element.ALIGN_CENTER);
        cRole.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cRole.setPadding(3f);
        cRole.setColspan(2);
        cRole.setBorderColor(BORDER_COLOR);
        sub.addCell(cRole);

        // Fila NOMBRE
        PdfPCell cNomLabel = new PdfPCell(new Phrase("NOMBRE:", fontLabel));
        cNomLabel.setPadding(3f);
        cNomLabel.setBorderColor(BORDER_COLOR);
        sub.addCell(cNomLabel);

        PdfPCell cNomVal = new PdfPCell(new Phrase(nombreText != null ? nombreText : "", fontValue));
        cNomVal.setPadding(3f);
        cNomVal.setBorderColor(BORDER_COLOR);
        sub.addCell(cNomVal);

        // Fila FIRMA
        PdfPCell cFirmaLabel = new PdfPCell(new Phrase("FIRMA:", fontLabel));
        cFirmaLabel.setPadding(3f);
        cFirmaLabel.setFixedHeight(34f);
        cFirmaLabel.setBorderColor(BORDER_COLOR);
        sub.addCell(cFirmaLabel);

        PdfPCell cFirmaVal = new PdfPCell();
        cFirmaVal.setPadding(3f);
        cFirmaVal.setFixedHeight(34f);
        cFirmaVal.setBackgroundColor(PEACH_FIRMA);
        cFirmaVal.setBorderColor(BORDER_COLOR);
        sub.addCell(cFirmaVal);

        PdfPCell wrapper = new PdfPCell(sub);
        wrapper.setPadding(3f);
        wrapper.setBorder(Rectangle.NO_BORDER);
        return wrapper;
    }

    // =========================================================================
    // MÉTODOS UTILITARIOS DE CELDAS Y RECURSOS
    // =========================================================================

    private PdfPCell createCell(String text, Font font, Color bg, int align, float padding) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        if (bg != null) {
            cell.setBackgroundColor(bg);
        }
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(padding);
        cell.setBorderColor(BORDER_COLOR);
        return cell;
    }

    private PdfPCell createEmptyCell(float height) {
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(height);
        cell.setBorderColor(BORDER_COLOR);
        return cell;
    }

    private byte[] loadUfpsLogoBytes() {
        try (InputStream is = getClass().getResourceAsStream("/images/ufps_logo.png")) {
            if (is != null) {
                return is.readAllBytes();
            }
        } catch (Exception e) {
            log.warn("No se pudo cargar el logo de la UFPS desde classpath: {}", e.getMessage());
        }
        return null;
    }

    // =========================================================================
    // EVENT HELPER PARA ENCABEZADO Y PIE DE PÁGINA REPETITIVOS
    // =========================================================================

    /**
     * Controla el dibujo del encabezado institucional UFPS y pie de página en cada página,
     * calculando con precisión la numeración total "PÁGINA X de Y".
     */
    private static class HeaderFooterPageEvent extends PdfPageEventHelper {

        private final byte[] logoBytes;
        private PdfTemplate totalPagesTemplate;
        private BaseFont baseFont;

        public HeaderFooterPageEvent(byte[] logoBytes) {
            this.logoBytes = logoBytes;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPagesTemplate = writer.getDirectContent().createTemplate(30f, 14f);
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            } catch (Exception ignored) {}
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                // 1. Encabezado institucional UFPS
                PdfPTable headerTable = buildHeaderTable(writer.getPageNumber());
                headerTable.setTotalWidth(document.right() - document.left());
                // Se posiciona el encabezado en la parte superior fija
                headerTable.writeSelectedRows(0, -1, document.left(), document.top() + 74f, writer.getDirectContent());

                // 2. Pie de página institucional
                PdfPTable footerTable = new PdfPTable(1);
                footerTable.setTotalWidth(document.right() - document.left());

                PdfPCell footCell = new PdfPCell();
                footCell.setBorder(Rectangle.NO_BORDER);
                footCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                Paragraph p1 = new Paragraph("Documento Digital - Copia controlada", new Font(Font.HELVETICA, 6.8f, Font.NORMAL, Color.DARK_GRAY));
                p1.setAlignment(Element.ALIGN_CENTER);
                Paragraph p2 = new Paragraph("(La descarga o impresión de este documento le da carácter de COPIA NO CONTROLADA)", new Font(Font.HELVETICA, 6.2f, Font.NORMAL, Color.GRAY));
                p2.setAlignment(Element.ALIGN_CENTER);

                footCell.addElement(p1);
                footCell.addElement(p2);
                footerTable.addCell(footCell);

                footerTable.writeSelectedRows(0, -1, document.left(), document.bottom() - 8f, writer.getDirectContent());

            } catch (Exception e) {
                log.error("Error al renderizar encabezado o pie de página en página {}", writer.getPageNumber(), e);
            }
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            // El número total de páginas en onCloseDocument corresponde a writer.getPageNumber() - 1
            int totalPages = Math.max(1, writer.getPageNumber() - 1);
            ColumnText.showTextAligned(
                    totalPagesTemplate,
                    Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(totalPages), new Font(baseFont, 6.8f, Font.BOLD, Color.BLACK)),
                    1f, 1f, 0
            );
        }

        /**
         * Construye la estructura exacta del encabezado FO-IN-14.
         */
        private PdfPTable buildHeaderTable(int currentPageNumber) throws DocumentException {
            PdfPTable table = new PdfPTable(3);
            table.setWidths(new float[]{18f, 58f, 24f});

            Font fontInv = new Font(Font.HELVETICA, 10f, Font.BOLD, Color.BLACK);
            Font fontBanner = new Font(Font.HELVETICA, 8.2f, Font.BOLD, Color.WHITE);
            Font fontMetaLabel = new Font(Font.HELVETICA, 6.8f, Font.BOLD, Color.BLACK);
            Font fontMetaVal = new Font(Font.HELVETICA, 6.8f, Font.BOLD, Color.BLACK);

            // Columna 1: Logotipo UFPS
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorderColor(BORDER_COLOR);
            logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            logoCell.setPadding(3f);

            if (logoBytes != null) {
                try {
                    Image logo = Image.getInstance(logoBytes);
                    logo.scaleToFit(85f, 40f);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    logoCell.addElement(logo);
                } catch (Exception e) {
                    logoCell.addElement(new Phrase("UFPS", fontInv));
                }
            } else {
                logoCell.addElement(new Phrase("UFPS", fontInv));
            }
            table.addCell(logoCell);

            // Columna 2: Subtabla con INVESTIGACIÓN arriba y Banner Rojo abajo
            PdfPTable subCol2 = new PdfPTable(1);
            subCol2.setWidthPercentage(100f);

            PdfPCell cInvestigacion = new PdfPCell(new Phrase("INVESTIGACIÓN", fontInv));
            cInvestigacion.setHorizontalAlignment(Element.ALIGN_CENTER);
            cInvestigacion.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cInvestigacion.setBorder(Rectangle.NO_BORDER);
            cInvestigacion.setPadding(4f);
            subCol2.addCell(cInvestigacion);

            PdfPCell cBanner = new PdfPCell(new Phrase("INFORME DE GESTIÓN DE SEMILLEROS DE INVESTIGACIÓN", fontBanner));
            cBanner.setBackgroundColor(RED_UFPS);
            cBanner.setHorizontalAlignment(Element.ALIGN_CENTER);
            cBanner.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cBanner.setBorder(Rectangle.TOP);
            cBanner.setBorderColor(BORDER_COLOR);
            cBanner.setPadding(4f);
            subCol2.addCell(cBanner);

            PdfPCell wrapperCol2 = new PdfPCell(subCol2);
            wrapperCol2.setPadding(0f);
            wrapperCol2.setBorderColor(BORDER_COLOR);
            table.addCell(wrapperCol2);

            // Columna 3: Metadatos institucionales (CÓDIGO, VERSIÓN, FECHA, PÁGINA)
            PdfPTable subCol3 = new PdfPTable(2);
            subCol3.setWidthPercentage(100f);
            subCol3.setWidths(new float[]{46f, 54f});

            addMetaRow(subCol3, "CÓDIGO", "FO-IN-14", fontMetaLabel, fontMetaVal);
            addMetaRow(subCol3, "VERSIÓN", "02", fontMetaLabel, fontMetaVal);
            addMetaRow(subCol3, "FECHA", "24/10/2025", fontMetaLabel, fontMetaVal);

            // Celda Página X de Y
            PdfPCell cPagLabel = new PdfPCell(new Phrase("PÁGINA", fontMetaLabel));
            cPagLabel.setPadding(2f);
            cPagLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
            cPagLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cPagLabel.setBorderColor(BORDER_COLOR);
            subCol3.addCell(cPagLabel);

            PdfPCell cPagVal = new PdfPCell();
            cPagVal.setPadding(2f);
            cPagVal.setHorizontalAlignment(Element.ALIGN_CENTER);
            cPagVal.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cPagVal.setBorderColor(BORDER_COLOR);

            Paragraph pPag = new Paragraph();
            pPag.setAlignment(Element.ALIGN_CENTER);
            pPag.add(new Phrase(currentPageNumber + " de ", fontMetaVal));
            Image totalImg = Image.getInstance(totalPagesTemplate);
            pPag.add(new Chunk(totalImg, 0f, -1f));
            cPagVal.addElement(pPag);
            subCol3.addCell(cPagVal);

            PdfPCell wrapperCol3 = new PdfPCell(subCol3);
            wrapperCol3.setPadding(0f);
            wrapperCol3.setBorderColor(BORDER_COLOR);
            table.addCell(wrapperCol3);

            // Fila 2 completa: ELABORÓ | REVISÓ | APROBÓ
            table.addCell(buildElabCell("ELABORÓ", "Líder Investigación"));
            table.addCell(buildElabCell("REVISÓ", "Equipo Operativo de Calidad"));
            table.addCell(buildElabCell("APROBÓ", "Líder de Calidad"));

            return table;
        }

        private void addMetaRow(PdfPTable table, String label, String val, Font fontLabel, Font fontVal) {
            PdfPCell c1 = new PdfPCell(new Phrase(label, fontLabel));
            c1.setPadding(2.5f);
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            c1.setBorderColor(BORDER_COLOR);
            table.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase(val, fontVal));
            c2.setPadding(2.5f);
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            c2.setBorderColor(BORDER_COLOR);
            table.addCell(c2);
        }

        private PdfPCell buildElabCell(String top, String bottom) {
            PdfPTable sub = new PdfPTable(1);
            sub.setWidthPercentage(100f);

            Font fTop = new Font(Font.HELVETICA, 7.2f, Font.BOLD, Color.BLACK);
            Font fBottom = new Font(Font.HELVETICA, 7.2f, Font.NORMAL, Color.BLACK);

            PdfPCell cTop = new PdfPCell(new Phrase(top, fTop));
            cTop.setHorizontalAlignment(Element.ALIGN_CENTER);
            cTop.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cTop.setPadding(2f);
            cTop.setBorder(Rectangle.NO_BORDER);
            sub.addCell(cTop);

            PdfPCell cBot = new PdfPCell(new Phrase(bottom, fBottom));
            cBot.setHorizontalAlignment(Element.ALIGN_CENTER);
            cBot.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cBot.setPadding(2f);
            cBot.setBorder(Rectangle.NO_BORDER);
            sub.addCell(cBot);

            PdfPCell wrapper = new PdfPCell(sub);
            wrapper.setPadding(0f);
            wrapper.setBorderColor(BORDER_COLOR);
            return wrapper;
        }
    }
}
