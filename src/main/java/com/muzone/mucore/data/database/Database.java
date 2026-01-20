package com.muzone.mucore.data.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface Database {
    void connect();
    void disconnect();
    Connection getConnection() throws SQLException;
    void initTables(); // Membuat tabel otomatis jika belum ada
    void saveViolation(String uuid, String checkName, double vl, String details);
}