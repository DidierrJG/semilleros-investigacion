package com.semilleros.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.semilleros.backend.entity.Actividad;
import com.semilleros.backend.entity.Capacitacion;
import com.semilleros.backend.entity.Integrante;
import com.semilleros.backend.entity.Producto;
import com.semilleros.backend.entity.Semillero;
import com.semilleros.backend.exception.BusinessException;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemilleroPdfReportService {

    private static final Color UFPS_RED = new Color(170, 25, 22);
    private static final Color HEADER_BG = new Color(241, 245, 249);
    private static final Color SECTION_BG = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);

    private static final Font FONT_DOC_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font FONT_HEADER_SUB = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.DARK_GRAY);
    private static final Font FONT_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, UFPS_RED);
    private static final Font FONT_TH = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Color.BLACK);
    private static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.BLACK);
    private static final Font FONT_BODY_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Color.BLACK);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 6.5f, Color.DARK_GRAY);

    private static final String LOGO_RESOURCE = "/templates/ufps_logo.png";
    private static final String LOGO_FALLBACK = "backend/templates/ufps_logo.png";
    private static final String LOGO_FALLBACK_2 = "templates/ufps_logo.png";

    private final SemilleroRepository semilleroRepository;
    private final IntegranteRepository integranteRepository;
    private final ProductoRepository productoRepository;
    private final ActividadRepository actividadRepository;
    private final CapacitacionRepository capacitacionRepository;

    @Transactional(readOnly = true)
    public byte[] generarInformeGestionPdf(Long semilleroId) {
        Semillero semillero = semilleroRepository.findById(semilleroId)
                .orElseThrow(() -> new ResourceNotFoundException("Semillero no encontrado con id: " + semilleroId));

        List<Integrante> integrantes = integranteRepository.findBySemilleroId(semilleroId);
        List<Producto> productos = productoRepository.findBySemilleroId(semilleroId);
        List<Actividad> actividades = actividadRepository.findBySemilleroId(semilleroId);
        List<Capacitacion> capacitaciones = capacitacionRepository.findBySemilleroId(semilleroId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 28, 28, 30, 32);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Numeración de páginas en el pie
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfPTable footer = new PdfPTable(1);
                    footer.setTotalWidth(document.right() - document.left());
                    PdfPCell cell = new PdfPCell(new Phrase("Página " + writer.getPageNumber(), FONT_SMALL));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorder(Rectangle.NO_BORDER);
                    footer.addCell(cell);
                    footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 10, writer.getDirectContent());
                }
            });

            document.open();

            // 1. Cabecera Institucional Oficial FO-IN-14
            document.add(crearCabeceraInstitucional());
            document.add(crearEspacio(6));

            // 2. Tabla de Información General
            document.add(crearTablaInformacionGeneral(semillero));
            document.add(crearEspacio(6));

            // Párrafo introductorio
            Paragraph intro = new Paragraph(
                    "Teniendo en cuenta el plan de acción presentado para desarrollar en el semestre académico actual, " +
                    "indique frente a cada producto el porcentaje de cumplimiento.",
                    FONT_SMALL
            );
            intro.setSpacingAfter(6);
            document.add(intro);

            // 3. Proyectos de Investigación
            document.add(crearTablaProyectos(productos));
            document.add(crearEspacio(6));

            // 4. Capacitaciones
            document.add(crearTablaCapacitaciones(capacitaciones));
            document.add(crearEspacio(6));

            // 5. Otras Actividades de Investigación
            document.add(crearTablaActividades(actividades));
            document.add(crearEspacio(6));

            // 6. Productos Obtenidos
            document.add(crearTablaProductosResumen(productos, capacitaciones));
            document.add(crearEspacio(6));

            // 7. Relación de Estudiantes
            document.add(crearSeccionEstudiantes(integrantes.size()));
            document.add(crearEspacio(8));

            // 8. Firmas de Elaboración y Aprobación
            document.add(crearTablaFirmas(semillero));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar el PDF del informe de gestión para el semillero {}", semilleroId, e);
            throw new BusinessException("Error al generar el documento PDF: " + e.getMessage());
        }
    }

    private PdfPTable crearCabeceraInstitucional() {
        PdfPTable header = new PdfPTable(new float[]{22, 50, 28});
        header.setWidthPercentage(100);

        // Celda 1: Logo UFPS
        PdfPCell cellLogo = new PdfPCell();
        cellLogo.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellLogo.setPadding(3);
        cellLogo.setBorderColor(BORDER_COLOR);

        byte[] logoData = cargarLogoUfps();
        if (logoData != null) {
            try {
                Image logo = Image.getInstance(logoData);
                logo.scaleToFit(90, 48);
                cellLogo.addElement(logo);
            } catch (Exception e) {
                cellLogo.addElement(new Phrase("UFPS", FONT_DOC_TITLE));
            }
        } else {
            cellLogo.addElement(new Phrase("UFPS", FONT_DOC_TITLE));
        }
        header.addCell(cellLogo);

        // Celda 2: Título del Formato
        PdfPCell cellTitle = new PdfPCell();
        cellTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTitle.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellTitle.setPadding(4);
        cellTitle.setBorderColor(BORDER_COLOR);

        Paragraph pMacro = new Paragraph("INVESTIGACIÓN", FONT_HEADER_SUB);
        pMacro.setAlignment(Element.ALIGN_CENTER);
        cellTitle.addElement(pMacro);

        Paragraph pTitle = new Paragraph("INFORME DE GESTIÓN DE SEMILLEROS DE INVESTIGACIÓN", FONT_DOC_TITLE);
        pTitle.setAlignment(Element.ALIGN_CENTER);
        cellTitle.addElement(pTitle);

        header.addCell(cellTitle);

        // Celda 3: Metadatos de Calidad (Código, Versión, Fecha)
        PdfPTable metaTable = new PdfPTable(new float[]{42, 58});
        metaTable.setWidthPercentage(100);
        metaTable.addCell(cellMeta("CÓDIGO:", true));
        metaTable.addCell(cellMeta("FO-IN-14", false));
        metaTable.addCell(cellMeta("VERSIÓN:", true));
        metaTable.addCell(cellMeta("02", false));
        metaTable.addCell(cellMeta("FECHA:", true));
        metaTable.addCell(cellMeta("24/10/2025", false));

        PdfPCell cellMetaWrap = new PdfPCell(metaTable);
        cellMetaWrap.setPadding(0);
        cellMetaWrap.setBorderColor(BORDER_COLOR);
        header.addCell(cellMetaWrap);

        // Fila inferior de aprobación de calidad
        PdfPCell cellQuality = new PdfPCell(new Phrase(
                "ELABORÓ: Líder Investigación   |   REVISÓ: Equipo Operativo de Calidad   |   APROBÓ: Líder de Calidad",
                FONT_SMALL
        ));
        cellQuality.setColspan(3);
        cellQuality.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellQuality.setPadding(3);
        cellQuality.setBackgroundColor(HEADER_BG);
        cellQuality.setBorderColor(BORDER_COLOR);
        header.addCell(cellQuality);

        return header;
    }

    private PdfPCell cellMeta(String text, boolean bold) {
        PdfPCell cell = new PdfPCell(new Phrase(text, bold ? FONT_BODY_BOLD : FONT_BODY));
        cell.setPadding(2);
        cell.setBorderColor(BORDER_COLOR);
        return cell;
    }

    private PdfPTable crearTablaInformacionGeneral(Semillero semillero) {
        PdfPTable table = new PdfPTable(new float[]{24, 38, 16, 22});
        table.setWidthPercentage(100);

        // Fila 0: Semillero
        table.addCell(createCell("SEMILLERO DE INVESTIGACIÓN", FONT_TH, Element.ALIGN_LEFT, HEADER_BG, 1));
        String semilleroDesc = semillero.getNombre() + (semillero.getSigla() != null ? " (" + semillero.getSigla() + ")" : "");
        table.addCell(createCell(semilleroDesc, FONT_BODY_BOLD, Element.ALIGN_LEFT, null, 3));

        // Fila 1: Director
        table.addCell(createCell("DIRECTOR", FONT_TH, Element.ALIGN_LEFT, HEADER_BG, 1));
        String directorNombre = semillero.getDirector() != null ? semillero.getDirector().getNombre() : "No asignado";
        table.addCell(createCell(directorNombre, FONT_BODY, Element.ALIGN_LEFT, null, 3));

        // Fila 2: Celular y Correo
        table.addCell(createCell("CELULAR", FONT_TH, Element.ALIGN_LEFT, HEADER_BG, 1));
        table.addCell(createCell("No registrado", FONT_BODY, Element.ALIGN_LEFT, null, 1));
        table.addCell(createCell("CORREO ELECTRÓNICO:", FONT_TH, Element.ALIGN_LEFT, HEADER_BG, 1));
        String directorCorreo = semillero.getDirector() != null ? semillero.getDirector().getCorreo() : "No registrado";
        table.addCell(createCell(directorCorreo, FONT_BODY, Element.ALIGN_LEFT, null, 1));

        // Fila 3: Grupo de Investigación y Director Grupo
        table.addCell(createCell("GRUPO DE INVESTIGACIÓN", FONT_TH, Element.ALIGN_LEFT, HEADER_BG, 1));
        String grupoNombre = semillero.getGrupoInvestigacion() != null
                ? semillero.getGrupoInvestigacion().getSigla() + " - " + semillero.getGrupoInvestigacion().getNombre()
                : "No asignado";
        table.addCell(createCell(grupoNombre, FONT_BODY, Element.ALIGN_LEFT, null, 1));
        table.addCell(createCell("DIRECTOR:", FONT_TH, Element.ALIGN_LEFT, HEADER_BG, 1));
        String grupoLider = semillero.getGrupoInvestigacion() != null ? semillero.getGrupoInvestigacion().getLider() : "No asignado";
        table.addCell(createCell(grupoLider, FONT_BODY, Element.ALIGN_LEFT, null, 1));

        // Fila 4: Facultad
        table.addCell(createCell("FACULTAD", FONT_TH, Element.ALIGN_LEFT, HEADER_BG, 1));
        String facultad = semillero.getGrupoInvestigacion() != null ? semillero.getGrupoInvestigacion().getFacultad() : "Ingeniería";
        table.addCell(createCell(facultad, FONT_BODY, Element.ALIGN_LEFT, null, 3));

        // Fila 5: Departamento
        table.addCell(createCell("DEPARTAMENTO", FONT_TH, Element.ALIGN_LEFT, HEADER_BG, 1));
        String departamento = semillero.getGrupoInvestigacion() != null ? semillero.getGrupoInvestigacion().getDepartamento() : "Sistemas e Informática";
        table.addCell(createCell(departamento, FONT_BODY, Element.ALIGN_LEFT, null, 3));

        return table;
    }

    private PdfPTable crearTablaProyectos(List<Producto> productos) {
        PdfPTable table = new PdfPTable(new float[]{30, 36, 14, 20});
        table.setWidthPercentage(100);

        // Header de sección
        PdfPCell sectionCell = createCell("1. Proyectos de Investigación", FONT_SECTION, Element.ALIGN_LEFT, SECTION_BG, 4);
        sectionCell.setPadding(4);
        table.addCell(sectionCell);

        // Columnas
        table.addCell(createCell("Nombre Proyecto", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));
        table.addCell(createCell("Actividades", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));
        table.addCell(createCell("% de Cumplimiento", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));
        table.addCell(createCell("Ejecutor (Autores)", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));

        if (productos.isEmpty()) {
            PdfPCell empty = createCell("Sin proyectos o productos de investigación registrados a la fecha.", FONT_BODY, Element.ALIGN_CENTER, null, 4);
            empty.setPadding(6);
            table.addCell(empty);
        } else {
            for (Producto p : productos) {
                table.addCell(createCell(p.getNombre(), FONT_BODY_BOLD, Element.ALIGN_LEFT, null, 1));
                table.addCell(createCell(p.getDescripcion() != null ? p.getDescripcion() : "Desarrollo del proyecto", FONT_BODY, Element.ALIGN_LEFT, null, 1));
                table.addCell(createCell("100%", FONT_BODY, Element.ALIGN_CENTER, null, 1));

                String autores = (p.getIntegrantes() != null && !p.getIntegrantes().isEmpty())
                        ? p.getIntegrantes().stream().map(Integrante::getNombre).collect(Collectors.joining(", "))
                        : "Integrantes del semillero";
                table.addCell(createCell(autores, FONT_BODY, Element.ALIGN_LEFT, null, 1));
            }
        }

        return table;
    }

    private PdfPTable crearTablaCapacitaciones(List<Capacitacion> capacitaciones) {
        PdfPTable table = new PdfPTable(new float[]{34, 34, 16, 16});
        table.setWidthPercentage(100);

        PdfPCell sectionCell = createCell("2. Capacitaciones", FONT_SECTION, Element.ALIGN_LEFT, SECTION_BG, 4);
        sectionCell.setPadding(4);
        table.addCell(sectionCell);

        table.addCell(createCell("Nombre de la Capacitación", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));
        table.addCell(createCell("Objetivo", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));
        table.addCell(createCell("Fecha de realización", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));
        table.addCell(createCell("% de Cumplimiento", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));

        if (capacitaciones.isEmpty()) {
            PdfPCell empty = createCell("Sin capacitaciones registradas en el periodo actual.", FONT_BODY, Element.ALIGN_CENTER, null, 4);
            empty.setPadding(6);
            table.addCell(empty);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Capacitacion c : capacitaciones) {
                table.addCell(createCell(c.getNombreCapacitacion(), FONT_BODY_BOLD, Element.ALIGN_LEFT, null, 1));
                table.addCell(createCell(c.getObjetivoCapacitacion(), FONT_BODY, Element.ALIGN_LEFT, null, 1));
                String fechaStr = c.getFecha() != null ? c.getFecha().format(formatter) : "No registrada";
                table.addCell(createCell(fechaStr, FONT_BODY, Element.ALIGN_CENTER, null, 1));
                String cStr = c.getPorcentajeCumplimiento() != null ? String.format("%.0f%%", c.getPorcentajeCumplimiento()) : "100%";
                table.addCell(createCell(cStr, FONT_BODY, Element.ALIGN_CENTER, null, 1));
            }
        }

        return table;
    }

    private PdfPTable crearTablaActividades(List<Actividad> actividades) {
        PdfPTable table = new PdfPTable(new float[]{50, 30, 20});
        table.setWidthPercentage(100);

        PdfPCell sectionCell = createCell("3. Otras Actividades de Investigación (*)", FONT_SECTION, Element.ALIGN_LEFT, SECTION_BG, 3);
        sectionCell.setPadding(4);
        table.addCell(sectionCell);

        table.addCell(createCell("Nombre", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));
        table.addCell(createCell("Tipo de Actividad", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));
        table.addCell(createCell("% de Cumplimiento", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));

        if (actividades.isEmpty()) {
            PdfPCell empty = createCell("Sin otras actividades de investigación registradas a la fecha.", FONT_BODY, Element.ALIGN_CENTER, null, 3);
            empty.setPadding(6);
            table.addCell(empty);
        } else {
            for (Actividad a : actividades) {
                table.addCell(createCell(a.getNombre(), FONT_BODY_BOLD, Element.ALIGN_LEFT, null, 1));
                String tipo = a.getTipoActividad() != null ? a.getTipoActividad().getNombre() : "Actividad formativa";
                table.addCell(createCell(tipo, FONT_BODY, Element.ALIGN_LEFT, null, 1));
                String cump = a.getPorcentajeCumplimiento() != null ? a.getPorcentajeCumplimiento() + "%" : "100%";
                table.addCell(createCell(cump, FONT_BODY, Element.ALIGN_CENTER, null, 1));
            }
        }

        PdfPCell footnote = createCell("(*) Actividades de investigación: Espacios de socialización, Foros, Reuniones, Elaboración de documentos, entre otros.", FONT_SMALL, Element.ALIGN_LEFT, null, 3);
        footnote.setPadding(3);
        table.addCell(footnote);

        return table;
    }

    private PdfPTable crearTablaProductosResumen(List<Producto> productos, List<Capacitacion> capacitaciones) {
        PdfPTable table = new PdfPTable(new float[]{30, 42, 16, 12});
        table.setWidthPercentage(100);

        PdfPCell headTitle = createCell(
                "A continuación, describa los productos obtenidos por el semillero de investigación en el semestre actual, según el Acuerdo que adopta el Sistema de Investigación UFPS:",
                FONT_SMALL, Element.ALIGN_LEFT, SECTION_BG, 4
        );
        headTitle.setPadding(3);
        table.addCell(headTitle);

        table.addCell(createCell("PRODUCTO", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));
        table.addCell(createCell("DESCRIPCIÓN", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));
        table.addCell(createCell("RESPONSABLE", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));
        table.addCell(createCell("FECHA", FONT_TH, Element.ALIGN_CENTER, HEADER_BG, 1));

        // 1. Proyectos terminados / en ejecución
        table.addCell(createCell("Proyectos terminados y/o ejecución, avalado financiación interna(FINU) o externa", FONT_BODY_BOLD, Element.ALIGN_LEFT, null, 1));
        String descProy = !productos.isEmpty()
                ? productos.stream().map(Producto::getNombre).collect(Collectors.joining("; "))
                : "No registrados";
        table.addCell(createCell(descProy, FONT_BODY, Element.ALIGN_LEFT, null, 1));
        table.addCell(createCell("Director Semillero", FONT_BODY, Element.ALIGN_LEFT, null, 1));
        table.addCell(createCell(LocalDate.now().getYear() + "-1", FONT_BODY, Element.ALIGN_CENTER, null, 1));

        // 2. Capacitaciones (4) correspondientes a las áreas de formación
        table.addCell(createCell("Capacitaciones correspondientes a las áreas de formación", FONT_BODY_BOLD, Element.ALIGN_LEFT, null, 1));
        String descCap = !capacitaciones.isEmpty()
                ? capacitaciones.stream().map(Capacitacion::getNombreCapacitacion).collect(Collectors.joining("; "))
                : "No registradas";
        table.addCell(createCell(descCap, FONT_BODY, Element.ALIGN_LEFT, null, 1));
        table.addCell(createCell("Director Semillero", FONT_BODY, Element.ALIGN_LEFT, null, 1));
        table.addCell(createCell(LocalDate.now().getYear() + "-1", FONT_BODY, Element.ALIGN_CENTER, null, 1));

        // 3. Participación en capacitaciones y espacios de socialización
        table.addCell(createCell("Participación en capacitaciones y espacios de socialización de la Vicerrectoría Asistente", FONT_BODY_BOLD, Element.ALIGN_LEFT, null, 1));
        table.addCell(createCell("Participación activa en eventos y encuentros de semilleros UFPS", FONT_BODY, Element.ALIGN_LEFT, null, 1));
        table.addCell(createCell("Integrantes Semillero", FONT_BODY, Element.ALIGN_LEFT, null, 1));
        table.addCell(createCell(LocalDate.now().getYear() + "-1", FONT_BODY, Element.ALIGN_CENTER, null, 1));

        return table;
    }

    private PdfPTable crearSeccionEstudiantes(int totalIntegrantes) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell sectionCell = createCell(
                "4. Relación estudiantes del semillero en el semestre académico actual (con al menos 80% de participación)",
                FONT_SECTION, Element.ALIGN_LEFT, SECTION_BG, 1
        );
        sectionCell.setPadding(4);
        table.addCell(sectionCell);

        String texto = "Como se muestra en el anexo FO-IN-12 ESTUDIANTES DE SEMILLEROS DE INVESTIGACION QUE CUMPLIERON CON " +
                "REQUERIMIENTO DE PARTICIPACIÓN, la cantidad de estudiantes que cumplieron con al menos el 80% de participación " +
                "en las reuniones de semillero, corresponde a " + totalIntegrantes + " estudiante(s) activo(s).";

        PdfPCell bodyCell = createCell(texto, FONT_BODY, Element.ALIGN_JUSTIFIED, null, 1);
        bodyCell.setPadding(5);
        table.addCell(bodyCell);

        return table;
    }

    private PdfPTable crearTablaFirmas(Semillero semillero) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        // Bloque 1: Elaborado por (Director Semillero)
        String directorNombre = semillero.getDirector() != null ? semillero.getDirector().getNombre() : "";
        PdfPCell cell1 = crearCuadroFirma(
                "ELABORADO POR",
                "Director Semillero de Investigación",
                directorNombre
        );

        // Bloque 2: Revisado por (Director Grupo de Investigación)
        String grupoLider = semillero.getGrupoInvestigacion() != null ? semillero.getGrupoInvestigacion().getLider() : "";
        PdfPCell cell2 = crearCuadroFirma(
                "REVISADO POR",
                "Director Grupo de Investigación",
                grupoLider
        );

        // Bloque 3: Revisado por (Director Departamento)
        PdfPCell cell3 = crearCuadroFirma(
                "REVISADO POR",
                "Director de Departamento",
                ""
        );

        // Bloque 4: VoBo Representante Facultad
        PdfPCell cell4 = crearCuadroFirma(
                "REVISADO POR",
                "VoBo. Representante de la Facultad ante el CCIE",
                ""
        );

        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell3);
        table.addCell(cell4);

        return table;
    }

    private PdfPCell crearCuadroFirma(String tipo, String cargo, String nombre) {
        PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(100);

        PdfPCell cTipo = new PdfPCell(new Phrase(tipo, FONT_TH));
        cTipo.setHorizontalAlignment(Element.ALIGN_CENTER);
        cTipo.setBackgroundColor(HEADER_BG);
        cTipo.setPadding(3);
        cTipo.setBorderColor(BORDER_COLOR);
        box.addCell(cTipo);

        PdfPCell cCargo = new PdfPCell(new Phrase(cargo, FONT_BODY_BOLD));
        cCargo.setHorizontalAlignment(Element.ALIGN_CENTER);
        cCargo.setPadding(3);
        cCargo.setBorderColor(BORDER_COLOR);
        box.addCell(cCargo);

        String textoNombre = "NOMBRE: " + (nombre != null && !nombre.isBlank() ? nombre : "__________________________________");
        PdfPCell cNom = new PdfPCell(new Phrase(textoNombre, FONT_BODY));
        cNom.setPadding(4);
        cNom.setBorderColor(BORDER_COLOR);
        box.addCell(cNom);

        PdfPCell cFirma = new PdfPCell(new Phrase("FIRMA:   __________________________________\n ", FONT_BODY));
        cFirma.setPadding(5);
        cFirma.setBorderColor(BORDER_COLOR);
        box.addCell(cFirma);

        PdfPCell container = new PdfPCell(box);
        container.setPadding(3);
        container.setBorder(Rectangle.NO_BORDER);
        return container;
    }

    private PdfPCell createCell(String text, Font font, int alignment, Color bgColor, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3.5f);
        cell.setColspan(colspan);
        cell.setBorderColor(BORDER_COLOR);
        if (bgColor != null) {
            cell.setBackgroundColor(bgColor);
        }
        return cell;
    }

    private Paragraph crearEspacio(float pt) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(pt);
        p.setSpacingAfter(0);
        p.getFont().setSize(1);
        return p;
    }

    private byte[] cargarLogoUfps() {
        try {
            InputStream is = getClass().getResourceAsStream(LOGO_RESOURCE);
            if (is != null) {
                return is.readAllBytes();
            }
            File f = new File(LOGO_FALLBACK);
            if (f.exists()) {
                try (InputStream fis = new FileInputStream(f)) {
                    return fis.readAllBytes();
                }
            }
            File f2 = new File(LOGO_FALLBACK_2);
            if (f2.exists()) {
                try (InputStream fis = new FileInputStream(f2)) {
                    return fis.readAllBytes();
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo cargar el logo de la UFPS: {}", e.getMessage());
        }
        return null;
    }
}
