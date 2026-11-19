-- V6: Agregar campos de contacto e información personal a la tabla integrantes

ALTER TABLE integrantes
    ADD COLUMN identificacion  VARCHAR(20)  NOT NULL DEFAULT '',
    ADD COLUMN semestre_actual SMALLINT     NOT NULL DEFAULT 1,
    ADD COLUMN edad            SMALLINT     NOT NULL DEFAULT 18,
    ADD COLUMN direccion       VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN email           VARCHAR(160) NOT NULL DEFAULT '',
    ADD COLUMN celular         VARCHAR(20)  NOT NULL DEFAULT '';

-- Eliminar los defaults temporales (los datos futuros deben ser explícitos)
ALTER TABLE integrantes
    ALTER COLUMN identificacion  DROP DEFAULT,
    ALTER COLUMN semestre_actual DROP DEFAULT,
    ALTER COLUMN edad            DROP DEFAULT,
    ALTER COLUMN direccion       DROP DEFAULT,
    ALTER COLUMN email           DROP DEFAULT,
    ALTER COLUMN celular         DROP DEFAULT;
