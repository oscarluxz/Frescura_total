# Principios y patrones de diseño: módulo de proveedores

## Patrón Singleton (creacional)
La clase `DatabaseConnection` (paquete `common`) es un Singleton. Tiene un constructor privado, un atributo estático que guarda la única instancia y un método `getInstance()` que la crea la primera vez y la reutiliza después. Se aplicó porque la configuración de la conexión (URL, usuario y contraseña) debe existir en un solo lugar y ser la misma para todos los módulos del proyecto. Así, si cambia la contraseña de MySQL, se modifica una sola clase.

## Patrón DAO / Repository
El módulo de proveedores accede a la base de datos mediante interfaces (`SupplierRepository` y `PurchaseHistoryRepository`) y sus implementaciones JDBC (`JdbcSupplierRepository` y `JdbcPurchaseHistoryRepository`). Todo el SQL vive únicamente en las implementaciones. El controlador solo conoce las interfaces. Se eligió este patrón para separar la lógica de negocio del acceso a datos: si cambiara la base de datos, solo habría que escribir otra implementación de las interfaces, sin tocar el controlador ni la vista.

## Principios SOLID

**Responsabilidad única (S).** Cada clase hace una sola cosa. Los modelos (`Supplier`, `CatalogProduct`, `PurchaseRecord`, `SupplierReliability`) guardan datos y reglas propias, como la calificación de confiabilidad. Los repositorios guardan y consultan. El controlador (`SupplierController`) valida las reglas de negocio. La vista (`SupplierView`) solo lee lo que escribe el usuario y muestra resultados.

**Inversión de dependencias (D).** `SupplierController` recibe sus repositorios por el constructor y declara los atributos con el tipo de la interfaz, no de la clase concreta. De esta forma depende de abstracciones y no de detalles de JDBC.

**Segregación de interfaces (I).** El historial de compras tiene su propia interfaz de solo lectura (`PurchaseHistoryRepository`), separada de `SupplierRepository`. Quien solo consulta el historial no necesita conocer las operaciones de crear, actualizar ni desactivar proveedores.

## Manejo de excepciones
El controlador lanza `IllegalArgumentException` cuando el usuario ingresa datos inválidos (por ejemplo un NIT mal formado o repetido), y los repositorios lanzan `SQLException` cuando falla la base de datos. La vista captura ambas y muestra un mensaje distinto en cada caso, en español. Los datos inválidos se detectan antes de llegar a la base de datos.

## Decisiones de diseño
- **Desactivar en lugar de eliminar:** los proveedores no se borran físicamente porque los lotes del inventario los referencian. En su lugar se marcan como inactivos (`activo = FALSE`), lo que conserva el historial de abastecimiento.
- **Historial de compras:** cada lote registrado en `lotes_inventario` representa una entrega del proveedor. La confiabilidad se calcula con la cantidad de lotes recibidos, las mermas registradas y las inspecciones rechazadas.
