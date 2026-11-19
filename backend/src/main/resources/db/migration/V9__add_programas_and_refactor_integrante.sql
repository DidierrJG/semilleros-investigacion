-- V7: Crear tabla programas y migrar columna programa de integrantes a FK

CREATE TABLE programas (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(160) NOT NULL UNIQUE
);

-- Poblar programas con los valores únicos ya existentes en integrantes
INSERT INTO programas (nombre)
SELECT DISTINCT TRIM(programa) FROM integrantes WHERE programa IS NOT NULL AND TRIM(programa) <> '';

-- Agregar la columna FK (nullable temporalmente para poder hacer el UPDATE)
ALTER TABLE integrantes ADD COLUMN programa_id BIGINT REFERENCES programas(id);

-- Asignar el programa_id correspondiente basado en el nombre
UPDATE integrantes i
SET programa_id = p.id
FROM programas p
WHERE TRIM(i.programa) = p.nombre;

-- Hacer la columna NOT NULL ahora que todos los registros tienen valor
ALTER TABLE integrantes ALTER COLUMN programa_id SET NOT NULL;

-- Eliminar la columna de texto libre
ALTER TABLE integrantes DROP COLUMN programa;
