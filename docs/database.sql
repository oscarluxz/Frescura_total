-- Esquema de la actividad recuperado de ERP_FrescuraTotal_Junior.zip.
-- Instalación sin borrado: no elimina bases ni datos existentes.
-- Para una instalación nueva; si ya hay tablas, use solo migraciones compatibles.
-- *** Script Proyecto ERP Frescura Total **

-- Script de Creación de Base de Datos: erp_frescura_total
-- El estudiante puede agregar elementos a la base de datos si considera que hace falta la modelo actual


CREATE DATABASE IF NOT EXISTS erp_frescura_total CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_frescura_total;

-- TABLAS DE CATÁLOGO (LOOKUP TABLES)

CREATE TABLE estados_lote (
    id_estado INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE tipos_cliente (
    id_tipo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE estados_venta (
    id_estado INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB;

-- INFORMACIÓN DE LA EMPRESA

CREATE TABLE informacion_empresa (
    id_empresa INT AUTO_INCREMENT PRIMARY KEY,
    nombre_empresa VARCHAR(255) NOT NULL,
    nit VARCHAR(20) NOT NULL UNIQUE,
    direccion TEXT NOT NULL,
    ciudad VARCHAR(100),
    telefono VARCHAR(20),
    email VARCHAR(100)
) ENGINE=InnoDB;

-- MÓDULO: PROVEEDORES

CREATE TABLE proveedores (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    nit VARCHAR(20) NOT NULL UNIQUE,
    persona_contacto VARCHAR(100),
    email VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    direccion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- MÓDULO: PRODUCTOS

CREATE TABLE categorias_producto (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    id_categoria INT NOT NULL,
    unidad_medida VARCHAR(20) NOT NULL COMMENT 'Ej: kg, litro, unidad',
    precio_base DECIMAL(10, 2),
    FOREIGN KEY (id_categoria) REFERENCES categorias_producto(id_categoria)
) ENGINE=InnoDB;

CREATE TABLE productos_proveedor (
    id_proveedor INT NOT NULL,
    id_producto INT NOT NULL,
    PRIMARY KEY (id_proveedor, id_producto),
    FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE
) ENGINE=InnoDB;

-- MÓDULO: INVENTARIO Y CALIDAD

CREATE TABLE lotes_inventario (
    id_lote INT AUTO_INCREMENT PRIMARY KEY,
    numero_lote VARCHAR(50) NOT NULL UNIQUE,
    id_producto INT NOT NULL,
    id_proveedor INT NOT NULL,
    fecha_ingreso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento DATE NOT NULL,
    cantidad_inicial DECIMAL(10, 2) NOT NULL,
    cantidad DECIMAL(10, 2) NOT NULL,
    precio_compra DECIMAL(10, 2) NOT NULL,
    precio_venta DECIMAL(10, 2) NOT NULL,
    pasillo VARCHAR(50),
    estante VARCHAR(50),
    id_estado INT NOT NULL,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor),
    FOREIGN KEY (id_estado) REFERENCES estados_lote(id_estado),
    INDEX idx_fecha_vencimiento (fecha_vencimiento),
    INDEX idx_estado (id_estado),
    INDEX idx_producto (id_producto)
) ENGINE=InnoDB;

CREATE TABLE registro_cuarentena (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_lote INT NOT NULL,
    motivo TEXT NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resuelta BOOLEAN DEFAULT FALSE,
    fecha_resolucion DATETIME NULL,
    notas_resolucion TEXT,
    FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote),
    INDEX idx_resuelta (resuelta)
) ENGINE=InnoDB;

CREATE TABLE inspecciones_calidad (
    id_inspeccion INT AUTO_INCREMENT PRIMARY KEY,
    id_lote INT NOT NULL,
    fecha_inspeccion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resultado ENUM('Aprobado', 'Rechazado') NOT NULL,
    nombre_inspector VARCHAR(100) NOT NULL,
    motivo_rechazo TEXT COMMENT 'Motivo detallado si el resultado es Rechazado',
    notas TEXT,
    FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote),
    INDEX idx_resultado (resultado)
) ENGINE=InnoDB;

CREATE TABLE registro_mermas (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_lote INT NOT NULL,
    id_inspeccion INT COMMENT 'Inspección que generó esta merma (si aplica)',
    cantidad_perdida DECIMAL(10, 2) NOT NULL,
    motivo TEXT NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_registro VARCHAR(100),
    FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote),
    FOREIGN KEY (id_inspeccion) REFERENCES inspecciones_calidad(id_inspeccion) ON DELETE SET NULL,
    INDEX idx_fecha (fecha_registro)
) ENGINE=InnoDB;

