CREATE TABLE capacitaciones (
    id                      BIGSERIAL        PRIMARY KEY,
    semillero_id            BIGINT           NOT NULL REFERENCES semilleros(id),
    nombre_capacitacion     VARCHAR(200)     NOT NULL,
    fecha                   DATE             NOT NULL,
    objetivo_capacitacion   TEXT             NOT NULL,
    porcentaje_cumplimiento DOUBLE PRECISION NOT NULL CHECK (porcentaje_cumplimiento >= 0 AND porcentaje_cumplimiento <= 100),
    cantidad_participantes  INT              NOT NULL CHECK (cantidad_participantes >= 0)
);

-- =====================================================
-- Capacitaciones de ejemplo para semilleros
-- =====================================================
INSERT INTO capacitaciones (semillero_id, nombre_capacitacion, fecha, objetivo_capacitacion, porcentaje_cumplimiento, cantidad_participantes) VALUES
    (
        (SELECT id FROM semilleros WHERE sigla = 'SIAA'),
        'Taller de Redes Neuronales y Deep Learning con PyTorch',
        '2024-03-20',
        'Capacitar a los semilleristas en el diseño, entrenamiento y optimización de redes neuronales convolucionales y transformers.',
        100.0,
        28
    ),
    (
        (SELECT id FROM semilleros WHERE sigla = 'SIAA'),
        'Pipelines de MLOps y Despliegue de Modelos de IA',
        '2024-05-15',
        'Instruir en buenas prácticas de integración continua, monitoreo de deriva y versionado de modelos con MLflow.',
        85.0,
        22
    ),
    (
        (SELECT id FROM semilleros WHERE sigla = 'SAM'),
        'Arquitectura de Microservicios con Spring Boot y Docker',
        '2024-04-12',
        'Enseñar diseño por capas, comunicación asíncrona, contenerización y pruebas automatizadas en arquitecturas modernas.',
        100.0,
        35
    ),
    (
        (SELECT id FROM semilleros WHERE sigla = 'SAM'),
        'Desarrollo Frontend Moderno con Web Components y JavaScript Vanilla',
        '2024-06-18',
        'Dominar manipulación del DOM, reactividad, enrutamiento del lado del cliente y consumo de APIs REST.',
        92.0,
        27
    ),
    (
        (SELECT id FROM semilleros WHERE sigla = 'SIHE'),
        'Fundamentos de Hacking Ético y Pentesting Web (OWASP Top 10)',
        '2024-04-28',
        'Identificar y mitigar vulnerabilidades comunes de seguridad web como inyecciones SQL, XSS y fallos de autenticación.',
        95.0,
        32
    ),
    (
        (SELECT id FROM semilleros WHERE sigla = 'SIHE'),
        'Análisis Forense Digital y Respuesta a Incidentes',
        '2024-07-22',
        'Adquirir habilidades para recopilación de evidencias, inspección de memoria y preservación de cadena de custodia.',
        70.0,
        19
    ),
    (
        (SELECT id FROM semilleros WHERE sigla = 'SRSE'),
        'Programación de Sistemas Embebidos y RTOS con ESP32',
        '2024-05-10',
        'Diseñar aplicaciones multitarea de tiempo real utilizando FreeRTOS, sensores y protocolos de comunicación IoT.',
        88.0,
        24
    ),
    (
        (SELECT id FROM semilleros WHERE sigla = 'SCDA'),
        'Análisis Exploratorio y Visualización Avanzada con Python y Seaborn',
        '2024-03-30',
        'Desarrollar capacidades de limpieza, perfilado y storytelling interactivo con conjuntos de datos complejos.',
        100.0,
        38
    );
