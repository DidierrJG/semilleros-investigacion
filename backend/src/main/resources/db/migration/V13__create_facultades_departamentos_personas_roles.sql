-- =========================================================================
-- V12: Creación de estructura organizacional (Facultades, Departamentos),
--      tabla de Personas con soporte multi-rol institucional y relación
--      de Programas Académicos con Facultad.
-- =========================================================================

-- 1. Tabla de Facultades
CREATE TABLE facultades (
    id               BIGSERIAL    PRIMARY KEY,
    nombre           VARCHAR(180) NOT NULL UNIQUE,
    sigla            VARCHAR(20)  NOT NULL UNIQUE,
    representante_id BIGINT
);

-- 2. Tabla de Departamentos (cada departamento pertenece a una facultad)
CREATE TABLE departamentos (
    id           BIGSERIAL    PRIMARY KEY,
    nombre       VARCHAR(180) NOT NULL,
    facultad_id  BIGINT       NOT NULL REFERENCES facultades(id),
    director_id  BIGINT,
    CONSTRAINT uk_departamento_facultad UNIQUE (nombre, facultad_id)
);

-- 3. Tabla de Personas (investigadores, directores, representantes)
CREATE TABLE personas (
    id              BIGSERIAL    PRIMARY KEY,
    identificacion  VARCHAR(20)  NOT NULL UNIQUE,
    tipo_documento  VARCHAR(20)  NOT NULL DEFAULT 'CC',
    nombres         VARCHAR(100) NOT NULL,
    apellidos       VARCHAR(100) NOT NULL,
    correo          VARCHAR(180) NOT NULL UNIQUE,
    telefono        VARCHAR(20),
    cvlac_url       VARCHAR(300),
    departamento_id BIGINT       REFERENCES departamentos(id),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE
);

-- 4. Llaves foráneas circulares hacia personas
ALTER TABLE facultades
    ADD CONSTRAINT fk_facultad_representante FOREIGN KEY (representante_id) REFERENCES personas(id);

ALTER TABLE departamentos
    ADD CONSTRAINT fk_departamento_director FOREIGN KEY (director_id) REFERENCES personas(id);

-- 5. Tabla de Roles Institucionales y de Investigación
CREATE TABLE roles (
    id          BIGSERIAL    PRIMARY KEY,
    codigo      VARCHAR(50)  NOT NULL UNIQUE,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);

-- 6. Tabla intermedia Persona - Roles (Muchos a Muchos)
CREATE TABLE persona_roles (
    persona_id       BIGINT    NOT NULL REFERENCES personas(id) ON DELETE CASCADE,
    rol_id           BIGINT    NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (persona_id, rol_id)
);

-- 7. Agregar relación de facultad a programas académicos
ALTER TABLE programas
    ADD COLUMN facultad_id BIGINT REFERENCES facultades(id);

-- =========================================================================
-- Siembra de datos iniciales institucionales UFPS
-- =========================================================================

-- Roles
INSERT INTO roles (codigo, nombre, descripcion) VALUES
    ('REPRESENTANTE_FACULTAD', 'Representante de Facultad', 'Representante de los investigadores de la facultad ante el comité central'),
    ('DIRECTOR_DEPARTAMENTO', 'Director de Departamento', 'Director de departamento académico responsable de avalar semilleros'),
    ('DIRECTOR_SEMILLERO', 'Director de Semillero', 'Docente encargado de la dirección técnica y formativa del semillero'),
    ('LIDER_GRUPO', 'Líder de Grupo', 'Líder de grupo de investigación categorizado por Minciencias'),
    ('DOCENTE_INVESTIGADOR', 'Docente Investigador', 'Profesor vinculado a actividades formativas de investigación'),
    ('ADMINISTRADOR', 'Administrador', 'Administrador institucional del sistema')
ON CONFLICT (codigo) DO NOTHING;

