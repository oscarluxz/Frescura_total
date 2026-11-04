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


-- ============================================
-- DATOS DE PRUEBA (DML)
-- Se cargan en este orden, despues del esquema y los catalogos.
-- ============================================

-- ===== dml_suppliers_logistics.sql =====
-- Datos de prueba de Oscar: proveedores, conductores y vehiculos
-- Ejecutar despues del DDL. Requiere base de datos vacia en estas tablas.

-- PROVEEDORES (55 registros; los ids 7, 19, 28, 41 y 50 estan inactivos)
INSERT INTO proveedores (id_proveedor, nombre, nit, persona_contacto, email, telefono, direccion, activo) VALUES
(1, 'Frutiandina Ltda.', '821760089-3', 'Felipe Moreno Arango', 'felipe.moreno@gmail.com', '3019871205', 'Carrera 72 #95-88, Barranquilla', TRUE),
(2, 'Siembras del Norte S.A.S.', '823933272-6', 'Alexánder Moreno Quintero', 'alexander.moreno@gmail.com', '3166383408', 'Calle 99 #44-59, Tunja', TRUE),
(3, 'Lácteos Boyacá Ltda.', '870911337-2', 'Paola Rodríguez García', 'ventas@lacteosboyaca.com.co', '3239098020', 'Calle 19 #51-47, Cali', TRUE),
(4, 'Carnes Premium S.A.S.', '865093398-2', 'Sergio Moreno Montoya', 'ventas@carnespremium.com.co', '3141778017', 'Carrera 18 #44-27, Funza', TRUE),
(5, 'Granero Nacional Ltda.', '900869742-1', 'Wilson Rodríguez Montoya', 'wilson.rodriguez@gmail.com', '3013254055', 'Carrera 18 #91-52, Villavicencio', TRUE),
(6, 'Jugos Naturales S.A.S.', '870384277-7', 'Camilo Restrepo Acosta', 'camilo.restrepo@gmail.com', '3116534278', 'Calle 110 #8-28, Bogotá', TRUE),
(7, 'Alimentos en Conserva y Cía. S.A.S.', '857252561-0', 'Luisa Acosta Jiménez', 'luisa.acosta@outlook.com', '3136406339', 'Carrera 115 #58-92, Funza', FALSE),
(8, 'Trigales y Cía. S.A.S.', '847256752-2', 'Néstor Gutiérrez Cruz', 'ventas@trigales.com.co', '3502597663', 'Carrera 100 #93-69, Bogotá', TRUE),
(9, 'Agrofrutas S.A.', '891891747-8', 'Luisa Quintero Pardo', 'ventas@agrofrutas.com.co', '3515730688', 'Carrera 109 #72-97, Funza', TRUE),
(10, 'La Huerta Orgánica S.A.', '815984090-1', 'Liliana Aguirre Herrera', 'ventas@lahuertaorganica.com.co', '3006865152', 'Carrera 101 #45-69, Duitama', TRUE),
(11, 'Lácteos Sabana Ltda.', '822992677-3', 'Liliana Castro Beltrán', 'ventas@lacteossabana.com.co', '3014217725', 'Calle 57 #26-65, Bogotá', TRUE),
(12, 'Embutidos El Prado Ltda.', '901819504-5', 'Carolina Arango Martínez', 'carolina.arango@gmail.com', '3202290277', 'Calle 77 #52-26, Mosquera', TRUE),
(13, 'Granero Nacional S.A.', '882153560-1', 'Diana Restrepo Leal', 'ventas@graneronacional.com.co', '3203779128', 'Carrera 76 #9-66, Bogotá', TRUE),
(14, 'Bebidas Montecarlo S.A.S.', '839297654-5', 'Tatiana Vargas Salazar', 'ventas@bebidasmontecarlo.com.co', '3232021037', 'Carrera 85 #98-65, Barranquilla', TRUE),
(15, 'Alimentos Preservados S.A.S.', '833540533-2', 'Alexánder Mendoza Cárdenas', 'ventas@alimentospreservados.com.co', '3501986513', 'Carrera 20 #3-96, Duitama', TRUE),
(16, 'Panificadora La Espiga y Cía. S.A.S.', '862008835-7', 'Gustavo Castro Ramírez', 'gustavo.castro@gmail.com', '3176854635', 'Carrera 47 #10-12, Funza', TRUE),
(17, 'Cultivos de Sumapaz S.A.', '814651192-1', 'Tatiana Rodríguez Ospina', 'ventas@cultivosdesumapaz.com.co', '3516333805', 'Carrera 97 #56-24, Duitama', TRUE),
(18, 'Lácteos Sabana S.A.S.', '887422410-6', 'Julián Parra Montoya', 'julian.parra@outlook.com', '3029316855', 'Calle 25 #34-92, Bucaramanga', TRUE),
(19, 'Embutidos El Prado S.A.', '875651643-3', 'Julián Ríos Ríos', 'ventas@embutidoselprado.com.co', '3501218586', 'Carrera 50 #35-60, Villavicencio', FALSE),
(20, 'Agrograno S.A.S.', '896808267-1', 'Álvaro Vargas Posada', 'alvaro.vargas@gmail.com', '3509792095', 'Carrera 47 #1-89, Bogotá', TRUE),
(21, 'Bebidas Refrescantes S.A.', '874827825-0', 'Sonia Posada Posada', 'ventas@bebidasrefrescantes.com.co', '3159960831', 'Carrera 18 #47-56, Villavicencio', TRUE),
(22, 'Conservas El Cóndor S.A.S.', '878120892-3', 'Tatiana Díaz Salazar', 'ventas@conservaselcondor.com.co', '3188586888', 'Carrera 107 #68-90, Duitama', TRUE),
(23, 'Trigales S.A.S.', '844122361-1', 'Beatriz Castro Gutiérrez', 'beatriz.castro@outlook.com', '3183807505', 'Carrera 81 #49-6, Barranquilla', TRUE),
(24, 'Tropifruta S.A.S.', '801737342-3', 'Carlos Cortés Quintero', 'ventas@tropifruta.com.co', '3221051045', 'Carrera 96 #4-48, Bogotá', TRUE),
(25, 'Siembras del Norte y Cía. S.A.S.', '835100029-1', 'Milena Medina Pérez', 'milena.medina@hotmail.com', '3214183370', 'Calle 70 #81-18, Mosquera', TRUE),
(26, 'Lácteos Santa Rosa S.A.', '856443244-7', 'Jorge Arango Leal', 'ventas@lacteossantarosa.com.co', '3221769092', 'Calle 93 #38-74, Bucaramanga', TRUE),
(27, 'Granos y Cereales S.A.S.', '864245374-6', 'Gustavo Mendoza Vega', 'ventas@granosycereales.com.co', '3046426276', 'Carrera 20 #32-48, Medellín', TRUE),
(28, 'Refrescos La Cumbre S.A.S.', '862892288-8', 'Milena Leal Martínez', 'milena.leal@gmail.com', '3214777639', 'Carrera 92 #65-44, Duitama', FALSE),
(29, 'Campo Fresco Ltda.', '901618381-3', 'Marcela Suárez Acosta', 'ventas@campofresco.com.co', '3176183501', 'Calle 8 #68-58, Bucaramanga', TRUE),
(30, 'Ordeño Fresco Ltda.', '850136570-5', 'Alexánder Jiménez Beltrán', 'ventas@ordenofresco.com.co', '3049467262', 'Calle 22 #46-98, Tunja', TRUE),
(31, 'Ganadera Los Robles S.A.S.', '829264463-7', 'Leonardo Pérez Medina', 'ventas@ganaderalosrobles.com.co', '3233255659', 'Carrera 67 #13-11, Ibagué', TRUE),
(32, 'Arrocera Tolima S.A.', '899930136-5', 'Marcela Sánchez Ortiz', 'ventas@arroceratolima.com.co', '3005066314', 'Calle 102 #71-93, Bogotá', TRUE),
(33, 'Café y Bebidas S.A.', 'SYN-FT-900427-AG', 'Alexánder Cárdenas Vega', 'ventas@cafeybebidas.com.co', '3122715228', 'Carrera 8 #20-21, Ibagué', TRUE),
(34, 'Conservas Sol S.A.S.', '822830333-0', 'Milena Sánchez Ospina', 'ventas@conservassol.com.co', '3185719865', 'Calle 117 #84-82, Bogotá', TRUE),
(35, 'Hornos del Centro S.A.S.', '864355467-4', 'Marcela Gutiérrez Medina', 'ventas@hornosdelcentro.com.co', '3126579717', 'Calle 43 #81-18, Mosquera', TRUE),
(36, 'Cosecha Dorada S.A.S.', '854391957-8', 'Ricardo Pardo Suárez', 'ventas@cosechadorada.com.co', '3044375364', 'Carrera 83 #95-46, Villavicencio', TRUE),
(37, 'Hortalizas del Tolima S.A.S.', '811746390-1', 'Jhon Parra Aguirre', 'jhon.parra@hotmail.com', '3514071006', 'Carrera 51 #2-11, Funza', TRUE),
(38, 'Alpina Local y Cía. S.A.S.', '808973334-0', 'Camilo Medina Cortés', 'ventas@alpinalocal.com.co', '3238531054', 'Carrera 84 #39-50, Bogotá', TRUE),
(39, 'Frigorífico Sabana y Cía. S.A.S.', '825642930-2', 'Edwin Quintero Rojas', 'ventas@frigorificosabana.com.co', '3515785471', 'Carrera 6 #23-71, Barranquilla', TRUE),
(40, 'Molino Santa Fe S.A.S.', '877503289-2', 'Andrés Restrepo Rojas', 'ventas@molinosantafe.com.co', '3232245105', 'Carrera 14 #85-44, Barranquilla', TRUE),
(41, 'Atuneros del Caribe Ltda.', '881191546-4', 'María Pardo Arango', 'ventas@atunerosdelcaribe.com.co', '3155549134', 'Carrera 79 #45-8, Barranquilla', FALSE),
(42, 'Huerta Tropical Ltda.', '838141731-0', 'Marcela Díaz Medina', 'marcela.diaz@gmail.com', '3154462274', 'Calle 104 #81-83, Mosquera', TRUE),
(43, 'Avícola Campestre S.A.S.', '801610117-6', 'Miguel Martínez Restrepo', 'ventas@avicolacampestre.com.co', '3166380869', 'Carrera 64 #64-8, Villavicencio', TRUE),
(44, 'Molinos del Sur S.A.S.', '814475570-8', 'Milena Cárdenas Montoya', 'milena.cardenas@outlook.com', '3127102806', 'Carrera 57 #5-66, Villavicencio', TRUE),
(45, 'Envasados Colombia Ltda.', '851277736-9', 'Julián Arango Suárez', 'ventas@envasadoscolombia.com.co', '3117451362', 'Carrera 95 #44-23, Ibagué', TRUE),
(46, 'Naranjales S.A.S.', '887967983-3', 'Milena Leal Suárez', 'ventas@naranjales.com.co', '3106109165', 'Calle 105 #7-28, Bogotá', TRUE),
(47, 'Verduras Cundinamarca Ltda.', '852052678-7', 'Beatriz Mendoza Mendoza', 'ventas@verdurascundinamarca.com.co', '3133013626', 'Calle 32 #37-65, Mosquera', TRUE),
(48, 'Molinos del Sur Ltda.', '806404459-5', 'Miguel Posada Montoya', 'ventas@molinosdelsur.com.co', '3507528600', 'Calle 85 #62-8, Bucaramanga', TRUE),
(49, 'Refrescos La Cumbre Ltda.', '875205173-1', 'Hernán Aguirre Ríos', 'hernan.aguirre@outlook.com', '3018258115', 'Calle 40 #82-38, Mosquera', TRUE),
(50, 'Conservas Nueva Era S.A.S.', '824026186-3', 'Álvaro Vargas Ortiz', 'ventas@conservasnuevaera.com.co', '3207527366', 'Carrera 20 #35-76, Bogotá', FALSE),
(51, 'Panificadora La Espiga S.A.', '867172763-4', 'Alejandra Salazar Ortiz', 'ventas@panificadoralaespiga.com.co', '3519806798', 'Carrera 99 #46-56, Duitama', TRUE),
(52, 'Verde Andino y Cía. S.A.S.', '872429769-1', 'Adriana Ortiz Pardo', 'ventas@verdeandino.com.co', '3029669737', 'Carrera 5 #3-31, Funza', TRUE),
(53, 'Quesos del Páramo S.A.S.', '855454107-1', 'Camilo Cárdenas Salazar', 'camilo.cardenas@hotmail.com', '3042698825', 'Carrera 25 #82-5, Duitama', TRUE),
(54, 'Cereales Huila Ltda.', '853050157-6', 'Álvaro Gómez Pérez', 'ventas@cerealeshuila.com.co', '3017783494', 'Calle 85 #71-96, Bogotá', TRUE),
(55, 'Embotelladora Andina Ltda.', '835954527-2', 'Edwin López Moreno', 'edwin.lopez@outlook.com', '3125057809', 'Carrera 31 #36-28, Duitama', TRUE);

