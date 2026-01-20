package com.muzone.mucore.data.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface Database {
    void connect();
    void disconnect();
    Connection getConnection() throws SQLException;
    void initTables(); 
    void saveViolation(String uuid, String checkName, double vl, String details);
    
    // METHOD WAJIB (Agar Command '/mucore status' tidak error)
    String getType();
}