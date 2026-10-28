CREATE TABLE roles (
    id TEXT PRIMARY KEY,
    nombre TEXT NOT NULL
);

CREATE TABLE usuarios (
    id TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    rut TEXT NOT NULL,
    correo TEXT NOT NULL,
    rol TEXT NOT NULL REFERENCES roles(id) DEFERRABLE INITIALLY DEFERRED,
    clienteId TEXT REFERENCES clientes(id) DEFERRABLE INITIALLY DEFERRED,
    activo INTEGER NOT NULL CHECK(activo IN (0,1)),
    hash TEXT NOT NULL,
    sal TEXT NOT NULL
);

CREATE TABLE clientes (
    id TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    rut TEXT NOT NULL,
    giro TEXT NOT NULL,
    contacto TEXT NOT NULL,
    telefono TEXT NOT NULL,
    correo TEXT NOT NULL
);

CREATE TABLE instalaciones (
    id TEXT PRIMARY KEY,
    clienteId TEXT NOT NULL REFERENCES clientes(id) DEFERRABLE INITIALLY DEFERRED,
    nombre TEXT NOT NULL,
    ubicacion TEXT NOT NULL,
    comuna TEXT NOT NULL,
    region TEXT NOT NULL,
    acceso TEXT NOT NULL
);

CREATE TABLE equipos (
    id TEXT PRIMARY KEY,
    instalacionId TEXT NOT NULL REFERENCES instalaciones(id) DEFERRABLE INITIALLY DEFERRED,
    tag TEXT NOT NULL,
    tipo TEXT NOT NULL,
    marca TEXT NOT NULL,
    modelo TEXT NOT NULL,
    serie TEXT NOT NULL,
    anio INTEGER NOT NULL,
    placa TEXT NOT NULL,
    maxContactos REAL NOT NULL,
    observaciones TEXT NOT NULL
);

CREATE TABLE instrumentos (
    id TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    marca TEXT NOT NULL,
    modelo TEXT NOT NULL,
    serie TEXT NOT NULL
);

CREATE TABLE visitas (
    id TEXT PRIMARY KEY,
    instalacionId TEXT NOT NULL REFERENCES instalaciones(id) DEFERRABLE INITIALLY DEFERRED,
    ot TEXT NOT NULL,
    fecha TEXT NOT NULL,
    ingenieroId TEXT NOT NULL REFERENCES usuarios(id) DEFERRABLE INITIALLY DEFERRED,
    mantenimiento INTEGER NOT NULL CHECK(mantenimiento IN (0,1)),
    cierre TEXT NOT NULL
);

CREATE TABLE protocolos (
    id TEXT PRIMARY KEY,
    numero TEXT NOT NULL,
    visitaId TEXT NOT NULL REFERENCES visitas(id) DEFERRABLE INITIALLY DEFERRED,
    equipoId TEXT NOT NULL REFERENCES equipos(id) DEFERRABLE INITIALLY DEFERRED,
    creadorId TEXT NOT NULL REFERENCES usuarios(id) DEFERRABLE INITIALLY DEFERRED,
    fecha TEXT NOT NULL,
    temperatura REAL NOT NULL,
    humedad REAL NOT NULL,
    estado TEXT NOT NULL,
    mantenimiento TEXT NOT NULL,
    observaciones TEXT NOT NULL,
    reemplazaId TEXT REFERENCES protocolos(id) DEFERRABLE INITIALLY DEFERRED,
    evidencia TEXT NOT NULL
);

CREATE TABLE calibraciones (
    instrumentoId TEXT NOT NULL REFERENCES instrumentos(id),
    orden INTEGER NOT NULL,
    desde TEXT NOT NULL,
    hasta TEXT NOT NULL,
    PRIMARY KEY(instrumentoId,orden)
);

CREATE TABLE participantes (
    visitaId TEXT NOT NULL REFERENCES visitas(id),
    orden INTEGER NOT NULL,
    usuarioId TEXT NOT NULL REFERENCES usuarios(id),
    PRIMARY KEY(visitaId,orden)
);

CREATE TABLE recepciones (
    visitaId TEXT PRIMARY KEY REFERENCES visitas(id),
    nombre TEXT NOT NULL,
    rut TEXT NOT NULL,
    cargo TEXT NOT NULL,
    fecha TEXT NOT NULL
);

CREATE TABLE lineas_acta (
    visitaId TEXT NOT NULL REFERENCES visitas(id),
    orden INTEGER NOT NULL,
    texto TEXT NOT NULL,
    PRIMARY KEY(visitaId,orden)
);

CREATE TABLE mediciones (
    id TEXT PRIMARY KEY,
    protocoloId TEXT NOT NULL REFERENCES protocolos(id),
    orden INTEGER NOT NULL,
    prueba TEXT NOT NULL,
    elemento TEXT NOT NULL,
    momento TEXT NOT NULL,
    realizada INTEGER NOT NULL CHECK(realizada IN (0,1)),
    motivo TEXT NOT NULL,
    instrumentoId TEXT REFERENCES instrumentos(id),
    resultado TEXT,
    comentario TEXT NOT NULL
);

CREATE TABLE valores_medicion (
    medicionId TEXT NOT NULL REFERENCES mediciones(id),
    clave TEXT NOT NULL,
    valor REAL NOT NULL,
    PRIMARY KEY(medicionId,clave)
);

CREATE TABLE historial (
    protocoloId TEXT NOT NULL REFERENCES protocolos(id),
    orden INTEGER NOT NULL,
    usuarioId TEXT NOT NULL REFERENCES usuarios(id),
    estado TEXT NOT NULL,
    fecha TEXT NOT NULL,
    motivo TEXT NOT NULL,
    PRIMARY KEY(protocoloId,orden)
);

CREATE TABLE meta (
    clave TEXT PRIMARY KEY,
    valor TEXT NOT NULL
);

CREATE UNIQUE INDEX equipo_tag ON equipos(instalacionId,tag COLLATE NOCASE);

CREATE UNIQUE INDEX protocolo_numero ON protocolos(numero);

CREATE UNIQUE INDEX protocolo_equipo_visita ON protocolos(visitaId,equipoId);

CREATE UNIQUE INDEX visita_ot ON visitas(ot COLLATE NOCASE);