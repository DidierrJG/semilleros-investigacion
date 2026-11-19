CREATE TABLE grupos_investigacion (
    id                  BIGSERIAL    PRIMARY KEY,
    nombre              VARCHAR(200) NOT NULL,
    sigla               VARCHAR(30)  NOT NULL UNIQUE,
    codigo_minciencias  VARCHAR(30)  NOT NULL UNIQUE,
    categoria           VARCHAR(30)  NOT NULL,
    facultad            VARCHAR(150) NOT NULL,
    departamento        VARCHAR(150) NOT NULL,
    lider               VARCHAR(180) NOT NULL,
    correo              VARCHAR(180) NOT NULL UNIQUE,
    sitio_web           VARCHAR(255)
);

-- =====================================================
-- Grupos de investigación institucionales UFPS iniciales
-- =====================================================
INSERT INTO grupos_investigacion (nombre, sigla, codigo_minciencias, categoria, facultad, departamento, lider, correo, sitio_web) VALUES
    (
        'Grupo de Sistemas Inteligentes',
        'GSI',
        'COL0021481',
        'A',
        'Facultad de Ingeniería',
        'Sistemas e Informática',
        'Dra. Ana María Pérez',
        'gsi@ufps.edu.co',
        'https://scienti.minciencias.gov.co/gruplac/jsp/visualiza/visualizagr.jsp?nro=000000000021481'
    ),
    (
        'Grupo de Ingeniería de Software',
        'GIDISOFT',
        'COL0006204',
        'A1',
        'Facultad de Ingeniería',
        'Sistemas e Informática',
        'Dr. Carlos Eduardo Martínez',
        'gidisoft@ufps.edu.co',
        'https://scienti.minciencias.gov.co/gruplac/jsp/visualiza/visualizagr.jsp?nro=000000000006204'
    ),
    (
        'Grupo de Redes y Seguridad',
        'GIDET',
        'COL0010609',
        'A',
        'Facultad de Ingeniería',
        'Electricidad y Electrónica',
        'Mg. Laura Sofía Gómez',
        'gidet@ufps.edu.co',
        'https://scienti.minciencias.gov.co/gruplac/jsp/visualiza/visualizagr.jsp?nro=000000000010609'
    ),
    (
        'Grupo de Automatización e IoT',
        'GAIOT',
        'COL0052341',
        'B',
        'Facultad de Ingeniería',
        'Electricidad y Electrónica',
        'Dr. Andrés Felipe Rojas',
        'gaiot@ufps.edu.co',
        'https://scienti.minciencias.gov.co/gruplac/jsp/visualiza/visualizagr.jsp?nro=000000000052341'
    ),
    (
        'Grupo de Computación Visual',
        'GCV',
        'COL0083921',
        'B',
        'Facultad de Ingeniería',
        'Sistemas e Informática',
        'Dr. Andrés Felipe Rojas',
        'gcv@ufps.edu.co',
        'https://scienti.minciencias.gov.co/gruplac/jsp/visualiza/visualizagr.jsp?nro=000000000083921'
    ),
    (
        'Grupo de Gestión Tecnológica',
        'GEDES',
        'COL0169989',
        'A',
        'Facultad de Ciencias Empresariales',
        'Ciencias Administrativas',
        'Mg. Diana Carolina Torres',
        'gedes@ufps.edu.co',
        'https://scienti.minciencias.gov.co/gruplac/jsp/visualiza/visualizagr.jsp?nro=000000000169989'
    ),
    (
        'Grupo de Investigación Ambiente y Vida',
        'GIAV',
        'COL0000425',
        'A1',
        'Facultad de Ciencias Agrarias y del Ambiente',
        'Ciencias del Medio Ambiente',
        'Dra. Alina Katil Sigarroa Rieche',
        'giav@ufps.edu.co',
        'https://scienti.minciencias.gov.co/gruplac/jsp/visualiza/visualizagr.jsp?nro=000000000000425'
    )
ON CONFLICT (sigla) DO NOTHING;

-- =====================================================
-- Migración de la relación en semilleros
-- =====================================================
ALTER TABLE semilleros ADD COLUMN grupo_investigacion_id BIGINT REFERENCES grupos_investigacion(id);

UPDATE semilleros s
SET grupo_investigacion_id = g.id
FROM grupos_investigacion g
WHERE s.grupo_investigacion = g.nombre;

UPDATE semilleros
SET grupo_investigacion_id = (SELECT id FROM grupos_investigacion ORDER BY id LIMIT 1)
WHERE grupo_investigacion_id IS NULL;

ALTER TABLE semilleros ALTER COLUMN grupo_investigacion_id SET NOT NULL;

ALTER TABLE semilleros DROP COLUMN grupo_investigacion;
