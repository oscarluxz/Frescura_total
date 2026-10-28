/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package frescuratotal.erp.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton that centralizes the database configuration.
 * Shared by the whole team: if a teammate already created it, keep theirs.
 */
public final class DatabaseConnection {

    // Example credentials: change them to match your local MySQL.
    private static final String URL =
            "jdbc:mysql://localhost:3306/erp_frescura_total?useSSL=false&serverTimezone=America/Bogota";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static DatabaseConnection instance;

    private DatabaseConnection() { }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** Returns a NEW connection; callers must close it (try-with-resources). */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}