-- MÓDULO: PROMOCIONES

CREATE TABLE promociones (
    id_promocion INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    tasa_descuento DECIMAL(5, 2) NOT NULL COMMENT 'Ej: 0.50 para 50%',
    aplica_estado_lote INT,
    aplica_categoria_producto INT,
    activa BOOLEAN DEFAULT TRUE,
    fecha_inicio DATE,
    fecha_fin DATE,
    FOREIGN KEY (aplica_estado_lote) REFERENCES estados_lote(id_estado),
    FOREIGN KEY (aplica_categoria_producto) REFERENCES categorias_producto(id_categoria),
    INDEX idx_activa (activa)
) ENGINE=InnoDB;

-- MÓDULO: CLIENTES Y VENTAS

CREATE TABLE clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    documento VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    telefono VARCHAR(20),
    direccion TEXT,
    id_tipo INT NOT NULL,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_tipo) REFERENCES tipos_cliente(id_tipo)
) ENGINE=InnoDB;

CREATE TABLE ventas (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    numero_venta VARCHAR(50) NOT NULL UNIQUE,
    id_cliente INT NOT NULL,
    fecha_venta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(12, 2) NOT NULL,
    total_descuento DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_impuesto DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    monto_total DECIMAL(12, 2) NOT NULL,
    id_estado INT NOT NULL,
    usuario_registro VARCHAR(100) COMMENT 'Usuario que registró la venta',
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    FOREIGN KEY (id_estado) REFERENCES estados_venta(id_estado),
    INDEX idx_estado (id_estado),
    INDEX idx_fecha (fecha_venta)
) ENGINE=InnoDB;

CREATE TABLE detalles_venta (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    id_lote INT NOT NULL,
    cantidad_vendida DECIMAL(10, 2) NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    descuento_aplicado DECIMAL(5, 2) DEFAULT 0.00,
    monto_descuento DECIMAL(10, 2) DEFAULT 0.00,
    tasa_impuesto DECIMAL(5, 2) DEFAULT 0.00,
    monto_impuesto DECIMAL(10, 2) DEFAULT 0.00,
    subtotal DECIMAL(10, 2) NOT NULL,
    total_linea DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE,
    FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote)
) ENGINE=InnoDB;

CREATE TABLE facturas (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    numero_factura VARCHAR(50) NOT NULL UNIQUE,
    cufe VARCHAR(255) NOT NULL UNIQUE COMMENT 'Código simulado para factura electrónica',
    enlace_qr VARCHAR(255) COMMENT 'URL o datos para generar código QR',
    fecha_emision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(12, 2) NOT NULL,
    total_descuento DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    monto_impuesto DECIMAL(12, 2) NOT NULL,
    monto_total DECIMAL(12, 2) NOT NULL,
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
    INDEX idx_numero (numero_factura)
) ENGINE=InnoDB;

-- MÓDULO: LOGÍSTICA Y ENTREGAS