-- CONDUCTORES (50 registros)
INSERT INTO conductores (nombre, numero_licencia, telefono, email, activo) VALUES
('Sonia Cárdenas Leal', 'B1-87937205', '3169707209', 'sonia.cardenas77@outlook.com', TRUE),
('Gloria Quintero López', 'C3-11398324', '3215510997', 'gloria.quintero82@outlook.com', TRUE),
('Edwin Rojas Cárdenas', 'C1-66669821', '3017223326', 'edwin.rojas10@outlook.com', TRUE),
('Milena Ospina Moreno', 'C3-83074612', '3184704335', 'milena.ospina12@hotmail.com', TRUE),
('Fabián Herrera Jiménez', 'C1-45903905', '3171541586', 'fabian.herrera54@hotmail.com', TRUE),
('Beatriz Salazar Pérez', 'C1-42086776', '3152968771', 'beatriz.salazar59@outlook.com', TRUE),
('Gloria Aguirre Ortiz', 'B1-16630922', '3172321224', 'gloria.aguirre78@hotmail.com', TRUE),
('Claudia García Vega', 'C3-53494079', '3219236204', 'claudia.garcia66@outlook.com', TRUE),
('Carolina Díaz Montoya', 'C2-84729278', '3025986224', 'carolina.diaz82@outlook.com', FALSE),
('Catalina Cruz Pardo', 'C1-63492788', '3173161801', 'catalina.cruz94@hotmail.com', TRUE),
('Gloria Ríos Duarte', 'B1-57959503', '3173012983', 'gloria.rios72@gmail.com', TRUE),
('Ricardo Ramírez Sánchez', 'C2-37489322', '3239007406', 'ricardo.ramirez35@gmail.com', TRUE),
('Miguel Beltrán Parra', 'C2-14616566', '3011733741', 'miguel.beltran6@hotmail.com', TRUE),
('Jhon Leal García', 'C2-97294451', '3518201560', 'jhon.leal45@gmail.com', TRUE),
('Esperanza Acosta Rodríguez', 'C2-27951901', '3516465240', 'esperanza.acosta13@outlook.com', TRUE),
('Felipe Duarte Leal', 'C1-95789228', '3042342476', 'felipe.duarte8@hotmail.com', TRUE),
('Esperanza Posada Martínez', 'B1-13510197', '3104969177', 'esperanza.posada48@outlook.com', TRUE),
('Sergio Arango Duarte', 'C1-25723710', '3103181285', 'sergio.arango97@hotmail.com', TRUE),
('Oscar Castro Rodríguez', 'C1-86081914', '3181248017', 'oscar.castro97@outlook.com', TRUE),
('Catalina Acosta López', 'C1-30733571', '3115184961', 'catalina.acosta10@gmail.com', TRUE),
('Alejandra Cruz Cárdenas', 'C3-73260159', '3043086424', 'alejandra.cruz35@hotmail.com', TRUE),
('Fabián Díaz Suárez', 'B1-44218842', '3118816280', 'fabian.diaz30@hotmail.com', FALSE),
('Darío Restrepo García', 'C1-17463659', '3117768916', 'dario.restrepo78@outlook.com', TRUE),
('Sergio Martínez García', 'C3-22511056', '3048253828', 'sergio.martinez79@gmail.com', TRUE),
('Lorena Díaz Suárez', 'B1-64594373', '3217075013', 'lorena.diaz1@gmail.com', TRUE),
('Luisa Medina Acosta', 'C2-49467110', '3165466570', 'luisa.medina31@outlook.com', TRUE),
('Miguel Gutiérrez Quintero', 'C2-28579951', '3516591409', 'miguel.gutierrez31@gmail.com', TRUE),
('Andrés Quintero Gutiérrez', 'C1-91565034', '3114714414', 'andres.quintero21@hotmail.com', TRUE),
('Claudia García Pérez', 'C1-20284862', '3145673016', 'claudia.garcia42@gmail.com', TRUE),
('Rodrigo Quintero Ríos', 'B1-27082538', '3179123270', 'rodrigo.quintero79@gmail.com', TRUE),
('Adriana Ortiz Mendoza', 'C1-52981015', '3016049142', 'adriana.ortiz37@gmail.com', TRUE),
('Fabián Arango Medina', 'C3-31821586', '3505998156', 'fabian.arango26@gmail.com', TRUE),
('Esperanza Moreno Martínez', 'C3-22843766', '3113858271', 'esperanza.moreno1@gmail.com', TRUE),
('Ricardo Salazar Parra', 'C2-30755903', '3233764305', 'ricardo.salazar92@hotmail.com', TRUE),
('Camilo Ramírez López', 'C3-38832677', '3143377938', 'camilo.ramirez52@outlook.com', TRUE),
('Álvaro Restrepo Gutiérrez', 'C3-34345633', '3136686286', 'alvaro.restrepo54@gmail.com', TRUE),
('Julián Beltrán Pérez', 'C3-87448956', '3184752469', 'julian.beltran22@hotmail.com', FALSE),
('Julián Pérez Leal', 'C3-64024099', '3187011382', 'julian.perez73@gmail.com', TRUE),
('Lorena Vega Montoya', 'B1-44754629', '3148898567', 'lorena.vega48@hotmail.com', TRUE),
('Paola Cárdenas Vega', 'C3-80845055', '3163990168', 'paola.cardenas77@outlook.com', TRUE),
('Néstor Cortés Ríos', 'B1-18320801', '3049908098', 'nestor.cortes3@gmail.com', TRUE),
('Héctor Restrepo Acosta', 'C2-81298191', '3101858549', 'hector.restrepo73@outlook.com', TRUE),
('Catalina Jiménez García', 'C2-27023864', '3047652405', 'catalina.jimenez61@hotmail.com', TRUE),
('Andrés Acosta Gómez', 'C3-60165218', '3102634104', 'andres.acosta52@outlook.com', FALSE),
('Alejandra Ríos Mendoza', 'C1-21068875', '3222691467', 'alejandra.rios25@hotmail.com', TRUE),
('Beatriz Vargas Sánchez', 'C2-82738650', '3136874856', 'beatriz.vargas53@gmail.com', TRUE),
('Beatriz Cruz Duarte', 'C1-41252321', '3022630917', 'beatriz.cruz10@outlook.com', TRUE),
('Marcela Martínez Quintero', 'C3-43585235', '3507117604', 'marcela.martinez28@outlook.com', TRUE),
('Luisa Castro Díaz', 'C1-13628572', '3213635379', 'luisa.castro86@gmail.com', TRUE),
('Adriana Posada Gómez', 'C2-82398902', '3164537165', 'adriana.posada49@gmail.com', TRUE);

-- VEHICULOS (50 registros)
INSERT INTO vehiculos (placa, modelo, capacidad, estado) VALUES
('VFX499', 'Ford Cargo 816 2020', 5500.00, 'Activo'),
('LMS488', 'JAC 1063 2024', 4000.00, 'Activo'),
('UTL990', 'Hyundai HD65 2020', 3500.00, 'Activo'),
('TEH842', 'Chevrolet NHR 2019', 2800.00, 'Activo'),
('HZY897', 'Isuzu ELF 2018', 3000.00, 'Activo'),
('YXM957', 'Dongfeng Captain 2023', 3000.00, 'Mantenimiento'),
('PVB233', 'Mitsubishi Canter 2022', 3200.00, 'Activo'),
('YTY990', 'Hyundai HD78 2021', 4500.00, 'Activo'),
('STE302', 'Hino Dutro 2017', 4200.00, 'Activo'),
('GDF743', 'Isuzu ELF 2022', 3000.00, 'Activo'),
('FDU167', 'Hyundai HD78 2018', 4500.00, 'Activo'),
('BTL641', 'Ford Cargo 816 2019', 5500.00, 'Inactivo'),
('KAK868', 'Hino Dutro 2022', 4200.00, 'Activo'),
('GGY984', 'Dongfeng Captain 2024', 3000.00, 'Activo'),
('ASN538', 'Foton BJ1049 2018', 2200.00, 'Activo'),
('AVG271', 'Ford Cargo 816 2022', 5500.00, 'Activo'),
('UNX986', 'Kia K2700 2016', 1500.00, 'Activo'),
('RAV461', 'Kia Frontier 2017', 2500.00, 'Mantenimiento'),
('KLY739', 'JAC 1063 2023', 4000.00, 'Activo'),
('RFT832', 'Renault Master 2017', 1300.00, 'Activo'),
('UKA932', 'Chevrolet NPR 2019', 5000.00, 'Activo'),
('VYK877', 'Kia Frontier 2018', 2500.00, 'Activo'),
('TAP604', 'Hino Dutro 2022', 4200.00, 'Activo'),
('EYU345', 'Hino Dutro 2021', 4200.00, 'Activo'),
('LUE419', 'Chevrolet NPR 2017', 5000.00, 'Activo'),
('HUK283', 'JAC 1063 2024', 4000.00, 'Activo'),
('KVL269', 'Dongfeng Captain 2019', 3000.00, 'Activo'),
('MHJ919', 'Foton BJ1049 2018', 2200.00, 'Activo'),
('ZET840', 'Hyundai HD78 2023', 4500.00, 'Inactivo'),
('CZV231', 'Ford Cargo 816 2020', 5500.00, 'Activo'),
('YVM602', 'Chevrolet NPR 2018', 5000.00, 'Mantenimiento'),
('BNC828', 'Hyundai HD65 2020', 3500.00, 'Activo'),
('FHP494', 'Kia Frontier 2022', 2500.00, 'Activo'),
('JWJ531', 'Chevrolet NPR 2024', 5000.00, 'Activo'),
('FGF256', 'Hino Dutro 2023', 4200.00, 'Activo'),
('RTW400', 'Chevrolet NHR 2023', 2800.00, 'Activo'),
('YEP130', 'Isuzu ELF 2022', 3000.00, 'Activo'),
('PXG248', 'Hyundai HD65 2021', 3500.00, 'Activo'),
('YJL827', 'Hino Dutro 2016', 4200.00, 'Activo'),
('LMZ368', 'Hyundai HD78 2020', 4500.00, 'Activo'),
('YAR729', 'JAC 1040 2024', 2500.00, 'Activo'),
('DAM265', 'Kia Frontier 2017', 2500.00, 'Activo'),
('TTN798', 'Chevrolet NKR 2017', 3500.00, 'Inactivo'),
('LXT443', 'Hyundai HD78 2023', 4500.00, 'Activo'),
('SBP104', 'Ford Cargo 816 2022', 5500.00, 'Activo'),
('JDB550', 'Renault Master 2018', 1300.00, 'Activo'),
('RWD199', 'Ford Cargo 816 2022', 5500.00, 'Mantenimiento'),
('GZU701', 'Hino Dutro 2020', 4200.00, 'Activo'),
('JUJ167', 'Isuzu ELF 2020', 3000.00, 'Activo'),
('UFU579', 'Chevrolet NKR 2019', 3500.00, 'Activo');

