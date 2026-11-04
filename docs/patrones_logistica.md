# Patrones y principios de diseño: módulo de logística

Este documento explica dónde se aplicaron patrones de diseño y principios SOLID en el paquete `frescuratotal.erp.logistics`, y por qué se tomó cada decisión.

## Patrón Repository / DAO

Cada entidad de logística tiene una **interfaz** y una **implementación JDBC** en `logistics/repository`:

| Interfaz | Implementación |
|---|---|
| `VehicleRepository` | `JdbcVehicleRepository` |
| `DriverRepository` | `JdbcDriverRepository` |
| `DispatchOrderRepository` | `JdbcDispatchOrderRepository` |
| `DeliveryRouteRepository` | `JdbcDeliveryRouteRepository` |

Todo el SQL vive únicamente en las clases `Jdbc*`. El controlador y la vista solo conocen las interfaces. Se eligió este patrón porque separa la lógica de negocio del acceso a datos: si cambia una tabla, solo se modifica la clase `Jdbc*` correspondiente, y la vista nunca toca SQL.

## Principios SOLID

**Responsabilidad única (S).** Cada clase hace una sola cosa. Los modelos (`Vehicle`, `Driver`, `DeliveryRoute`, `RouteStop`, `DispatchOrder`) guardan datos. Los repositorios persisten. `LogisticsController` aplica las reglas de negocio (placa válida, vehículo y conductor disponibles, pedidos sin ruta). `LogisticsView` solo lee y muestra por consola. Los repositorios de vehículos, conductores, despachos y rutas están separados en lugar de mezclarse en una sola clase gigante.

**Inversión de dependencias (D).** `LogisticsController` recibe los cuatro repositorios por constructor y depende de las interfaces, no de las clases `Jdbc*`. El ensamblado se hace en `Main`: repositorios, luego controlador, luego vista. Esto permite cambiar la fuente de datos sin tocar el controlador.

**Segregación de interfaces (I).** `DispatchOrderRepository` concentra la preparación de pedidos y la consulta del panel; `DeliveryRouteRepository` gestiona rutas y entregas. Las interfaces de vehículos y conductores mantienen sus operaciones propias, sin obligar a un repositorio a implementar la persistencia de todo el módulo.

## Máquina de estados del ciclo de entrega

El enum `DispatchStatus` modela el ciclo `IN_PREPARATION`, `IN_TRANSIT`, `DELIVERED`. El método `canTransitionTo()` solo permite avanzar un paso. `JdbcDeliveryRouteRepository.changeSaleStatus()` lo consulta antes de actualizar la venta, así que es imposible saltar de "En preparación" a "Entregada" o retroceder. La regla vive en un solo lugar y no repartida en `if` por toda la aplicación.

El paso previo Registrada → En preparación se ejecuta desde la opción 9 de logística mediante un UPDATE condicionado al estado Registrada. Si no existe la venta o ya está en otro estado, no se modifica ninguna fila y se informa el rechazo.

Los enums `DispatchStatus`, `RouteStatus` y `VehicleStatus` guardan dos textos: `dbName`, el valor exacto que usa MySQL (sin tildes, por ejemplo `En Transito`), y `label`, el texto en español que se muestra al usuario. Así la base de datos y la consola no se mezclan.

## Transacciones

Crear una ruta (ruta, asignación de vehículo y conductor, y paradas) y también iniciar una ruta o confirmar una entrega modifican varias tablas a la vez. Cada una de estas operaciones en `JdbcDeliveryRouteRepository` desactiva el `autoCommit`, ejecuta todos sus pasos y hace `commit` si todo salió bien. Si ocurre cualquier error hace `rollback`, de modo que la base de datos nunca queda a medias: no puede haber una ruta creada sin paradas, ni una ruta en tránsito con pedidos que siguen en preparación.

La disponibilidad se vuelve a comprobar dentro de la transacción: se bloquean vehículo, conductor y pedidos antes de guardar la asignación. Así dos solicitudes no pueden reservar simultáneamente el mismo recurso o pedido aunque ambas hayan visto el panel disponible. Para las entregas se bloquea la ruta antes de cambiar el pedido y contar pendientes, evitando que dos entregas simultáneas dejen la ruta abierta cuando ya no quedan pedidos pendientes.

## Modelo de lectura para el panel de despacho

El panel muestra **todos** los pedidos En preparación, y una columna indica si cada uno ya tiene ruta. Para esto `JdbcDispatchOrderRepository` usa un `LEFT JOIN` hacia las rutas activas: un pedido sin ruta aparece igual, con la ruta vacía. Las rutas canceladas se ignoran, así que sus pedidos vuelven a verse como "Sin ruta". El controlador valida los pedidos elegidos y el repositorio vuelve a validar su estado y asignación con bloqueos dentro de la transacción.

## Decisiones adicionales

- **Disponibilidad:** un vehículo o conductor solo puede asignarse si está activo y no pertenece a una ruta `Planificada` o `En Transito`.
- **Seguridad:** todas las consultas usan `PreparedStatement` con parámetros, nunca se arma SQL concatenando texto del usuario, para evitar inyección SQL.
- **Mensajes:** el código, los identificadores y los comentarios están en inglés. Los textos que ve el usuario en consola, incluidos los mensajes de error, están en español.