-- Facultades
INSERT INTO facultades (nombre, sigla) VALUES
    ('Facultad de Ingeniería', 'FING'),
    ('Facultad de Ciencias Empresariales', 'FACE'),
    ('Facultad de Ciencias Agrarias y del Ambiente', 'FCA'),
    ('Facultad de Ciencias de la Salud', 'FCS'),
    ('Facultad de Educación, Artes y Humanidades', 'FEAH'),
    ('Facultad de Ciencias Básicas', 'FCB')
ON CONFLICT (sigla) DO NOTHING;

-- Departamentos
INSERT INTO departamentos (nombre, facultad_id)
SELECT 'Sistemas e Informática', id FROM facultades WHERE sigla = 'FING'
ON CONFLICT (nombre, facultad_id) DO NOTHING;

INSERT INTO departamentos (nombre, facultad_id)
SELECT 'Electricidad y Electrónica', id FROM facultades WHERE sigla = 'FING'
ON CONFLICT (nombre, facultad_id) DO NOTHING;

INSERT INTO departamentos (nombre, facultad_id)
SELECT 'Construcción y Vías', id FROM facultades WHERE sigla = 'FING'
ON CONFLICT (nombre, facultad_id) DO NOTHING;

INSERT INTO departamentos (nombre, facultad_id)
SELECT 'Mecánica y Mecatrónica', id FROM facultades WHERE sigla = 'FING'
ON CONFLICT (nombre, facultad_id) DO NOTHING;

INSERT INTO departamentos (nombre, facultad_id)
SELECT 'Procesos Industriales', id FROM facultades WHERE sigla = 'FING'
ON CONFLICT (nombre, facultad_id) DO NOTHING;

INSERT INTO departamentos (nombre, facultad_id)
SELECT 'Ciencias Administrativas', id FROM facultades WHERE sigla = 'FACE'
ON CONFLICT (nombre, facultad_id) DO NOTHING;

INSERT INTO departamentos (nombre, facultad_id)
SELECT 'Contabilidad y Finanzas', id FROM facultades WHERE sigla = 'FACE'
ON CONFLICT (nombre, facultad_id) DO NOTHING;

INSERT INTO departamentos (nombre, facultad_id)
SELECT 'Ciencias del Medio Ambiente', id FROM facultades WHERE sigla = 'FCA'
ON CONFLICT (nombre, facultad_id) DO NOTHING;

INSERT INTO departamentos (nombre, facultad_id)
SELECT 'Ciencias Agrarias', id FROM facultades WHERE sigla = 'FCA'
ON CONFLICT (nombre, facultad_id) DO NOTHING;

-- Personas iniciales (migradas / homologadas con los directores existentes)
INSERT INTO personas (identificacion, tipo_documento, nombres, apellidos, correo, cvlac_url, departamento_id, activo)
SELECT
    '10901001', 'CC', 'Ana María', 'Pérez', 'ana.perez@universidad.edu.co',
    'https://scienti.minciencias.gov.co/cvlac/visualizador/generarCurriculoCv.do?cod_rh=00010901001',
    d.id, true
FROM departamentos d
JOIN facultades f ON d.facultad_id = f.id
WHERE d.nombre = 'Sistemas e Informática' AND f.sigla = 'FING'
ON CONFLICT (identificacion) DO NOTHING;

INSERT INTO personas (identificacion, tipo_documento, nombres, apellidos, correo, cvlac_url, departamento_id, activo)
SELECT
    '10901002', 'CC', 'Carlos Eduardo', 'Martínez', 'carlos.martinez@universidad.edu.co',
    'https://scienti.minciencias.gov.co/cvlac/visualizador/generarCurriculoCv.do?cod_rh=00010901002',
    d.id, true
FROM departamentos d
JOIN facultades f ON d.facultad_id = f.id
WHERE d.nombre = 'Sistemas e Informática' AND f.sigla = 'FING'
ON CONFLICT (identificacion) DO NOTHING;