-- ===== dml_customers.sql =====
-- Clientes de prueba: 25 empresas (Mayorista) y 25 personas (Minorista).
-- Requiere el catalogo tipos_cliente de database.sql: 1 = Mayorista, 2 = Minorista.
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES
('Julián Ramírez Pardo', '574277948', 'julian.ramirez76@outlook.com', '3504536932', 'Avenida Calle 116 #83-29, Santa Fe, Bogotá', 2),
('Tienda Plaza Mayor', '863843210-9', 'compras@tiendaplazamayor.com.co', '3115970784', 'Carrera 94 #55-50, Usaquén, Bogotá', 1),
('Andrés Cortés Vargas', '561542159', 'andres.cortes80@gmail.com', '3116768399', 'Carrera 21 #94-40, Teusaquillo, Bogotá', 2),
('Asadero Del Parque', '860785487-1', 'compras@asaderodelparque.com.co', '3217262217', 'Transversal 33 #39-62, Puente Aranda, Bogotá', 1),
('Luisa Cortés Martínez', '672881578', 'luisa.cortes6@gmail.com', '3115494646', 'Calle 140 #74-7, Teusaquillo, Bogotá', 2),
('Colegio Bella Vista', '856746745-6', 'compras@colegiobellavista.com.co', '3139860522', 'Calle 54 #25-82, Tunjuelito, Bogotá', 1),
('Iván Herrera Torres', '151174374', 'ivan.herrera11@outlook.com', '3508506800', 'Transversal 148 #13-22, Barrios Unidos, Bogotá', 2),
('Fruver Cerros Orientales', '808037816-4', 'compras@fruvercerrosorientales.com.co', '3127304165', 'Avenida Calle 65 #69-18, Barrios Unidos, Bogotá', 1),
('Marcela Moreno Díaz', '716868580', 'marcela.moreno84@outlook.com', '3211953923', 'Carrera 90 #42-68, Kennedy, Bogotá', 2),
('Tienda La Economía', '803555340-4', 'compras@tiendalaeconomia.com.co', '3505893219', 'Transversal 71 #4-81, Usaquén, Bogotá', 1),
('Néstor Rodríguez Sánchez', '501029319', 'nestor.rodriguez27@hotmail.com', '3162856124', 'Transversal 107 #42-1, Salitre, Bogotá', 2),
('Panadería Don Pedro', '817771938-4', 'compras@panaderiadonpedro.com.co', '3232898112', 'Carrera 25 #20-14, Usaquén, Bogotá', 1),
('Andrés Díaz Sánchez', '650165007', 'andres.diaz9@gmail.com', '3148902362', 'Carrera 144 #90-86, Restrepo, Bogotá', 2),
('Fruver Villa Rosa', '866017712-2', 'compras@fruvervillarosa.com.co', '3013799131', 'Carrera 139 #9-84, Tunjuelito, Bogotá', 1),
('Néstor Pérez Herrera', '206014274', 'nestor.perez41@gmail.com', '3229635044', 'Calle 93 #11-83, Engativá, Bogotá', 2),
('Supermercado Nueva Granada', '831471020-2', 'compras@supermercadonuevagranada.com.co', '3166985094', 'Transversal 98 #4-37, Engativá, Bogotá', 1),
('Andrés Castro Ortiz', '359870312', 'andres.castro8@outlook.com', '3206286005', 'Transversal 65 #95-44, Teusaquillo, Bogotá', 2),
('Cafetería El Gran Mercado', '807026996-0', 'compras@cafeteriaelgranmercado.com.co', '3235044664', 'Calle 36 #46-83, Fontibón, Bogotá', 1),
('Diana Rodríguez Moreno', '930021404', 'diana.rodriguez21@hotmail.com', '3134282185', 'Transversal 72 #23-61, Kennedy, Bogotá', 2),
('Hotel Central', '870689765-1', 'compras@hotelcentral.com.co', '3507559255', 'Transversal 27 #13-35, Tunjuelito, Bogotá', 1),
('Iván Ramírez Pérez', '63469851', 'ivan.ramirez79@outlook.com', '3228513174', 'Carrera 20 #70-54, Suba, Bogotá', 2),
('Casino San Martín', '832157218-1', 'compras@casinosanmartin.com.co', '3005917166', 'Avenida Calle 137 #81-5, Usaquén, Bogotá', 1),
('Néstor Acosta Gutiérrez', '804844731', 'nestor.acosta20@hotmail.com', '3505834477', 'Calle 38 #77-79, Santa Fe, Bogotá', 2),
('Clínica Mi Barrio', '808279085-5', 'compras@clinicamibarrio.com.co', '3205777258', 'Avenida Calle 29 #22-99, Usaquén, Bogotá', 1),
('Carlos Rojas Cortés', '423142974', 'carlos.rojas16@hotmail.com', '3209142739', 'Calle 120 #1-24, Tunjuelito, Bogotá', 2),
('Hotel San Jorge', '839162011-1', 'compras@hotelsanjorge.com.co', '3165026206', 'Calle 69 #42-33, Puente Aranda, Bogotá', 1),
('Diana Pardo Vargas', '969118283', 'diana.pardo22@hotmail.com', '3229576915', 'Calle 140 #56-28, Salitre, Bogotá', 2),
('Casino El Buen Sabor', '848774094-1', 'compras@casinoelbuensabor.com.co', '3219585529', 'Avenida Calle 44 #70-55, Teusaquillo, Bogotá', 1),
('María Ortiz Beltrán', '440782322', 'maria.ortiz95@gmail.com', '3112174445', 'Transversal 144 #9-94, Santa Fe, Bogotá', 2),
('Minimercado La Esperanza', '878805350-4', 'compras@minimercadolaesperanza.com.co', '3132902027', 'Avenida Calle 70 #73-38, Usaquén, Bogotá', 1),
('Adriana Rodríguez Torres', '268251082', 'adriana.rodriguez78@hotmail.com', '3108845020', 'Transversal 61 #14-40, Salitre, Bogotá', 2),
('Asadero La Palma', '859988598-9', 'compras@asaderolapalma.com.co', '3005574437', 'Calle 49 #60-97, Tunjuelito, Bogotá', 1),
('Andrés Martínez Castro', '460054937', 'andres.martinez4@gmail.com', '3129028653', 'Carrera 9 #87-80, Kennedy, Bogotá', 2),
('Colegio Las Delicias', '842243907-2', 'compras@colegiolasdelicias.com.co', '3142727816', 'Avenida Calle 23 #20-14, Salitre, Bogotá', 1),
('Johana García Gutiérrez', '1077636380', 'johana.garcia12@gmail.com', '3518015441', 'Calle 58 #32-16, Kennedy, Bogotá', 2),
('Cafetería El Dorado', '880318099-8', 'compras@cafeteriaeldorado.com.co', '3131054401', 'Transversal 78 #16-81, Suba, Bogotá', 1),
('Jorge Pardo Cárdenas', '638943056', 'jorge.pardo77@gmail.com', '3005422553', 'Calle 123 #11-50, Teusaquillo, Bogotá', 2),
('Clínica Santa Clara', '819523243-9', 'compras@clinicasantaclara.com.co', '3168786887', 'Calle 119 #94-53, Tunjuelito, Bogotá', 1),
('Camilo Ortiz López', '1013213919', 'camilo.ortiz63@hotmail.com', '3112146867', 'Carrera 123 #88-89, Suba, Bogotá', 2),
('Supermercado La Sabana', '885798514-1', 'compras@supermercadolasabana.com.co', '3145622241', 'Avenida Calle 103 #17-24, Chapinero, Bogotá', 1),
('Hernán Jiménez Acosta', '430968422', 'hernan.jimenez33@outlook.com', '3126600881', 'Carrera 39 #47-34, Bosa, Bogotá', 2),
('Colegio Los Andes', '862754688-1', 'compras@colegiolosandes.com.co', '3128225964', 'Carrera 54 #31-8, Cedritos, Bogotá', 1),
('Natalia Vargas Cortés', '137752039', 'natalia.vargas6@hotmail.com', '3519928935', 'Calle 16 #14-7, Usaquén, Bogotá', 2),
('Casino Casa Real', '816722647-3', 'compras@casinocasareal.com.co', '3222132088', 'Calle 76 #40-87, Engativá, Bogotá', 1),
('Néstor Moreno Díaz', '389741611', 'nestor.moreno73@gmail.com', '3123740013', 'Carrera 143 #49-53, Barrios Unidos, Bogotá', 2),
('Fruver El Trébol', '842850990-8', 'compras@fruvereltrebol.com.co', '3511518886', 'Carrera 13 #4-72, Puente Aranda, Bogotá', 1),
('Natalia Mendoza Acosta', '993199688', 'natalia.mendoza96@gmail.com', '3122979648', 'Calle 148 #25-54, Bosa, Bogotá', 2),
('Supermercado El Fogón Llanero', '820936526-4', 'compras@supermercadoelfogonllane.com.co', '3221455075', 'Transversal 79 #69-30, Engativá, Bogotá', 1),
('Luisa López Vargas', '93580023', 'luisa.lopez9@gmail.com', '3224222026', 'Transversal 150 #27-65, Suba, Bogotá', 2),
('Clínica El Corral', '826709534-2', 'compras@clinicaelcorral.com.co', '3215364887', 'Transversal 100 #6-79, Puente Aranda, Bogotá', 1);

-- ===== dml_inventory.sql =====
-- Datos ficticios de integración de Junior: inventario y calidad.
-- Cargar UNA SOLA VEZ después del DDL/catálogos y dml_suppliers_logistics.sql.
-- Requiere libres los IDs 1001..1080 de lotes y los rangos indicados en dml_inventory.md.
-- Proveedores activos usados del archivo de Oscar: 1, 2, 3, 4, 5, 6, 8 y 15.
-- Catálogos base: categorías 1..8; estados 1 Disponible, 2 Proximo a Vencer,
-- 3 Vencido, 4 En Cuarentena. Fechas relativas al día de carga.
-- Solo INSERT; no cambia de base, no crea tablas y no sobrescribe datos.
-- Después se carga el DML de ventas de Thomas, referenciando estos IDs de lote.

