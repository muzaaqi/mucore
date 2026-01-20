package com.muzone.mucore.data.database;

import com.muzone.mucore.MuCore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class H2Database implements Database {

    private final MuCore plugin;
    private HikariDataSource dataSource;

    public H2Database(MuCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        // Buat folder plugin jika belum ada
        File dataFolder = new File(plugin.getDataFolder(), "database");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        HikariConfig config = new HikariConfig();
        
        // JDBC URL untuk H2 (File Mode)
        // Ini akan membuat file 'mucore_data.mv.db' di folder /plugins/MuCore/database/
        config.setJdbcUrl("jdbc:h2:file:" + dataFolder.getAbsolutePath() + "/mucore_data;MODE=MySQL");
        config.setDriverClassName("org.h2.Driver");
        
        // Optimasi H2
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(5000);

        this.dataSource = new HikariDataSource(config);
        
        initTables();
        plugin.getLogger().info("Connected to local H2 database.");
    }

    @Override
    public void initTables() {
        // Syntax H2 mirip dengan MySQL karena kita pakai MODE=MySQL
        String query = "CREATE TABLE IF NOT EXISTS mucore_violations (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "uuid VARCHAR(36) NOT NULL, " +
                "check_name VARCHAR(50), " +
                "vl DOUBLE, " +
                "details VARCHAR(255), " +
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
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT INTO mucore_violations (uuid, check_name, vl, details) VALUES (?, ?, ?, ?)";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid);
                ps.setString(2, checkName);
                ps.setDouble(3, vl);
                ps.setString(4, details);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Database Error: " + e.getMessage());
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