CREATE TABLE capacitaciones (
    id BIGSERIAL PRIMARY KEY,
    nombre_capacitacion VARCHAR(200) NOT NULL,
    fecha DATE NOT NULL,
    objetivo_capacitacion TEXT NOT NULL,
    porcentaje_cumplimiento DOUBLE PRECISION NOT NULL,
    cantidad_participantes INT NOT NULL
);

