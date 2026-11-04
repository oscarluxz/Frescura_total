package frescuratotal.erp.logistics.repository;

import frescuratotal.erp.logistics.model.DeliveryRoute;

import java.sql.SQLException;
import java.util.List;

/**
 * Persistence of delivery routes. Operations that touch several tables
 * (route + assignment + stops + sale status) are atomic.
 */
public interface DeliveryRouteRepository {
    /** Creates the route, its vehicle/driver assignment and its stops. Returns the new route id. */
    int create(DeliveryRoute route, List<Integer> saleIds) throws SQLException;
    /** PLANNED -> IN_TRANSIT; every order of the route moves IN_PREPARATION -> IN_TRANSIT. */
    void start(int routeId) throws SQLException;
    /** Marks one order as DELIVERED; completes the route when no stops remain. */
    void markDelivered(int routeId, int saleId, String receivedBy, String observations) throws SQLException;
    void cancel(int routeId) throws SQLException;
    List<DeliveryRoute> findAll() throws SQLException;
    DeliveryRoute findById(int routeId) throws SQLException;
}