-- 1. PRODUCTOS: 50 registros, IDs 1001..1050.
INSERT INTO productos (id_producto, nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES
(1001, 'Manzana roja', 'Producto ficticio para pruebas de integración del ERP.', 1, 'kg', 4200.00),
(1002, 'Banano', 'Producto ficticio para pruebas de integración del ERP.', 1, 'kg', 2800.00),
(1003, 'Mango Tommy', 'Producto ficticio para pruebas de integración del ERP.', 1, 'kg', 3900.00),
(1004, 'Naranja Valencia', 'Producto ficticio para pruebas de integración del ERP.', 1, 'kg', 3000.00),
(1005, 'Papaya Maradol', 'Producto ficticio para pruebas de integración del ERP.', 1, 'kg', 3200.00),
(1006, 'Piña Golden', 'Producto ficticio para pruebas de integración del ERP.', 1, 'kg', 3800.00),
(1007, 'Uva Isabella', 'Producto ficticio para pruebas de integración del ERP.', 1, 'kg', 6500.00),
(1008, 'Tomate chonto', 'Producto ficticio para pruebas de integración del ERP.', 2, 'kg', 3500.00),
(1009, 'Cebolla cabezona', 'Producto ficticio para pruebas de integración del ERP.', 2, 'kg', 2600.00),
(1010, 'Zanahoria', 'Producto ficticio para pruebas de integración del ERP.', 2, 'kg', 2400.00),
(1011, 'Papa criolla', 'Producto ficticio para pruebas de integración del ERP.', 2, 'kg', 4800.00),
(1012, 'Lechuga', 'Producto ficticio para pruebas de integración del ERP.', 2, 'kg', 3000.00),
(1013, 'Cilantro', 'Producto ficticio para pruebas de integración del ERP.', 2, 'kg', 6000.00),
(1014, 'Brócoli', 'Producto ficticio para pruebas de integración del ERP.', 2, 'kg', 5200.00),
(1015, 'Leche entera 1 litro', 'Producto ficticio para pruebas de integración del ERP.', 3, 'unidad', 4200.00),
(1016, 'Leche deslactosada 1 litro', 'Producto ficticio para pruebas de integración del ERP.', 3, 'unidad', 4800.00),
(1017, 'Yogur natural 1 litro', 'Producto ficticio para pruebas de integración del ERP.', 3, 'unidad', 8500.00),
(1018, 'Cuajada campesina 500 gramos', 'Producto ficticio para pruebas de integración del ERP.', 3, 'unidad', 10500.00),
(1019, 'Queso fresco 500 gramos', 'Producto ficticio para pruebas de integración del ERP.', 3, 'unidad', 12000.00),
(1020, 'Mantequilla sin sal 250 gramos', 'Producto ficticio para pruebas de integración del ERP.', 3, 'unidad', 9500.00),
(1021, 'Kumis 1 litro', 'Producto ficticio para pruebas de integración del ERP.', 3, 'unidad', 7800.00),
(1022, 'Pechuga de pollo', 'Producto ficticio para pruebas de integración del ERP.', 4, 'kg', 16000.00),
(1023, 'Muslo de pollo', 'Producto ficticio para pruebas de integración del ERP.', 4, 'kg', 12500.00),
(1024, 'Carne de res molida', 'Producto ficticio para pruebas de integración del ERP.', 4, 'kg', 23000.00),
(1025, 'Lomo de cerdo', 'Producto ficticio para pruebas de integración del ERP.', 4, 'kg', 21000.00),
(1026, 'Costilla de res', 'Producto ficticio para pruebas de integración del ERP.', 4, 'kg', 19000.00),
(1027, 'Chorizo artesanal', 'Producto ficticio para pruebas de integración del ERP.', 4, 'kg', 18500.00),
(1028, 'Arroz blanco', 'Producto ficticio para pruebas de integración del ERP.', 5, 'kg', 4500.00),
(1029, 'Fríjol rojo', 'Producto ficticio para pruebas de integración del ERP.', 5, 'kg', 9200.00),
(1030, 'Lenteja seca', 'Producto ficticio para pruebas de integración del ERP.', 5, 'kg', 6800.00),
(1031, 'Garbanzo seco', 'Producto ficticio para pruebas de integración del ERP.', 5, 'kg', 7500.00),
(1032, 'Maíz amarillo', 'Producto ficticio para pruebas de integración del ERP.', 5, 'kg', 3800.00),
(1033, 'Avena en hojuelas', 'Producto ficticio para pruebas de integración del ERP.', 5, 'kg', 6200.00),
(1034, 'Quinua', 'Producto ficticio para pruebas de integración del ERP.', 5, 'kg', 14500.00),
(1035, 'Agua sin gas 600 mililitros', 'Producto ficticio para pruebas de integración del ERP.', 6, 'unidad', 1800.00),
(1036, 'Jugo de naranja 1 litro', 'Producto ficticio para pruebas de integración del ERP.', 6, 'unidad', 6500.00),
(1037, 'Jugo de mora 1 litro', 'Producto ficticio para pruebas de integración del ERP.', 6, 'unidad', 6200.00),
(1038, 'Jugo de mango 1 litro', 'Producto ficticio para pruebas de integración del ERP.', 6, 'unidad', 6200.00),
(1039, 'Té helado 500 mililitros', 'Producto ficticio para pruebas de integración del ERP.', 6, 'unidad', 3000.00),
(1040, 'Agua con gas 600 mililitros', 'Producto ficticio para pruebas de integración del ERP.', 6, 'unidad', 2200.00),
(1041, 'Atún en agua 160 gramos', 'Producto ficticio para pruebas de integración del ERP.', 7, 'unidad', 6500.00),
(1042, 'Arveja enlatada 300 gramos', 'Producto ficticio para pruebas de integración del ERP.', 7, 'unidad', 4200.00),
(1043, 'Maíz dulce 300 gramos', 'Producto ficticio para pruebas de integración del ERP.', 7, 'unidad', 4800.00),
(1044, 'Fríjol en conserva 400 gramos', 'Producto ficticio para pruebas de integración del ERP.', 7, 'unidad', 5500.00),
(1045, 'Durazno en almíbar 820 gramos', 'Producto ficticio para pruebas de integración del ERP.', 7, 'unidad', 12500.00),
(1046, 'Pan integral 500 gramos', 'Producto ficticio para pruebas de integración del ERP.', 8, 'unidad', 6800.00),
(1047, 'Pan blanco 500 gramos', 'Producto ficticio para pruebas de integración del ERP.', 8, 'unidad', 5500.00),
(1048, 'Pan multicereal 500 gramos', 'Producto ficticio para pruebas de integración del ERP.', 8, 'unidad', 7500.00),
(1049, 'Arepa de trigo 5 unidades', 'Producto ficticio para pruebas de integración del ERP.', 8, 'unidad', 4200.00),
(1050, 'Tostadas 250 gramos', 'Producto ficticio para pruebas de integración del ERP.', 8, 'unidad', 4500.00);

-- 2. CATÁLOGO PRODUCTO-PROVEEDOR: 50 relaciones con IDs existentes de Oscar.
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES
(1, 1001),
(1, 1002),
(1, 1003),
(1, 1004),
(1, 1005),
(1, 1006),
(1, 1007),
(2, 1008),
(2, 1009),
(2, 1010),
(2, 1011),
(2, 1012),
(2, 1013),
(2, 1014),
(3, 1015),
(3, 1016),
(3, 1017),
(3, 1018),
(3, 1019),
(3, 1020),
(3, 1021),
(4, 1022),
(4, 1023),
(4, 1024),
(4, 1025),
(4, 1026),
(4, 1027),
(5, 1028),
(5, 1029),
(5, 1030),
(5, 1031),
(5, 1032),
(5, 1033),
(5, 1034),
(6, 1035),
(6, 1036),
(6, 1037),
(6, 1038),
(6, 1039),
(6, 1040),
(15, 1041),
(15, 1042),
(15, 1043),
(15, 1044),
(15, 1045),
(8, 1046),
(8, 1047),
(8, 1048),
(8, 1049),
(8, 1050);

-- 3. LOTES: 80 registros, IDs 1001..1080. Cantidad = inicial menos mermas.
INSERT INTO lotes_inventario (id_lote, numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES
(1001, 'LOT-JR-DEMO-1001', 1001, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 101.00, 99.00, 2940.00, 4200.00, 'P-1', 'E-2', 2),
(1002, 'LOT-JR-DEMO-1002', 1002, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 102.00, 99.00, 1960.00, 2800.00, 'P-1', 'E-3', 3),
(1003, 'LOT-JR-DEMO-1003', 1003, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 103.00, 99.00, 2730.00, 3900.00, 'P-1', 'E-4', 1),
(1004, 'LOT-JR-DEMO-1004', 1004, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 104.00, 99.00, 2100.00, 3000.00, 'P-1', 'E-5', 1),
(1005, 'LOT-JR-DEMO-1005', 1005, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 105.00, 104.00, 2240.00, 3200.00, 'P-1', 'E-1', 2),
(1006, 'LOT-JR-DEMO-1006', 1006, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 106.00, 104.00, 2660.00, 3800.00, 'P-1', 'E-2', 3),
(1007, 'LOT-JR-DEMO-1007', 1007, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 107.00, 104.00, 4550.00, 6500.00, 'P-1', 'E-3', 1),
(1008, 'LOT-JR-DEMO-1008', 1008, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 108.00, 104.00, 2450.00, 3500.00, 'P-2', 'E-4', 1),
(1009, 'LOT-JR-DEMO-1009', 1009, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 109.00, 104.00, 1820.00, 2600.00, 'P-2', 'E-5', 2),
(1010, 'LOT-JR-DEMO-1010', 1010, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 110.00, 109.00, 1680.00, 2400.00, 'P-2', 'E-1', 3),
(1011, 'LOT-JR-DEMO-1011', 1011, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 111.00, 109.00, 3360.00, 4800.00, 'P-2', 'E-2', 1),
(1012, 'LOT-JR-DEMO-1012', 1012, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 112.00, 109.00, 2100.00, 3000.00, 'P-2', 'E-3', 1),
(1013, 'LOT-JR-DEMO-1013', 1013, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 113.00, 109.00, 4200.00, 6000.00, 'P-2', 'E-4', 2),
(1014, 'LOT-JR-DEMO-1014', 1014, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 114.00, 109.00, 3640.00, 5200.00, 'P-2', 'E-5', 3),
(1015, 'LOT-JR-DEMO-1015', 1015, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 115.00, 114.00, 2940.00, 4200.00, 'P-3', 'E-1', 1),
(1016, 'LOT-JR-DEMO-1016', 1016, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 116.00, 114.00, 3360.00, 4800.00, 'P-3', 'E-2', 1),
(1017, 'LOT-JR-DEMO-1017', 1017, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 117.00, 114.00, 5950.00, 8500.00, 'P-3', 'E-3', 2),
(1018, 'LOT-JR-DEMO-1018', 1018, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 118.00, 114.00, 7350.00, 10500.00, 'P-3', 'E-4', 3),
(1019, 'LOT-JR-DEMO-1019', 1019, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 119.00, 114.00, 8400.00, 12000.00, 'P-3', 'E-5', 1),
(1020, 'LOT-JR-DEMO-1020', 1020, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 120.00, 119.00, 6650.00, 9500.00, 'P-3', 'E-1', 1),
(1021, 'LOT-JR-DEMO-1021', 1021, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 121.00, 119.00, 5460.00, 7800.00, 'P-3', 'E-2', 2),
(1022, 'LOT-JR-DEMO-1022', 1022, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 122.00, 119.00, 11200.00, 16000.00, 'P-4', 'E-3', 3),
(1023, 'LOT-JR-DEMO-1023', 1023, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 123.00, 119.00, 8750.00, 12500.00, 'P-4', 'E-4', 1),
(1024, 'LOT-JR-DEMO-1024', 1024, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 124.00, 119.00, 16100.00, 23000.00, 'P-4', 'E-5', 1),
(1025, 'LOT-JR-DEMO-1025', 1025, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 125.00, 124.00, 14700.00, 21000.00, 'P-4', 'E-1', 2),
(1026, 'LOT-JR-DEMO-1026', 1026, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 126.00, 124.00, 13300.00, 19000.00, 'P-4', 'E-2', 4),
(1027, 'LOT-JR-DEMO-1027', 1027, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 127.00, 124.00, 12950.00, 18500.00, 'P-4', 'E-3', 4),
(1028, 'LOT-JR-DEMO-1028', 1028, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 128.00, 124.00, 3150.00, 4500.00, 'P-5', 'E-4', 4),
(1029, 'LOT-JR-DEMO-1029', 1029, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 129.00, 124.00, 6440.00, 9200.00, 'P-5', 'E-5', 4),
(1030, 'LOT-JR-DEMO-1030', 1030, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 130.00, 0.00, 4760.00, 6800.00, 'P-5', 'E-1', 4),
(1031, 'LOT-JR-DEMO-1031', 1031, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 131.00, 129.00, 5250.00, 7500.00, 'P-5', 'E-2', 4),
(1032, 'LOT-JR-DEMO-1032', 1032, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 132.00, 129.00, 2660.00, 3800.00, 'P-5', 'E-3', 4),
(1033, 'LOT-JR-DEMO-1033', 1033, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 133.00, 129.00, 4340.00, 6200.00, 'P-5', 'E-4', 4),
(1034, 'LOT-JR-DEMO-1034', 1034, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 134.00, 129.00, 10150.00, 14500.00, 'P-5', 'E-5', 4),
(1035, 'LOT-JR-DEMO-1035', 1035, 6, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 135.00, 134.00, 1260.00, 1800.00, 'P-6', 'E-1', 4),
(1036, 'LOT-JR-DEMO-1036', 1036, 6, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 136.00, 134.00, 4550.00, 6500.00, 'P-6', 'E-2', 4),
(1037, 'LOT-JR-DEMO-1037', 1037, 6, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 137.00, 134.00, 4340.00, 6200.00, 'P-6', 'E-3', 4),
(1038, 'LOT-JR-DEMO-1038', 1038, 6, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 138.00, 134.00, 4340.00, 6200.00, 'P-6', 'E-4', 4),
(1039, 'LOT-JR-DEMO-1039', 1039, 6, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 139.00, 134.00, 2100.00, 3000.00, 'P-6', 'E-5', 4),
(1040, 'LOT-JR-DEMO-1040', 1040, 6, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 140.00, 139.00, 1540.00, 2200.00, 'P-6', 'E-1', 4),
(1041, 'LOT-JR-DEMO-1041', 1041, 15, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 141.00, 139.00, 4550.00, 6500.00, 'P-7', 'E-2', 4),
(1042, 'LOT-JR-DEMO-1042', 1042, 15, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 142.00, 139.00, 2940.00, 4200.00, 'P-7', 'E-3', 4),
(1043, 'LOT-JR-DEMO-1043', 1043, 15, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 143.00, 139.00, 3360.00, 4800.00, 'P-7', 'E-4', 4),
(1044, 'LOT-JR-DEMO-1044', 1044, 15, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 144.00, 139.00, 3850.00, 5500.00, 'P-7', 'E-5', 4),
(1045, 'LOT-JR-DEMO-1045', 1045, 15, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 145.00, 144.00, 8750.00, 12500.00, 'P-7', 'E-1', 4),
(1046, 'LOT-JR-DEMO-1046', 1046, 8, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 146.00, 144.00, 4760.00, 6800.00, 'P-8', 'E-2', 4),
(1047, 'LOT-JR-DEMO-1047', 1047, 8, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 147.00, 144.00, 3850.00, 5500.00, 'P-8', 'E-3', 4),
(1048, 'LOT-JR-DEMO-1048', 1048, 8, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 148.00, 144.00, 5250.00, 7500.00, 'P-8', 'E-4', 4),
(1049, 'LOT-JR-DEMO-1049', 1049, 8, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 149.00, 144.00, 2940.00, 4200.00, 'P-8', 'E-5', 4),
(1050, 'LOT-JR-DEMO-1050', 1050, 8, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 150.00, 149.00, 3150.00, 4500.00, 'P-8', 'E-1', 4),
(1051, 'LOT-JR-DEMO-1051', 1001, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 151.00, 151.00, 2940.00, 4200.00, 'P-1', 'E-2', 1),
(1052, 'LOT-JR-DEMO-1052', 1002, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 152.00, 152.00, 1960.00, 2800.00, 'P-1', 'E-3', 1),
(1053, 'LOT-JR-DEMO-1053', 1003, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 153.00, 153.00, 2730.00, 3900.00, 'P-1', 'E-4', 2),
(1054, 'LOT-JR-DEMO-1054', 1004, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 154.00, 154.00, 2100.00, 3000.00, 'P-1', 'E-5', 3),
(1055, 'LOT-JR-DEMO-1055', 1005, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 155.00, 155.00, 2240.00, 3200.00, 'P-1', 'E-1', 1),
(1056, 'LOT-JR-DEMO-1056', 1006, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 156.00, 156.00, 2660.00, 3800.00, 'P-1', 'E-2', 1),
(1057, 'LOT-JR-DEMO-1057', 1007, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 157.00, 157.00, 4550.00, 6500.00, 'P-1', 'E-3', 2),
(1058, 'LOT-JR-DEMO-1058', 1008, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 158.00, 158.00, 2450.00, 3500.00, 'P-2', 'E-4', 3),
(1059, 'LOT-JR-DEMO-1059', 1009, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 159.00, 159.00, 1820.00, 2600.00, 'P-2', 'E-5', 1),
(1060, 'LOT-JR-DEMO-1060', 1010, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 160.00, 160.00, 1680.00, 2400.00, 'P-2', 'E-1', 1),
(1061, 'LOT-JR-DEMO-1061', 1011, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 161.00, 161.00, 3360.00, 4800.00, 'P-2', 'E-2', 2),
(1062, 'LOT-JR-DEMO-1062', 1012, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 162.00, 162.00, 2100.00, 3000.00, 'P-2', 'E-3', 3),
(1063, 'LOT-JR-DEMO-1063', 1013, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 163.00, 163.00, 4200.00, 6000.00, 'P-2', 'E-4', 1),
(1064, 'LOT-JR-DEMO-1064', 1014, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 164.00, 164.00, 3640.00, 5200.00, 'P-2', 'E-5', 1),
(1065, 'LOT-JR-DEMO-1065', 1015, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 165.00, 165.00, 2940.00, 4200.00, 'P-3', 'E-1', 2),
(1066, 'LOT-JR-DEMO-1066', 1016, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 166.00, 166.00, 3360.00, 4800.00, 'P-3', 'E-2', 3),
(1067, 'LOT-JR-DEMO-1067', 1017, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 167.00, 167.00, 5950.00, 8500.00, 'P-3', 'E-3', 1),
(1068, 'LOT-JR-DEMO-1068', 1018, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 168.00, 168.00, 7350.00, 10500.00, 'P-3', 'E-4', 1),
(1069, 'LOT-JR-DEMO-1069', 1019, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 169.00, 169.00, 8400.00, 12000.00, 'P-3', 'E-5', 2),
(1070, 'LOT-JR-DEMO-1070', 1020, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 170.00, 170.00, 6650.00, 9500.00, 'P-3', 'E-1', 3),
(1071, 'LOT-JR-DEMO-1071', 1021, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 171.00, 171.00, 5460.00, 7800.00, 'P-3', 'E-2', 1),
(1072, 'LOT-JR-DEMO-1072', 1022, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 172.00, 172.00, 11200.00, 16000.00, 'P-4', 'E-3', 1),
(1073, 'LOT-JR-DEMO-1073', 1023, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 173.00, 173.00, 8750.00, 12500.00, 'P-4', 'E-4', 2),
(1074, 'LOT-JR-DEMO-1074', 1024, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 174.00, 174.00, 16100.00, 23000.00, 'P-4', 'E-5', 3),
(1075, 'LOT-JR-DEMO-1075', 1025, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 175.00, 175.00, 14700.00, 21000.00, 'P-4', 'E-1', 1),
(1076, 'LOT-JR-DEMO-1076', 1026, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 176.00, 176.00, 13300.00, 19000.00, 'P-4', 'E-2', 1),
(1077, 'LOT-JR-DEMO-1077', 1027, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 177.00, 177.00, 12950.00, 18500.00, 'P-4', 'E-3', 2),
(1078, 'LOT-JR-DEMO-1078', 1028, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 178.00, 178.00, 3150.00, 4500.00, 'P-5', 'E-4', 3),
(1079, 'LOT-JR-DEMO-1079', 1029, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 179.00, 179.00, 6440.00, 9200.00, 'P-5', 'E-5', 1),
(1080, 'LOT-JR-DEMO-1080', 1030, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 180.00, 180.00, 4760.00, 6800.00, 'P-5', 'E-1', 1);

-- 4. CUARENTENAS: 50 registros, IDs 1001..1050. Las primeras 25 están resueltas.
INSERT INTO registro_cuarentena (id_registro, id_lote, motivo, usuario, fecha_registro, resuelta, fecha_resolucion, notas_resolucion) VALUES
(1001, 1001, 'Empaque deteriorado', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1051: Inspector de prueba'),
(1002, 1002, 'Temperatura fuera de rango', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1052: Inspector de prueba'),
(1003, 1003, 'Golpes durante la recepción', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1053: Inspector de prueba'),
(1004, 1004, 'Etiqueta de conservación ilegible', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1054: Inspector de prueba'),
(1005, 1005, 'Humedad detectada en el empaque', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1055: Inspector de prueba'),
(1006, 1006, 'Empaque deteriorado', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1056: Inspector de prueba'),
(1007, 1007, 'Temperatura fuera de rango', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1057: Inspector de prueba'),
(1008, 1008, 'Golpes durante la recepción', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1058: Inspector de prueba'),
(1009, 1009, 'Etiqueta de conservación ilegible', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1059: Inspector de prueba'),
(1010, 1010, 'Humedad detectada en el empaque', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1060: Inspector de prueba'),
(1011, 1011, 'Empaque deteriorado', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1061: Inspector de prueba'),
(1012, 1012, 'Temperatura fuera de rango', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1062: Inspector de prueba'),
(1013, 1013, 'Golpes durante la recepción', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1063: Inspector de prueba'),
(1014, 1014, 'Etiqueta de conservación ilegible', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1064: Inspector de prueba'),
(1015, 1015, 'Humedad detectada en el empaque', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1065: Inspector de prueba'),
(1016, 1016, 'Empaque deteriorado', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1066: Inspector de prueba'),
(1017, 1017, 'Temperatura fuera de rango', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1067: Inspector de prueba'),
(1018, 1018, 'Golpes durante la recepción', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1068: Inspector de prueba'),
(1019, 1019, 'Etiqueta de conservación ilegible', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1069: Inspector de prueba'),
(1020, 1020, 'Humedad detectada en el empaque', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1070: Inspector de prueba'),
(1021, 1021, 'Empaque deteriorado', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1071: Inspector de prueba'),
(1022, 1022, 'Temperatura fuera de rango', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1072: Inspector de prueba'),
(1023, 1023, 'Golpes durante la recepción', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1073: Inspector de prueba'),
(1024, 1024, 'Etiqueta de conservación ilegible', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1074: Inspector de prueba'),
(1025, 1025, 'Humedad detectada en el empaque', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Inspección aprobada #1075: Inspector de prueba'),
(1026, 1026, 'Empaque deteriorado', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1027, 1027, 'Temperatura fuera de rango', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1028, 1028, 'Golpes durante la recepción', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1029, 1029, 'Etiqueta de conservación ilegible', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1030, 1030, 'Humedad detectada en el empaque', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1031, 1031, 'Empaque deteriorado', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1032, 1032, 'Temperatura fuera de rango', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1033, 1033, 'Golpes durante la recepción', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1034, 1034, 'Etiqueta de conservación ilegible', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1035, 1035, 'Humedad detectada en el empaque', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1036, 1036, 'Empaque deteriorado', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1037, 1037, 'Temperatura fuera de rango', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1038, 1038, 'Golpes durante la recepción', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1039, 1039, 'Etiqueta de conservación ilegible', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1040, 1040, 'Humedad detectada en el empaque', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1041, 1041, 'Empaque deteriorado', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1042, 1042, 'Temperatura fuera de rango', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1043, 1043, 'Golpes durante la recepción', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1044, 1044, 'Etiqueta de conservación ilegible', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1045, 1045, 'Humedad detectada en el empaque', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1046, 1046, 'Empaque deteriorado', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1047, 1047, 'Temperatura fuera de rango', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1048, 1048, 'Golpes durante la recepción', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1049, 1049, 'Etiqueta de conservación ilegible', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL),
(1050, 1050, 'Humedad detectada en el empaque', 'Junior - datos de prueba', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), FALSE, NULL, NULL);

-- 5. INSPECCIONES: 50 rechazos (1001..1050) y 25 aprobaciones posteriores (1051..1075).
INSERT INTO inspecciones_calidad (id_inspeccion, id_lote, fecha_inspeccion, resultado, nombre_inspector, motivo_rechazo, notas) VALUES
(1001, 1001, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Empaque deteriorado', 'Pérdida parcial; revisar las unidades restantes.'),
(1002, 1002, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Temperatura fuera de rango', 'Pérdida parcial; revisar las unidades restantes.'),
(1003, 1003, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Golpes durante la recepción', 'Pérdida parcial; revisar las unidades restantes.'),
(1004, 1004, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Etiqueta de conservación ilegible', 'Pérdida parcial; revisar las unidades restantes.'),
(1005, 1005, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Humedad detectada en el empaque', 'Pérdida parcial; revisar las unidades restantes.'),
(1006, 1006, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Empaque deteriorado', 'Pérdida parcial; revisar las unidades restantes.'),
(1007, 1007, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Temperatura fuera de rango', 'Pérdida parcial; revisar las unidades restantes.'),
(1008, 1008, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Golpes durante la recepción', 'Pérdida parcial; revisar las unidades restantes.'),
(1009, 1009, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Etiqueta de conservación ilegible', 'Pérdida parcial; revisar las unidades restantes.'),
(1010, 1010, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Humedad detectada en el empaque', 'Pérdida parcial; revisar las unidades restantes.'),
(1011, 1011, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Empaque deteriorado', 'Pérdida parcial; revisar las unidades restantes.'),
(1012, 1012, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Temperatura fuera de rango', 'Pérdida parcial; revisar las unidades restantes.'),
(1013, 1013, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Golpes durante la recepción', 'Pérdida parcial; revisar las unidades restantes.'),
(1014, 1014, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Etiqueta de conservación ilegible', 'Pérdida parcial; revisar las unidades restantes.'),
(1015, 1015, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Humedad detectada en el empaque', 'Pérdida parcial; revisar las unidades restantes.'),
(1016, 1016, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Empaque deteriorado', 'Pérdida parcial; revisar las unidades restantes.'),
(1017, 1017, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Temperatura fuera de rango', 'Pérdida parcial; revisar las unidades restantes.'),
(1018, 1018, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Golpes durante la recepción', 'Pérdida parcial; revisar las unidades restantes.'),
(1019, 1019, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Etiqueta de conservación ilegible', 'Pérdida parcial; revisar las unidades restantes.'),
(1020, 1020, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Humedad detectada en el empaque', 'Pérdida parcial; revisar las unidades restantes.'),
(1021, 1021, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Empaque deteriorado', 'Pérdida parcial; revisar las unidades restantes.'),
(1022, 1022, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Temperatura fuera de rango', 'Pérdida parcial; revisar las unidades restantes.'),
(1023, 1023, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Golpes durante la recepción', 'Pérdida parcial; revisar las unidades restantes.'),
(1024, 1024, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Etiqueta de conservación ilegible', 'Pérdida parcial; revisar las unidades restantes.'),
(1025, 1025, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Humedad detectada en el empaque', 'Pérdida parcial; revisar las unidades restantes.'),
(1026, 1026, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Empaque deteriorado', 'Pérdida parcial; revisar las unidades restantes.'),
(1027, 1027, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Temperatura fuera de rango', 'Pérdida parcial; revisar las unidades restantes.'),
(1028, 1028, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Golpes durante la recepción', 'Pérdida parcial; revisar las unidades restantes.'),
(1029, 1029, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Etiqueta de conservación ilegible', 'Pérdida parcial; revisar las unidades restantes.'),
(1030, 1030, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Humedad detectada en el empaque', 'Pérdida total; mantener el lote bloqueado.'),
(1031, 1031, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Empaque deteriorado', 'Pérdida parcial; revisar las unidades restantes.'),
(1032, 1032, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Temperatura fuera de rango', 'Pérdida parcial; revisar las unidades restantes.'),
(1033, 1033, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Golpes durante la recepción', 'Pérdida parcial; revisar las unidades restantes.'),
(1034, 1034, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Etiqueta de conservación ilegible', 'Pérdida parcial; revisar las unidades restantes.'),
(1035, 1035, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Humedad detectada en el empaque', 'Pérdida parcial; revisar las unidades restantes.'),
(1036, 1036, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Empaque deteriorado', 'Pérdida parcial; revisar las unidades restantes.'),
(1037, 1037, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Temperatura fuera de rango', 'Pérdida parcial; revisar las unidades restantes.'),
(1038, 1038, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Golpes durante la recepción', 'Pérdida parcial; revisar las unidades restantes.'),
(1039, 1039, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Etiqueta de conservación ilegible', 'Pérdida parcial; revisar las unidades restantes.'),
(1040, 1040, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Humedad detectada en el empaque', 'Pérdida parcial; revisar las unidades restantes.'),
(1041, 1041, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Empaque deteriorado', 'Pérdida parcial; revisar las unidades restantes.'),
(1042, 1042, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Temperatura fuera de rango', 'Pérdida parcial; revisar las unidades restantes.'),
(1043, 1043, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Golpes durante la recepción', 'Pérdida parcial; revisar las unidades restantes.'),
(1044, 1044, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Etiqueta de conservación ilegible', 'Pérdida parcial; revisar las unidades restantes.'),
(1045, 1045, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Humedad detectada en el empaque', 'Pérdida parcial; revisar las unidades restantes.'),
(1046, 1046, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Empaque deteriorado', 'Pérdida parcial; revisar las unidades restantes.'),
(1047, 1047, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Temperatura fuera de rango', 'Pérdida parcial; revisar las unidades restantes.'),
(1048, 1048, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Golpes durante la recepción', 'Pérdida parcial; revisar las unidades restantes.'),
(1049, 1049, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Etiqueta de conservación ilegible', 'Pérdida parcial; revisar las unidades restantes.'),
(1050, 1050, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Rechazado', 'Inspector de prueba', 'Humedad detectada en el empaque', 'Pérdida parcial; revisar las unidades restantes.'),
(1051, 1001, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1052, 1002, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1053, 1003, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1054, 1004, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1055, 1005, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1056, 1006, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1057, 1007, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1058, 1008, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1059, 1009, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1060, 1010, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1061, 1011, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1062, 1012, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1063, 1013, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1064, 1014, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1065, 1015, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1066, 1016, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1067, 1017, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1068, 1018, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1069, 1019, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1070, 1020, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1071, 1021, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1072, 1022, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1073, 1023, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1074, 1024, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.'),
(1075, 1025, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Aprobado', 'Inspector de prueba', NULL, 'Unidades restantes revisadas después de registrar la merma; aplicar estado por vencimiento.');

-- 6. MERMAS: 50 registros, IDs 1001..1050. Una baja por rechazo, sin duplicados.
INSERT INTO registro_mermas (id_registro, id_lote, id_inspeccion, cantidad_perdida, motivo, fecha_registro, usuario_registro) VALUES
(1001, 1001, 1001, 2.00, 'Empaque deteriorado', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1002, 1002, 1002, 3.00, 'Temperatura fuera de rango', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1003, 1003, 1003, 4.00, 'Golpes durante la recepción', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1004, 1004, 1004, 5.00, 'Etiqueta de conservación ilegible', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1005, 1005, 1005, 1.00, 'Humedad detectada en el empaque', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1006, 1006, 1006, 2.00, 'Empaque deteriorado', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1007, 1007, 1007, 3.00, 'Temperatura fuera de rango', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1008, 1008, 1008, 4.00, 'Golpes durante la recepción', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1009, 1009, 1009, 5.00, 'Etiqueta de conservación ilegible', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1010, 1010, 1010, 1.00, 'Humedad detectada en el empaque', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1011, 1011, 1011, 2.00, 'Empaque deteriorado', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1012, 1012, 1012, 3.00, 'Temperatura fuera de rango', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1013, 1013, 1013, 4.00, 'Golpes durante la recepción', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1014, 1014, 1014, 5.00, 'Etiqueta de conservación ilegible', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1015, 1015, 1015, 1.00, 'Humedad detectada en el empaque', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1016, 1016, 1016, 2.00, 'Empaque deteriorado', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1017, 1017, 1017, 3.00, 'Temperatura fuera de rango', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1018, 1018, 1018, 4.00, 'Golpes durante la recepción', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1019, 1019, 1019, 5.00, 'Etiqueta de conservación ilegible', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1020, 1020, 1020, 1.00, 'Humedad detectada en el empaque', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1021, 1021, 1021, 2.00, 'Empaque deteriorado', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1022, 1022, 1022, 3.00, 'Temperatura fuera de rango', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1023, 1023, 1023, 4.00, 'Golpes durante la recepción', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1024, 1024, 1024, 5.00, 'Etiqueta de conservación ilegible', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1025, 1025, 1025, 1.00, 'Humedad detectada en el empaque', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1026, 1026, 1026, 2.00, 'Empaque deteriorado', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1027, 1027, 1027, 3.00, 'Temperatura fuera de rango', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1028, 1028, 1028, 4.00, 'Golpes durante la recepción', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1029, 1029, 1029, 5.00, 'Etiqueta de conservación ilegible', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1030, 1030, 1030, 130.00, 'Humedad detectada en el empaque', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1031, 1031, 1031, 2.00, 'Empaque deteriorado', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1032, 1032, 1032, 3.00, 'Temperatura fuera de rango', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1033, 1033, 1033, 4.00, 'Golpes durante la recepción', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1034, 1034, 1034, 5.00, 'Etiqueta de conservación ilegible', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1035, 1035, 1035, 1.00, 'Humedad detectada en el empaque', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1036, 1036, 1036, 2.00, 'Empaque deteriorado', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1037, 1037, 1037, 3.00, 'Temperatura fuera de rango', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1038, 1038, 1038, 4.00, 'Golpes durante la recepción', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1039, 1039, 1039, 5.00, 'Etiqueta de conservación ilegible', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1040, 1040, 1040, 1.00, 'Humedad detectada en el empaque', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1041, 1041, 1041, 2.00, 'Empaque deteriorado', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1042, 1042, 1042, 3.00, 'Temperatura fuera de rango', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1043, 1043, 1043, 4.00, 'Golpes durante la recepción', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1044, 1044, 1044, 5.00, 'Etiqueta de conservación ilegible', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1045, 1045, 1045, 1.00, 'Humedad detectada en el empaque', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1046, 1046, 1046, 2.00, 'Empaque deteriorado', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1047, 1047, 1047, 3.00, 'Temperatura fuera de rango', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1048, 1048, 1048, 4.00, 'Golpes durante la recepción', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1049, 1049, 1049, 5.00, 'Etiqueta de conservación ilegible', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba'),
(1050, 1050, 1050, 1.00, 'Humedad detectada en el empaque', DATE_ADD(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 1 HOUR), 'Junior - datos de prueba');

-- 7. CLAVES DE MERMA: relacionan cada baja con su clave única para los reintentos.
INSERT INTO operaciones_merma (clave_operacion, id_registro) VALUES
('SEED-JUNIOR-MERMA-1001', 1001),
('SEED-JUNIOR-MERMA-1002', 1002),
('SEED-JUNIOR-MERMA-1003', 1003),
('SEED-JUNIOR-MERMA-1004', 1004),
('SEED-JUNIOR-MERMA-1005', 1005),
('SEED-JUNIOR-MERMA-1006', 1006),
('SEED-JUNIOR-MERMA-1007', 1007),
('SEED-JUNIOR-MERMA-1008', 1008),
('SEED-JUNIOR-MERMA-1009', 1009),
('SEED-JUNIOR-MERMA-1010', 1010),
('SEED-JUNIOR-MERMA-1011', 1011),
('SEED-JUNIOR-MERMA-1012', 1012),
('SEED-JUNIOR-MERMA-1013', 1013),
('SEED-JUNIOR-MERMA-1014', 1014),
('SEED-JUNIOR-MERMA-1015', 1015),
('SEED-JUNIOR-MERMA-1016', 1016),
('SEED-JUNIOR-MERMA-1017', 1017),
('SEED-JUNIOR-MERMA-1018', 1018),
('SEED-JUNIOR-MERMA-1019', 1019),
('SEED-JUNIOR-MERMA-1020', 1020),
('SEED-JUNIOR-MERMA-1021', 1021),
('SEED-JUNIOR-MERMA-1022', 1022),
('SEED-JUNIOR-MERMA-1023', 1023),
('SEED-JUNIOR-MERMA-1024', 1024),
('SEED-JUNIOR-MERMA-1025', 1025),
('SEED-JUNIOR-MERMA-1026', 1026),
('SEED-JUNIOR-MERMA-1027', 1027),
('SEED-JUNIOR-MERMA-1028', 1028),
('SEED-JUNIOR-MERMA-1029', 1029),
('SEED-JUNIOR-MERMA-1030', 1030),
('SEED-JUNIOR-MERMA-1031', 1031),
('SEED-JUNIOR-MERMA-1032', 1032),
('SEED-JUNIOR-MERMA-1033', 1033),
('SEED-JUNIOR-MERMA-1034', 1034),
('SEED-JUNIOR-MERMA-1035', 1035),
('SEED-JUNIOR-MERMA-1036', 1036),
('SEED-JUNIOR-MERMA-1037', 1037),
('SEED-JUNIOR-MERMA-1038', 1038),
('SEED-JUNIOR-MERMA-1039', 1039),
('SEED-JUNIOR-MERMA-1040', 1040),
('SEED-JUNIOR-MERMA-1041', 1041),
('SEED-JUNIOR-MERMA-1042', 1042),
('SEED-JUNIOR-MERMA-1043', 1043),
('SEED-JUNIOR-MERMA-1044', 1044),
('SEED-JUNIOR-MERMA-1045', 1045),
('SEED-JUNIOR-MERMA-1046', 1046),
('SEED-JUNIOR-MERMA-1047', 1047),
('SEED-JUNIOR-MERMA-1048', 1048),
('SEED-JUNIOR-MERMA-1049', 1049),
('SEED-JUNIOR-MERMA-1050', 1050);

-- ===== dml_sales.sql =====
-- Sales and invoice demo data: 50 sales, 50 details, and 50 invoices.
-- Load once, after database.sql, Oscar supplier/logistics data, dml_customers.sql, and Junior dml_inventory.sql.
-- Reserves sale IDs 2001-2050 and requires free IDs in that range. Junior lots 1001-1080 are required.
-- Each detail sells one unit; lots used more than once retain positive stock. All values are fictitious.
START TRANSACTION;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2001, 'SALE-DEMO-2001', c.id_cliente, 4200.00, 2100.00, 399.00, 2499.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '574277948' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2001, 1001, 1.00, 4200.00, 0.50, 2100.00, 0.19, 399.00, 4200.00, 2499.00 FROM ventas WHERE id_venta = 2001;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2001', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2001', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2001', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2001;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2002, 'SALE-DEMO-2002', c.id_cliente, 3900.00, 0.00, 741.00, 4641.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '863843210-9' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2002, 1003, 1.00, 3900.00, 0.00, 0.00, 0.19, 741.00, 3900.00, 4641.00 FROM ventas WHERE id_venta = 2002;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2002', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2002', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2002', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2002;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2003, 'SALE-DEMO-2003', c.id_cliente, 3000.00, 0.00, 570.00, 3570.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '561542159' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2003, 1004, 1.00, 3000.00, 0.00, 0.00, 0.19, 570.00, 3000.00, 3570.00 FROM ventas WHERE id_venta = 2003;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2003', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2003', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2003', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2003;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2004, 'SALE-DEMO-2004', c.id_cliente, 3200.00, 1600.00, 304.00, 1904.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '860785487-1' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2004, 1005, 1.00, 3200.00, 0.50, 1600.00, 0.19, 304.00, 3200.00, 1904.00 FROM ventas WHERE id_venta = 2004;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2004', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2004', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2004', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2004;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2005, 'SALE-DEMO-2005', c.id_cliente, 6500.00, 0.00, 1235.00, 7735.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '672881578' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2005, 1007, 1.00, 6500.00, 0.00, 0.00, 0.19, 1235.00, 6500.00, 7735.00 FROM ventas WHERE id_venta = 2005;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2005', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2005', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2005', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2005;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2006, 'SALE-DEMO-2006', c.id_cliente, 3500.00, 0.00, 665.00, 4165.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '856746745-6' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2006, 1008, 1.00, 3500.00, 0.00, 0.00, 0.19, 665.00, 3500.00, 4165.00 FROM ventas WHERE id_venta = 2006;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2006', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2006', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2006', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2006;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2007, 'SALE-DEMO-2007', c.id_cliente, 2600.00, 1300.00, 247.00, 1547.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '151174374' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2007, 1009, 1.00, 2600.00, 0.50, 1300.00, 0.19, 247.00, 2600.00, 1547.00 FROM ventas WHERE id_venta = 2007;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2007', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2007', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2007', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2007;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2008, 'SALE-DEMO-2008', c.id_cliente, 4800.00, 0.00, 912.00, 5712.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '808037816-4' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2008, 1011, 1.00, 4800.00, 0.00, 0.00, 0.19, 912.00, 4800.00, 5712.00 FROM ventas WHERE id_venta = 2008;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2008', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2008', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2008', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2008;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2009, 'SALE-DEMO-2009', c.id_cliente, 3000.00, 0.00, 570.00, 3570.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '716868580' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2009, 1012, 1.00, 3000.00, 0.00, 0.00, 0.19, 570.00, 3000.00, 3570.00 FROM ventas WHERE id_venta = 2009;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2009', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2009', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2009', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2009;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2010, 'SALE-DEMO-2010', c.id_cliente, 6000.00, 3000.00, 570.00, 3570.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '803555340-4' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2010, 1013, 1.00, 6000.00, 0.50, 3000.00, 0.19, 570.00, 6000.00, 3570.00 FROM ventas WHERE id_venta = 2010;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2010', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2010', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2010', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2010;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2011, 'SALE-DEMO-2011', c.id_cliente, 4200.00, 0.00, 798.00, 4998.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '501029319' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2011, 1015, 1.00, 4200.00, 0.00, 0.00, 0.19, 798.00, 4200.00, 4998.00 FROM ventas WHERE id_venta = 2011;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2011', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2011', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2011', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2011;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2012, 'SALE-DEMO-2012', c.id_cliente, 4800.00, 0.00, 912.00, 5712.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '817771938-4' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2012, 1016, 1.00, 4800.00, 0.00, 0.00, 0.19, 912.00, 4800.00, 5712.00 FROM ventas WHERE id_venta = 2012;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2012', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2012', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2012', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2012;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2013, 'SALE-DEMO-2013', c.id_cliente, 8500.00, 4250.00, 807.50, 5057.50, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '650165007' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2013, 1017, 1.00, 8500.00, 0.50, 4250.00, 0.19, 807.50, 8500.00, 5057.50 FROM ventas WHERE id_venta = 2013;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2013', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2013', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2013', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2013;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2014, 'SALE-DEMO-2014', c.id_cliente, 12000.00, 0.00, 2280.00, 14280.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '866017712-2' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2014, 1019, 1.00, 12000.00, 0.00, 0.00, 0.19, 2280.00, 12000.00, 14280.00 FROM ventas WHERE id_venta = 2014;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2014', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2014', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2014', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2014;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2015, 'SALE-DEMO-2015', c.id_cliente, 9500.00, 0.00, 1805.00, 11305.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '206014274' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2015, 1020, 1.00, 9500.00, 0.00, 0.00, 0.19, 1805.00, 9500.00, 11305.00 FROM ventas WHERE id_venta = 2015;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2015', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2015', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2015', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2015;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2016, 'SALE-DEMO-2016', c.id_cliente, 7800.00, 3900.00, 741.00, 4641.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '831471020-2' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2016, 1021, 1.00, 7800.00, 0.50, 3900.00, 0.19, 741.00, 7800.00, 4641.00 FROM ventas WHERE id_venta = 2016;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2016', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2016', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2016', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2016;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2017, 'SALE-DEMO-2017', c.id_cliente, 12500.00, 0.00, 2375.00, 14875.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '359870312' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2017, 1023, 1.00, 12500.00, 0.00, 0.00, 0.19, 2375.00, 12500.00, 14875.00 FROM ventas WHERE id_venta = 2017;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2017', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2017', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2017', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2017;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2018, 'SALE-DEMO-2018', c.id_cliente, 23000.00, 0.00, 4370.00, 27370.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '807026996-0' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2018, 1024, 1.00, 23000.00, 0.00, 0.00, 0.19, 4370.00, 23000.00, 27370.00 FROM ventas WHERE id_venta = 2018;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2018', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2018', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2018', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2018;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2019, 'SALE-DEMO-2019', c.id_cliente, 21000.00, 10500.00, 1995.00, 12495.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '930021404' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2019, 1025, 1.00, 21000.00, 0.50, 10500.00, 0.19, 1995.00, 21000.00, 12495.00 FROM ventas WHERE id_venta = 2019;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2019', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2019', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2019', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2019;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2020, 'SALE-DEMO-2020', c.id_cliente, 4200.00, 0.00, 798.00, 4998.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '870689765-1' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2020, 1051, 1.00, 4200.00, 0.00, 0.00, 0.19, 798.00, 4200.00, 4998.00 FROM ventas WHERE id_venta = 2020;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2020', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2020', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2020', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2020;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2021, 'SALE-DEMO-2021', c.id_cliente, 2800.00, 0.00, 532.00, 3332.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '63469851' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2021, 1052, 1.00, 2800.00, 0.00, 0.00, 0.19, 532.00, 2800.00, 3332.00 FROM ventas WHERE id_venta = 2021;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2021', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2021', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2021', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2021;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2022, 'SALE-DEMO-2022', c.id_cliente, 3900.00, 1950.00, 370.50, 2320.50, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '832157218-1' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2022, 1053, 1.00, 3900.00, 0.50, 1950.00, 0.19, 370.50, 3900.00, 2320.50 FROM ventas WHERE id_venta = 2022;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2022', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2022', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2022', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2022;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2023, 'SALE-DEMO-2023', c.id_cliente, 3200.00, 0.00, 608.00, 3808.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '804844731' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2023, 1055, 1.00, 3200.00, 0.00, 0.00, 0.19, 608.00, 3200.00, 3808.00 FROM ventas WHERE id_venta = 2023;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2023', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2023', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2023', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2023;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2024, 'SALE-DEMO-2024', c.id_cliente, 3800.00, 0.00, 722.00, 4522.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '808279085-5' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2024, 1056, 1.00, 3800.00, 0.00, 0.00, 0.19, 722.00, 3800.00, 4522.00 FROM ventas WHERE id_venta = 2024;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2024', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2024', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2024', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2024;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2025, 'SALE-DEMO-2025', c.id_cliente, 6500.00, 3250.00, 617.50, 3867.50, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '423142974' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2025, 1057, 1.00, 6500.00, 0.50, 3250.00, 0.19, 617.50, 6500.00, 3867.50 FROM ventas WHERE id_venta = 2025;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2025', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2025', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2025', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2025;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2026, 'SALE-DEMO-2026', c.id_cliente, 2600.00, 0.00, 494.00, 3094.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '839162011-1' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2026, 1059, 1.00, 2600.00, 0.00, 0.00, 0.19, 494.00, 2600.00, 3094.00 FROM ventas WHERE id_venta = 2026;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2026', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2026', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2026', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2026;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2027, 'SALE-DEMO-2027', c.id_cliente, 2400.00, 0.00, 456.00, 2856.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '969118283' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2027, 1060, 1.00, 2400.00, 0.00, 0.00, 0.19, 456.00, 2400.00, 2856.00 FROM ventas WHERE id_venta = 2027;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2027', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2027', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2027', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2027;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2028, 'SALE-DEMO-2028', c.id_cliente, 4800.00, 2400.00, 456.00, 2856.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '848774094-1' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2028, 1061, 1.00, 4800.00, 0.50, 2400.00, 0.19, 456.00, 4800.00, 2856.00 FROM ventas WHERE id_venta = 2028;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2028', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2028', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2028', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2028;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2029, 'SALE-DEMO-2029', c.id_cliente, 6000.00, 0.00, 1140.00, 7140.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '440782322' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2029, 1063, 1.00, 6000.00, 0.00, 0.00, 0.19, 1140.00, 6000.00, 7140.00 FROM ventas WHERE id_venta = 2029;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2029', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2029', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2029', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2029;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2030, 'SALE-DEMO-2030', c.id_cliente, 5200.00, 0.00, 988.00, 6188.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '878805350-4' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2030, 1064, 1.00, 5200.00, 0.00, 0.00, 0.19, 988.00, 5200.00, 6188.00 FROM ventas WHERE id_venta = 2030;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2030', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2030', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2030', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2030;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2031, 'SALE-DEMO-2031', c.id_cliente, 4200.00, 2100.00, 399.00, 2499.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '268251082' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2031, 1065, 1.00, 4200.00, 0.50, 2100.00, 0.19, 399.00, 4200.00, 2499.00 FROM ventas WHERE id_venta = 2031;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2031', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2031', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2031', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2031;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2032, 'SALE-DEMO-2032', c.id_cliente, 8500.00, 0.00, 1615.00, 10115.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '859988598-9' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2032, 1067, 1.00, 8500.00, 0.00, 0.00, 0.19, 1615.00, 8500.00, 10115.00 FROM ventas WHERE id_venta = 2032;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2032', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2032', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2032', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2032;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2033, 'SALE-DEMO-2033', c.id_cliente, 10500.00, 0.00, 1995.00, 12495.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '460054937' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2033, 1068, 1.00, 10500.00, 0.00, 0.00, 0.19, 1995.00, 10500.00, 12495.00 FROM ventas WHERE id_venta = 2033;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2033', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2033', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2033', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2033;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2034, 'SALE-DEMO-2034', c.id_cliente, 12000.00, 6000.00, 1140.00, 7140.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '842243907-2' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2034, 1069, 1.00, 12000.00, 0.50, 6000.00, 0.19, 1140.00, 12000.00, 7140.00 FROM ventas WHERE id_venta = 2034;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2034', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2034', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2034', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2034;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2035, 'SALE-DEMO-2035', c.id_cliente, 7800.00, 0.00, 1482.00, 9282.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '1077636380' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2035, 1071, 1.00, 7800.00, 0.00, 0.00, 0.19, 1482.00, 7800.00, 9282.00 FROM ventas WHERE id_venta = 2035;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2035', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2035', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2035', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2035;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2036, 'SALE-DEMO-2036', c.id_cliente, 16000.00, 0.00, 3040.00, 19040.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '880318099-8' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2036, 1072, 1.00, 16000.00, 0.00, 0.00, 0.19, 3040.00, 16000.00, 19040.00 FROM ventas WHERE id_venta = 2036;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2036', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2036', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2036', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2036;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2037, 'SALE-DEMO-2037', c.id_cliente, 12500.00, 6250.00, 1187.50, 7437.50, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '638943056' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2037, 1073, 1.00, 12500.00, 0.50, 6250.00, 0.19, 1187.50, 12500.00, 7437.50 FROM ventas WHERE id_venta = 2037;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2037', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2037', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2037', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2037;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2038, 'SALE-DEMO-2038', c.id_cliente, 21000.00, 0.00, 3990.00, 24990.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '819523243-9' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2038, 1075, 1.00, 21000.00, 0.00, 0.00, 0.19, 3990.00, 21000.00, 24990.00 FROM ventas WHERE id_venta = 2038;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2038', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2038', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2038', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2038;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2039, 'SALE-DEMO-2039', c.id_cliente, 19000.00, 0.00, 3610.00, 22610.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '1013213919' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2039, 1076, 1.00, 19000.00, 0.00, 0.00, 0.19, 3610.00, 19000.00, 22610.00 FROM ventas WHERE id_venta = 2039;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2039', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2039', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2039', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2039;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2040, 'SALE-DEMO-2040', c.id_cliente, 18500.00, 9250.00, 1757.50, 11007.50, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '885798514-1' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2040, 1077, 1.00, 18500.00, 0.50, 9250.00, 0.19, 1757.50, 18500.00, 11007.50 FROM ventas WHERE id_venta = 2040;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2040', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2040', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2040', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2040;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2041, 'SALE-DEMO-2041', c.id_cliente, 9200.00, 0.00, 1748.00, 10948.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '430968422' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2041, 1079, 1.00, 9200.00, 0.00, 0.00, 0.19, 1748.00, 9200.00, 10948.00 FROM ventas WHERE id_venta = 2041;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2041', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2041', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2041', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2041;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2042, 'SALE-DEMO-2042', c.id_cliente, 6800.00, 0.00, 1292.00, 8092.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '862754688-1' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2042, 1080, 1.00, 6800.00, 0.00, 0.00, 0.19, 1292.00, 6800.00, 8092.00 FROM ventas WHERE id_venta = 2042;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2042', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2042', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2042', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2042;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2043, 'SALE-DEMO-2043', c.id_cliente, 4200.00, 2100.00, 399.00, 2499.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '137752039' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2043, 1001, 1.00, 4200.00, 0.50, 2100.00, 0.19, 399.00, 4200.00, 2499.00 FROM ventas WHERE id_venta = 2043;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2043', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2043', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2043', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2043;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2044, 'SALE-DEMO-2044', c.id_cliente, 3900.00, 0.00, 741.00, 4641.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '816722647-3' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2044, 1003, 1.00, 3900.00, 0.00, 0.00, 0.19, 741.00, 3900.00, 4641.00 FROM ventas WHERE id_venta = 2044;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2044', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2044', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2044', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2044;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2045, 'SALE-DEMO-2045', c.id_cliente, 3000.00, 0.00, 570.00, 3570.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '389741611' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2045, 1004, 1.00, 3000.00, 0.00, 0.00, 0.19, 570.00, 3000.00, 3570.00 FROM ventas WHERE id_venta = 2045;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2045', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2045', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2045', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2045;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2046, 'SALE-DEMO-2046', c.id_cliente, 3200.00, 1600.00, 304.00, 1904.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '842850990-8' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2046, 1005, 1.00, 3200.00, 0.50, 1600.00, 0.19, 304.00, 3200.00, 1904.00 FROM ventas WHERE id_venta = 2046;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2046', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2046', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2046', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2046;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2047, 'SALE-DEMO-2047', c.id_cliente, 6500.00, 0.00, 1235.00, 7735.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '993199688' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2047, 1007, 1.00, 6500.00, 0.00, 0.00, 0.19, 1235.00, 6500.00, 7735.00 FROM ventas WHERE id_venta = 2047;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2047', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2047', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2047', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2047;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2048, 'SALE-DEMO-2048', c.id_cliente, 3500.00, 0.00, 665.00, 4165.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '820936526-4' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2048, 1008, 1.00, 3500.00, 0.00, 0.00, 0.19, 665.00, 3500.00, 4165.00 FROM ventas WHERE id_venta = 2048;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2048', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2048', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2048', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2048;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2049, 'SALE-DEMO-2049', c.id_cliente, 2600.00, 1300.00, 247.00, 1547.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '93580023' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2049, 1009, 1.00, 2600.00, 0.50, 1300.00, 0.19, 247.00, 2600.00, 1547.00 FROM ventas WHERE id_venta = 2049;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2049', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2049', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2049', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2049;
INSERT INTO ventas (id_venta, numero_venta, id_cliente, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) SELECT 2050, 'SALE-DEMO-2050', c.id_cliente, 4800.00, 0.00, 912.00, 5712.00, e.id_estado, 'Demo sales seed' FROM clientes c CROSS JOIN estados_venta e WHERE c.documento = '826709534-2' AND e.nombre = 'Registrada';
INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) SELECT 2050, 1011, 1.00, 4800.00, 0.00, 0.00, 0.19, 912.00, 4800.00, 5712.00 FROM ventas WHERE id_venta = 2050;
INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, total_descuento, monto_impuesto, monto_total) SELECT v.id_venta, 'FT-DEMO-2050', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2050', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256), CONCAT('https://frescuratotal.local/verify?cufe=', SHA2(CONCAT(v.id_venta, '|', v.numero_venta, '|', 'FT-DEMO-2050', '|', v.id_cliente, '|', CAST(v.subtotal AS CHAR), '|', CAST(v.total_descuento AS CHAR), '|', CAST(v.total_impuesto AS CHAR), '|', CAST(v.monto_total AS CHAR)), 256)), v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total FROM ventas v WHERE v.id_venta = 2050;
-- Deduct the units represented by these demo sale details from inventory stock.
UPDATE lotes_inventario l
JOIN (
    SELECT id_lote, SUM(cantidad_vendida) AS cantidad_vendida
    FROM detalles_venta
    WHERE id_venta BETWEEN 2001 AND 2050
    GROUP BY id_lote
) vendidos ON vendidos.id_lote = l.id_lote
SET l.cantidad = l.cantidad - vendidos.cantidad_vendida
WHERE l.cantidad >= vendidos.cantidad_vendida;

COMMIT;

-- ===== dml_logistics_routes.sql =====
-- DATOS DE LOGISTICA: estados de pedidos, rutas, asignaciones y paradas.
-- Cargar AL FINAL, despues de dml_suppliers_logistics.sql, dml_inventory.sql y dml_sales.sql.
-- Requiere las ventas 2001..2050 y los conductores y vehiculos de dml_suppliers_logistics.sql.
-- Solo INSERT y UPDATE de estado sobre las ventas 2001..2050.

-- 1. ESTADO DE LOS PEDIDOS segun su avance de despacho.
UPDATE ventas SET id_estado = (SELECT id_estado FROM estados_venta WHERE nombre = 'Entregada') WHERE id_venta BETWEEN 2001 AND 2024;
UPDATE ventas SET id_estado = (SELECT id_estado FROM estados_venta WHERE nombre = 'En Transito') WHERE id_venta BETWEEN 2025 AND 2030;
UPDATE ventas SET id_estado = (SELECT id_estado FROM estados_venta WHERE nombre = 'En Preparacion') WHERE id_venta BETWEEN 2031 AND 2046;
-- Las ventas 2047..2050 quedan Registradas (aun sin preparar).

-- 2. RUTAS DE ENTREGA: 19 registros (12 Completada, 3 En Transito, 3 Planificada, 1 Cancelada).
INSERT INTO rutas_entrega (id_ruta, nombre_ruta, fecha_creacion, usuario_creacion, hora_salida_estimada, hora_llegada_estimada, hora_salida_real, hora_llegada_real, estado) VALUES
(1, 'Ruta Norte 01', DATE_SUB(NOW(), INTERVAL 25 DAY), 'Andrea Molina', DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 24 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 24 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 24 DAY), INTERVAL 380 MINUTE), 'Completada'),
(2, 'Ruta Sur 02', DATE_SUB(NOW(), INTERVAL 23 DAY), 'Camilo Ferreira', DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 22 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 22 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 22 DAY), INTERVAL 380 MINUTE), 'Completada'),
(3, 'Ruta Occidente 03', DATE_SUB(NOW(), INTERVAL 21 DAY), 'Andrea Molina', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 20 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 20 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 20 DAY), INTERVAL 380 MINUTE), 'Completada'),
(4, 'Ruta Centro 04', DATE_SUB(NOW(), INTERVAL 19 DAY), 'Camilo Ferreira', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 18 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 18 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 18 DAY), INTERVAL 380 MINUTE), 'Completada'),
(5, 'Ruta Suba 05', DATE_SUB(NOW(), INTERVAL 17 DAY), 'Andrea Molina', DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 16 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 16 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 16 DAY), INTERVAL 380 MINUTE), 'Completada'),
(6, 'Ruta Kennedy 06', DATE_SUB(NOW(), INTERVAL 15 DAY), 'Camilo Ferreira', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 14 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 14 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 14 DAY), INTERVAL 380 MINUTE), 'Completada'),
(7, 'Ruta Engativá 07', DATE_SUB(NOW(), INTERVAL 13 DAY), 'Andrea Molina', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 12 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 12 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 12 DAY), INTERVAL 380 MINUTE), 'Completada'),
(8, 'Ruta Fontibón 08', DATE_SUB(NOW(), INTERVAL 11 DAY), 'Camilo Ferreira', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 10 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 10 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 10 DAY), INTERVAL 380 MINUTE), 'Completada'),
(9, 'Ruta Usaquén 09', DATE_SUB(NOW(), INTERVAL 9 DAY), 'Andrea Molina', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 8 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 8 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 8 DAY), INTERVAL 380 MINUTE), 'Completada'),
(10, 'Ruta Bosa 10', DATE_SUB(NOW(), INTERVAL 7 DAY), 'Camilo Ferreira', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 6 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 6 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 6 DAY), INTERVAL 380 MINUTE), 'Completada'),
(11, 'Ruta Teusaquillo 11', DATE_SUB(NOW(), INTERVAL 5 DAY), 'Andrea Molina', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 380 MINUTE), 'Completada'),
(12, 'Ruta Chapinero 12', DATE_SUB(NOW(), INTERVAL 3 DAY), 'Camilo Ferreira', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 10 MINUTE), DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 380 MINUTE), 'Completada'),
(13, 'Ruta Antonio Nariño 13', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Andrea Molina', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, 'En Transito'),
(14, 'Ruta Puente Aranda 14', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Camilo Ferreira', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, 'En Transito'),
(15, 'Ruta Tunjuelito 15', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Andrea Molina', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, 'En Transito'),
(16, 'Ruta Barrios Unidos 16', NOW(), 'Andrea Molina', DATE_ADD(NOW(), INTERVAL 20 HOUR), DATE_ADD(NOW(), INTERVAL 28 HOUR), NULL, NULL, 'Planificada'),
(17, 'Ruta Mártires 17', NOW(), 'Camilo Ferreira', DATE_ADD(NOW(), INTERVAL 22 HOUR), DATE_ADD(NOW(), INTERVAL 30 HOUR), NULL, NULL, 'Planificada'),
(18, 'Ruta Ciudad Bolívar 18', NOW(), 'Andrea Molina', DATE_ADD(NOW(), INTERVAL 24 HOUR), DATE_ADD(NOW(), INTERVAL 32 HOUR), NULL, NULL, 'Planificada'),
(19, 'Ruta Funza 19', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Andrea Molina', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 9 HOUR), NULL, NULL, 'Cancelada');

-- 3. ASIGNACION DE VEHICULO Y CONDUCTOR (una por ruta).
INSERT INTO asignaciones_entrega (id_ruta, id_vehiculo, id_conductor, usuario_asignacion) VALUES
(1, 1, 1, 'Andrea Molina'),
(2, 2, 2, 'Camilo Ferreira'),
(3, 3, 3, 'Andrea Molina'),
(4, 4, 4, 'Camilo Ferreira'),
(5, 5, 5, 'Andrea Molina'),
(6, 7, 6, 'Camilo Ferreira'),
(7, 8, 7, 'Andrea Molina'),
(8, 9, 8, 'Camilo Ferreira'),
(9, 10, 10, 'Andrea Molina'),
(10, 11, 11, 'Camilo Ferreira'),
(11, 13, 12, 'Andrea Molina'),
(12, 14, 13, 'Camilo Ferreira'),
(13, 15, 14, 'Andrea Molina'),
(14, 16, 15, 'Camilo Ferreira'),
(15, 17, 16, 'Andrea Molina'),
(16, 19, 17, 'Andrea Molina'),
(17, 20, 18, 'Camilo Ferreira'),
(18, 21, 19, 'Andrea Molina'),
(19, 22, 20, 'Andrea Molina');

-- 4. PARADAS: pedidos de cada ruta, con direccion tomada del cliente.
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 1, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 1), INTERVAL 90 MINUTE), 'Marcela Rojas' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2001;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 1, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 1), INTERVAL 150 MINUTE), 'Hernán Castaño' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2002;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 2, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 2), INTERVAL 90 MINUTE), 'Luz Dary Pineda' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2003;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 2, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 2), INTERVAL 150 MINUTE), 'Jairo Beltrán' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2004;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 3, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 3), INTERVAL 90 MINUTE), 'Sandra Patiño' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2005;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 3, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 3), INTERVAL 150 MINUTE), 'Wilmer Acosta' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2006;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 4, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 4), INTERVAL 90 MINUTE), 'Gloria Quintero' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2007;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 4, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 4), INTERVAL 150 MINUTE), 'Mauricio Salgado' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2008;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 5, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 5), INTERVAL 90 MINUTE), 'Dora Cifuentes' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2009;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 5, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 5), INTERVAL 150 MINUTE), 'Néstor Parra' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2010;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 6, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 6), INTERVAL 90 MINUTE), 'Yolanda Mejía' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2011;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 6, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 6), INTERVAL 150 MINUTE), 'Édgar Barrero' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2012;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 7, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 7), INTERVAL 90 MINUTE), 'Patricia Lozano' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2013;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 7, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 7), INTERVAL 150 MINUTE), 'Orlando Cuervo' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2014;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 8, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 8), INTERVAL 90 MINUTE), 'Teresa Mahecha' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2015;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 8, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 8), INTERVAL 150 MINUTE), 'Fabio Camargo' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2016;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 9, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 9), INTERVAL 90 MINUTE), 'Rosalba Niño' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2017;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 9, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 9), INTERVAL 150 MINUTE), 'Gilberto Ardila' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2018;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 10, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 10), INTERVAL 90 MINUTE), 'Amparo Vélez' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2019;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 10, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 10), INTERVAL 150 MINUTE), 'Rubén Alarcón' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2020;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 11, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 11), INTERVAL 90 MINUTE), 'Clara Suárez' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2021;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 11, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 11), INTERVAL 150 MINUTE), 'Arturo Peña' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2022;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 12, v.id_venta, 1, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 12), INTERVAL 90 MINUTE), 'Martha Gaitán' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2023;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por) SELECT 12, v.id_venta, 2, COALESCE(c.direccion, 'N/A'), DATE_ADD((SELECT hora_salida_real FROM rutas_entrega WHERE id_ruta = 12), INTERVAL 150 MINUTE), 'Efraín Moya' FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2024;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 13, v.id_venta, 1, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2025;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 13, v.id_venta, 2, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2026;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 14, v.id_venta, 1, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2027;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 14, v.id_venta, 2, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2028;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 15, v.id_venta, 1, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2029;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 15, v.id_venta, 2, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2030;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 16, v.id_venta, 1, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2031;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 16, v.id_venta, 2, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2032;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 17, v.id_venta, 1, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2033;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 17, v.id_venta, 2, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2034;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 18, v.id_venta, 1, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2035;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 18, v.id_venta, 2, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2036;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 19, v.id_venta, 1, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2037;
INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) SELECT 19, v.id_venta, 2, COALESCE(c.direccion, 'N/A') FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente WHERE v.id_venta = 2038;
