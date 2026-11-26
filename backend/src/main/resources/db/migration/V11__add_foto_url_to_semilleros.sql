-- =====================================================
-- V11: Agregar columna foto_url a la tabla semilleros
-- Permite almacenar la ruta/URL de la imagen almacenada en disco
-- =====================================================
ALTER TABLE semilleros
    ADD COLUMN foto_url VARCHAR(500);
