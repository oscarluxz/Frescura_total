-- Extensión aditiva: clave única para no repetir una baja al reintentar.
-- Ejecutar sobre la base seleccionada, después del esquema de la actividad.
CREATE TABLE IF NOT EXISTS operaciones_merma (
    clave_operacion VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    id_registro INT NOT NULL UNIQUE,
    FOREIGN KEY (id_registro) REFERENCES registro_mermas(id_registro)
) ENGINE=InnoDB;
