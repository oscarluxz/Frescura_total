package frescuratotal.erp.logistics.repository;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.logistics.model.DeliveryRoute;
import frescuratotal.erp.logistics.model.DispatchStatus;
import frescuratotal.erp.logistics.model.Driver;
import frescuratotal.erp.logistics.model.RouteStatus;
import frescuratotal.erp.logistics.model.RouteStop;
import frescuratotal.erp.logistics.model.Vehicle;
import frescuratotal.erp.logistics.model.VehicleStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcDeliveryRouteRepository implements DeliveryRouteRepository {

    private static final String SELECT_ROUTE =
            "SELECT r.id_ruta, r.nombre_ruta, r.fecha_creacion, r.usuario_creacion, "
            + "r.hora_salida_estimada, r.hora_llegada_estimada, r.hora_salida_real, r.hora_llegada_real, "
            + "r.estado AS estado_ruta, v.id_vehiculo, v.placa, v.modelo, v.capacidad, "
            + "v.estado AS estado_vehiculo, d.id_conductor, d.nombre AS conductor, d.numero_licencia, "
            + "d.telefono, d.email, d.activo "
            + "FROM rutas_entrega r "
            + "LEFT JOIN asignaciones_entrega a ON a.id_ruta = r.id_ruta "
            + "LEFT JOIN vehiculos v ON v.id_vehiculo = a.id_vehiculo "
            + "LEFT JOIN conductores d ON d.id_conductor = a.id_conductor ";

    @Override
    public int create(DeliveryRoute route, List<Integer> saleIds) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                lockAndValidateAssignment(c, route, saleIds);
                int routeId = insertRoute(c, route);
                insertAssignment(c, routeId, route);
                insertStops(c, routeId, saleIds);
                c.commit();
                return routeId;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    @Override
    public void start(int routeId) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE rutas_entrega SET estado = 'En Transito', hora_salida_real = NOW() "
                        + "WHERE id_ruta = ? AND estado = 'Planificada'")) {
                    ps.setInt(1, routeId);
                    if (ps.executeUpdate() == 0) {
                        throw new IllegalStateException("Solo se puede iniciar una ruta en estado Planificada.");
                    }
                }
                for (int saleId : saleIdsOfRoute(c, routeId)) {
                    changeSaleStatus(c, saleId, DispatchStatus.IN_TRANSIT);
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    @Override
    public void markDelivered(int routeId, int saleId, String receivedBy, String observations)
            throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                checkRouteInTransit(c, routeId, saleId);
                changeSaleStatus(c, saleId, DispatchStatus.DELIVERED);
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE ventas_ruta SET fecha_entrega = NOW(), recibido_por = ?, observaciones = ? "
                        + "WHERE id_ruta = ? AND id_venta = ?")) {
                    ps.setString(1, receivedBy);
                    ps.setString(2, observations);
                    ps.setInt(3, routeId);
                    ps.setInt(4, saleId);
                    ps.executeUpdate();
                }
                if (countUndelivered(c, routeId) == 0) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE rutas_entrega SET estado = 'Completada', hora_llegada_real = NOW() "
                            + "WHERE id_ruta = ?")) {
                        ps.setInt(1, routeId);
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    @Override
    public void cancel(int routeId) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE rutas_entrega SET estado = 'Cancelada' WHERE id_ruta = ? AND estado = 'Planificada'")) {
            ps.setInt(1, routeId);
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException("Solo se puede cancelar una ruta en estado Planificada.");
            }
        }
    }

    @Override
    public List<DeliveryRoute> findAll() throws SQLException {
        List<DeliveryRoute> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ROUTE + "ORDER BY r.fecha_creacion DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRoute(rs));
            }
        }
        return list;
    }

    @Override
    public DeliveryRoute findById(int routeId) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ROUTE + "WHERE r.id_ruta = ?")) {
            ps.setInt(1, routeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                DeliveryRoute route = mapRoute(rs);
                route.setStops(findStops(c, routeId));
                return route;
            }
        }
    }

    // ---------- helpers (they receive the open transaction's connection) ----------

    private void lockAndValidateAssignment(Connection connection, DeliveryRoute route, List<Integer> saleIds) throws SQLException {
        if (route.getVehicle() == null || route.getDriver() == null || saleIds == null || saleIds.isEmpty()
                || new java.util.HashSet<>(saleIds).size() != saleIds.size()) {
            throw new IllegalArgumentException("La ruta requiere vehículo, conductor y pedidos únicos.");
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT estado FROM vehiculos WHERE id_vehiculo=? FOR UPDATE")) {
            statement.setInt(1, route.getVehicle().getId());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !"Activo".equals(result.getString(1))) {
                    throw new IllegalStateException("El vehículo no está disponible.");
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT activo FROM conductores WHERE id_conductor=? FOR UPDATE")) {
            statement.setInt(1, route.getDriver().getId());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !result.getBoolean(1)) { throw new IllegalStateException("El conductor no está disponible."); }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT r.id_ruta FROM asignaciones_entrega a JOIN rutas_entrega r ON r.id_ruta=a.id_ruta "
                + "WHERE (a.id_vehiculo=? OR a.id_conductor=?) AND r.estado IN ('Planificada','En Transito') LIMIT 1 FOR UPDATE")) {
            statement.setInt(1, route.getVehicle().getId());
            statement.setInt(2, route.getDriver().getId());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) { throw new IllegalStateException("El vehículo o conductor ya está asignado a una ruta activa."); }
            }
        }
        List<Integer> sortedIds = new ArrayList<>(saleIds);
        java.util.Collections.sort(sortedIds);
        for (int saleId : sortedIds) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT e.nombre FROM ventas v JOIN estados_venta e ON e.id_estado=v.id_estado WHERE v.id_venta=? FOR UPDATE")) {
                statement.setInt(1, saleId);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next() || !"En Preparacion".equals(result.getString(1))) {
                        throw new IllegalStateException("El pedido no está En preparación.");
                    }
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT r.id_ruta FROM ventas_ruta vr JOIN rutas_entrega r ON r.id_ruta=vr.id_ruta "
                    + "WHERE vr.id_venta=? AND r.estado<>'Cancelada' LIMIT 1 FOR UPDATE")) {
                statement.setInt(1, saleId);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) { throw new IllegalStateException("El pedido ya está asignado a una ruta."); }
                }
            }
        }
    }

    private int insertRoute(Connection c, DeliveryRoute route) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO rutas_entrega (nombre_ruta, usuario_creacion, hora_salida_estimada, "
                + "hora_llegada_estimada) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, route.getName());
            ps.setString(2, route.getCreatedBy());
            ps.setTimestamp(3, route.getEstimatedDeparture());
            ps.setTimestamp(4, route.getEstimatedArrival());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void insertAssignment(Connection c, int routeId, DeliveryRoute route) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO asignaciones_entrega (id_ruta, id_vehiculo, id_conductor, usuario_asignacion) "
                + "VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, routeId);
            ps.setInt(2, route.getVehicle().getId());
            ps.setInt(3, route.getDriver().getId());
            ps.setString(4, route.getCreatedBy());
            ps.executeUpdate();
        }
    }

    private void insertStops(Connection c, int routeId, List<Integer> saleIds) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) "
                + "SELECT ?, v.id_venta, ?, COALESCE(cl.direccion, 'N/A') "
                + "FROM ventas v JOIN clientes cl ON cl.id_cliente = v.id_cliente WHERE v.id_venta = ?")) {
            int order = 1;
            for (int saleId : saleIds) {
                ps.setInt(1, routeId);
                ps.setInt(2, order);
                ps.setInt(3, saleId);
                ps.executeUpdate();
                order++;
            }
        }
    }

    private void checkRouteInTransit(Connection c, int routeId, int saleId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT r.estado FROM rutas_entrega r JOIN ventas_ruta vr ON vr.id_ruta = r.id_ruta "
                + "WHERE r.id_ruta = ? AND vr.id_venta = ? FOR UPDATE")) {
            ps.setInt(1, routeId);
            ps.setInt(2, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "El pedido " + saleId + " no pertenece a la ruta " + routeId + ".");
                }
                if (RouteStatus.fromDbName(rs.getString(1)) != RouteStatus.IN_TRANSIT) {
                    throw new IllegalStateException("La ruta debe estar En tránsito para entregar pedidos.");
                }
            }
        }
    }

    private int countUndelivered(Connection c, int routeId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM ventas_ruta vr JOIN ventas v ON v.id_venta = vr.id_venta "
                + "JOIN estados_venta e ON e.id_estado = v.id_estado "
                + "WHERE vr.id_ruta = ? AND e.nombre <> ?")) {
            ps.setInt(1, routeId);
            ps.setString(2, DispatchStatus.DELIVERED.getDbName());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Applies a sale status change only if the state machine allows it. */
    private void changeSaleStatus(Connection c, int saleId, DispatchStatus target) throws SQLException {
        DispatchStatus current;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT e.nombre FROM ventas v JOIN estados_venta e ON e.id_estado = v.id_estado "
                + "WHERE v.id_venta = ? FOR UPDATE")) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("No se encontró la venta " + saleId + ".");
                current = DispatchStatus.fromDbName(rs.getString(1));
            }
        }
        if (current == null || !current.canTransitionTo(target)) {
            throw new IllegalStateException("La venta " + saleId + " no puede pasar de "
                    + (current == null ? "su estado actual" : current.getLabel()) + " a " + target.getLabel() + ".");
        }
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE ventas SET id_estado = (SELECT id_estado FROM estados_venta WHERE nombre = ?) "
                + "WHERE id_venta = ?")) {
            ps.setString(1, target.getDbName());
            ps.setInt(2, saleId);
            ps.executeUpdate();
        }
    }

    private List<Integer> saleIdsOfRoute(Connection c, int routeId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id_venta FROM ventas_ruta WHERE id_ruta = ? ORDER BY orden_entrega")) {
            ps.setInt(1, routeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    private List<RouteStop> findStops(Connection c, int routeId) throws SQLException {
        List<RouteStop> stops = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT vr.id_venta, v.numero_venta, vr.orden_entrega, vr.direccion_destino, "
                + "vr.fecha_entrega, vr.recibido_por, e.nombre AS estado "
                + "FROM ventas_ruta vr JOIN ventas v ON v.id_venta = vr.id_venta "
                + "JOIN estados_venta e ON e.id_estado = v.id_estado "
                + "WHERE vr.id_ruta = ? ORDER BY vr.orden_entrega")) {
            ps.setInt(1, routeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stops.add(new RouteStop(
                            rs.getInt("id_venta"), rs.getString("numero_venta"),
                            rs.getInt("orden_entrega"), rs.getString("direccion_destino"),
                            rs.getTimestamp("fecha_entrega"), rs.getString("recibido_por"),
                            DispatchStatus.fromDbName(rs.getString("estado"))));
                }
            }
        }
        return stops;
    }

    private DeliveryRoute mapRoute(ResultSet rs) throws SQLException {
        DeliveryRoute r = new DeliveryRoute();
        r.setId(rs.getInt("id_ruta"));
        r.setName(rs.getString("nombre_ruta"));
        r.setCreatedAt(rs.getTimestamp("fecha_creacion"));
        r.setCreatedBy(rs.getString("usuario_creacion"));
        r.setEstimatedDeparture(rs.getTimestamp("hora_salida_estimada"));
        r.setEstimatedArrival(rs.getTimestamp("hora_llegada_estimada"));
        r.setActualDeparture(rs.getTimestamp("hora_salida_real"));
        r.setActualArrival(rs.getTimestamp("hora_llegada_real"));
        r.setStatus(RouteStatus.fromDbName(rs.getString("estado_ruta")));
        if (rs.getObject("id_vehiculo") != null) {
            Vehicle v = new Vehicle();
            v.setId(rs.getInt("id_vehiculo"));
            v.setPlate(rs.getString("placa"));
            v.setModel(rs.getString("modelo"));
            v.setCapacity(rs.getDouble("capacidad"));
            v.setStatus(VehicleStatus.fromDbName(rs.getString("estado_vehiculo")));
            r.setVehicle(v);
        }
        if (rs.getObject("id_conductor") != null) {
            Driver d = new Driver();
            d.setId(rs.getInt("id_conductor"));
            d.setName(rs.getString("conductor"));
            d.setLicenseNumber(rs.getString("numero_licencia"));
            d.setPhone(rs.getString("telefono"));
            d.setEmail(rs.getString("email"));
            d.setActive(rs.getBoolean("activo"));
            r.setDriver(d);
        }
        return r;
    }
}