CREATE TABLE conductores (
    id_conductor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    numero_licencia VARCHAR(50) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    email VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE vehiculos (
    id_vehiculo INT AUTO_INCREMENT PRIMARY KEY,
    placa VARCHAR(20) NOT NULL UNIQUE,
    modelo VARCHAR(100),
    capacidad DECIMAL(10, 2) COMMENT 'Capacidad en kg o unidades',
    estado ENUM('Activo', 'Inactivo', 'Mantenimiento') DEFAULT 'Activo'
) ENGINE=InnoDB;

CREATE TABLE rutas_entrega (
    id_ruta INT AUTO_INCREMENT PRIMARY KEY,
    nombre_ruta VARCHAR(255) NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion VARCHAR(100) COMMENT 'Usuario logístico que creó la ruta',
    hora_salida_estimada DATETIME,
    hora_llegada_estimada DATETIME,
    hora_salida_real DATETIME,
    hora_llegada_real DATETIME,
    estado ENUM('Planificada', 'En Transito', 'Completada', 'Cancelada') DEFAULT 'Planificada',
    INDEX idx_estado (estado),
    INDEX idx_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB;

CREATE TABLE asignaciones_entrega (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_ruta INT NOT NULL,
    id_vehiculo INT NOT NULL,
    id_conductor INT NOT NULL,
    fecha_asignacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    usuario_asignacion VARCHAR(100) COMMENT 'Usuario que realizó la asignación',
    notas TEXT,
    FOREIGN KEY (id_ruta) REFERENCES rutas_entrega(id_ruta),
    FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo),
    FOREIGN KEY (id_conductor) REFERENCES conductores(id_conductor),
    INDEX idx_ruta (id_ruta)
) ENGINE=InnoDB;

CREATE TABLE ventas_ruta (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_ruta INT NOT NULL,
    id_venta INT NOT NULL,
    orden_entrega INT COMMENT 'Orden de entrega en la ruta',
    direccion_destino TEXT NOT NULL,
    fecha_entrega DATETIME,
    recibido_por VARCHAR(255),
    observaciones TEXT,
    FOREIGN KEY (id_ruta) REFERENCES rutas_entrega(id_ruta),
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
    UNIQUE KEY (id_ruta, id_venta),
    INDEX idx_ruta (id_ruta),
    INDEX idx_venta (id_venta)
) ENGINE=InnoDB;

-- DATOS INICIALES (SOLO CATÁLOGOS ESENCIALES)

INSERT INTO estados_lote (nombre, descripcion) VALUES
('Disponible', 'Lote disponible para venta'),
('Proximo a Vencer', 'Lote próximo a vencer (7 días o menos)'),
('Vencido', 'Lote vencido, no apto para venta'),
('En Cuarentena', 'Lote en cuarentena pendiente de inspección');

INSERT INTO tipos_cliente (nombre, descripcion) VALUES
('Mayorista', 'Cliente que compra en grandes cantidades'),
('Minorista', 'Cliente que compra en pequeñas cantidades');

INSERT INTO estados_venta (nombre, descripcion) VALUES
('Registrada', 'Venta registrada, pendiente de preparación'),
('En Preparacion', 'Pedido en preparación para despacho'),
('En Transito', 'Pedido en tránsito hacia el cliente'),
('Entregada', 'Pedido entregado al cliente'),
('Cancelada', 'Venta cancelada');

INSERT INTO informacion_empresa (nombre_empresa, nit, direccion, ciudad, telefono, email) VALUES
('Frescura Total S.A.S.', '900123456-7', 'Calle 123 #45-67', 'Bogotá', '(601) 234-5678', 'contacto@frescuratotal.com');

INSERT INTO categorias_producto (nombre, descripcion) VALUES
('Frutas', 'Frutas frescas'),
('Verduras', 'Verduras y hortalizas'),
('Lacteos', 'Productos lácteos'),
('Carnes', 'Carnes y derivados'),
('Granos', 'Granos y cereales'),
('Bebidas', 'Bebidas y líquidos'),
('Enlatados', 'Productos enlatados'),
('Panaderia', 'Productos de panadería');

-- Extensión de inventario para reintentos de mermas.
CREATE TABLE IF NOT EXISTS operaciones_merma (
    clave_operacion VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    id_registro INT NOT NULL UNIQUE,
    FOREIGN KEY (id_registro) REFERENCES registro_mermas(id_registro)
) ENGINE=InnoDB;
