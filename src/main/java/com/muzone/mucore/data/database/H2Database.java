package com.muzone.mucore.data.database;

import com.muzone.mucore.MuCore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class H2Database implements Database {

    private final MuCore plugin;
    private HikariDataSource dataSource;

    public H2Database(MuCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        File dbFile = new File(plugin.getDataFolder(), "database/mucore_data");
        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:file:" + dbFile.getAbsolutePath() + ";MODE=MySQL");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("");
        config.setPassword("");
        
        // Optimasi Pool
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000); 
        
        this.dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Connected to local H2 database.");
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void initTables() {
        String sql = "CREATE TABLE IF NOT EXISTS mucore_logs (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "uuid VARCHAR(36), " +
                     "check_name VARCHAR(32), " +
                     "vl DOUBLE, " +
                     "details VARCHAR(255), " +
                     "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                     ");";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveViolation(String uuid, String checkName, double vl, String details) {
        String sql = "INSERT INTO mucore_logs (uuid, check_name, vl, details) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setString(2, checkName);
            ps.setDouble(3, vl);
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getType() {
        return "H2 (Local File)";
    }
}