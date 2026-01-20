package com.muzone.mucore.data.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.muzone.mucore.MuCore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLDatabase implements Database {

    private HikariDataSource dataSource;
    private final MuCore plugin;

    public MySQLDatabase(MuCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        HikariConfig config = new HikariConfig();
        
        // Load settings from config.yml
        String host = plugin.getConfig().getString("database.host");
        String port = plugin.getConfig().getString("database.port");
        String dbName = plugin.getConfig().getString("database.name");
        
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false");
        config.setUsername(plugin.getConfig().getString("database.user"));
        config.setPassword(plugin.getConfig().getString("database.pass"));
        
        // Enterprise Optimization settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(10); // Menjaga koneksi tetap efisien

        this.dataSource = new HikariDataSource(config);
        initTables();
        plugin.getLogger().info("Database connected successfully using HikariCP.");
    }

    @Override
    public void initTables() {
        // Tabel Log Pelanggaran
        String query = "CREATE TABLE IF NOT EXISTS mucore_violations (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "uuid VARCHAR(36) NOT NULL, " +
                "check_name VARCHAR(50), " +
                "vl DOUBLE, " +
                "details TEXT, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveViolation(String uuid, String checkName, double vl, String details) {
        // Dilakukan secara Async agar tidak membekukan server
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT INTO mucore_violations (uuid, check_name, vl, details) VALUES (?, ?, ?, ?)";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid);
                ps.setString(2, checkName);
                ps.setDouble(3, vl);
                ps.setString(4, details);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save violation: " + e.getMessage());
            }
        });
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void disconnect() {
        if (dataSource != null) dataSource.close();
    }
}