INSERT INTO personas (identificacion, tipo_documento, nombres, apellidos, correo, cvlac_url, departamento_id, activo)
SELECT
    '10901003', 'CC', 'Laura Sofía', 'Gómez', 'laura.gomez@universidad.edu.co',
    'https://scienti.minciencias.gov.co/cvlac/visualizador/generarCurriculoCv.do?cod_rh=00010901003',
    d.id, true
FROM departamentos d
JOIN facultades f ON d.facultad_id = f.id
WHERE d.nombre = 'Electricidad y Electrónica' AND f.sigla = 'FING'
ON CONFLICT (identificacion) DO NOTHING;

INSERT INTO personas (identificacion, tipo_documento, nombres, apellidos, correo, cvlac_url, departamento_id, activo)
SELECT
    '10901004', 'CC', 'Andrés Felipe', 'Rojas', 'andres.rojas@universidad.edu.co',
    'https://scienti.minciencias.gov.co/cvlac/visualizador/generarCurriculoCv.do?cod_rh=00010901004',
    d.id, true
FROM departamentos d
JOIN facultades f ON d.facultad_id = f.id
WHERE d.nombre = 'Electricidad y Electrónica' AND f.sigla = 'FING'
ON CONFLICT (identificacion) DO NOTHING;

INSERT INTO personas (identificacion, tipo_documento, nombres, apellidos, correo, cvlac_url, departamento_id, activo)
SELECT
    '10901005', 'CC', 'Diana Carolina', 'Torres', 'diana.torres@universidad.edu.co',
    'https://scienti.minciencias.gov.co/cvlac/visualizador/generarCurriculoCv.do?cod_rh=00010901005',
    d.id, true
FROM departamentos d
JOIN facultades f ON d.facultad_id = f.id
WHERE d.nombre = 'Ciencias Administrativas' AND f.sigla = 'FACE'
ON CONFLICT (identificacion) DO NOTHING;

-- Asignar roles a las personas iniciales
INSERT INTO persona_roles (persona_id, rol_id)
SELECT p.id, r.id
FROM personas p, roles r
WHERE r.codigo = 'DIRECTOR_SEMILLERO'
ON CONFLICT DO NOTHING;

INSERT INTO persona_roles (persona_id, rol_id)
SELECT p.id, r.id
FROM personas p, roles r
WHERE p.identificacion = '10901001' AND r.codigo = 'DIRECTOR_DEPARTAMENTO'
ON CONFLICT DO NOTHING;

INSERT INTO persona_roles (persona_id, rol_id)
SELECT p.id, r.id
FROM personas p, roles r
WHERE p.identificacion = '10901002' AND r.codigo = 'REPRESENTANTE_FACULTAD'
ON CONFLICT DO NOTHING;

-- Asignar representantes de facultad y directores de departamento
UPDATE facultades
SET representante_id = (SELECT id FROM personas WHERE identificacion = '10901002')
WHERE sigla = 'FING';

UPDATE departamentos
SET director_id = (SELECT id FROM personas WHERE identificacion = '10901001')
WHERE nombre = 'Sistemas e Informática';

UPDATE departamentos
SET director_id = (SELECT id FROM personas WHERE identificacion = '10901003')
WHERE nombre = 'Electricidad y Electrónica';

UPDATE departamentos
SET director_id = (SELECT id FROM personas WHERE identificacion = '10901005')
WHERE nombre = 'Ciencias Administrativas';

-- Asignar facultades a los programas existentes
UPDATE programas
SET facultad_id = (SELECT id FROM facultades WHERE sigla = 'FING')
WHERE nombre ILIKE '%Ingeniería%' OR nombre ILIKE '%Tecnología%';

UPDATE programas
SET facultad_id = (SELECT id FROM facultades WHERE sigla = 'FACE')
WHERE facultad_id IS NULL AND (nombre ILIKE '%Administración%' OR nombre ILIKE '%Comercio%' OR nombre ILIKE '%Contaduría%');

UPDATE programas
SET facultad_id = (SELECT id FROM facultades WHERE sigla = 'FING')
WHERE facultad_id IS NULL;
