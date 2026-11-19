-- V10: Reemplazar columna edad por fecha_nacimiento calculando fecha aproximada

ALTER TABLE integrantes
    ADD COLUMN fecha_nacimiento DATE NOT NULL DEFAULT '2002-05-15';

-- Aproximar la fecha de nacimiento a partir de la edad registrada previamente
UPDATE integrantes
SET fecha_nacimiento = (CURRENT_DATE - (edad * 365 + 120) * INTERVAL '1 day')::date
WHERE edad IS NOT NULL;

-- Eliminar columna edad
ALTER TABLE integrantes DROP COLUMN edad;

-- Quitar default para que las futuras inserciones sean explícitas
ALTER TABLE integrantes ALTER COLUMN fecha_nacimiento DROP DEFAULT;
