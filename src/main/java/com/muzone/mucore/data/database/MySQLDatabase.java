package com.muzone.mucore.data.database;

import com.muzone.mucore.MuCore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLDatabase implements Database {

    private final MuCore plugin;
    private HikariDataSource dataSource;

    public MySQLDatabase(MuCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        String host = plugin.getConfigManager().getString("database.host");
        int port = plugin.getConfigManager().getInt("database.port");
        String database = plugin.getConfigManager().getString("database.name");
        String username = plugin.getConfigManager().getString("database.user");
        String password = plugin.getConfigManager().getString("database.password");
        boolean useSSL = plugin.getConfigManager().getBoolean("database.useSSL");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver"); // Driver Modern
        config.setUsername(username);
        config.setPassword(password);

        // Optimasi Pool MySQL
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setLeakDetectionThreshold(60000);

        this.dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Connected to remote MySQL database.");
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
        // MySQL Syntax
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
        return "MySQL (Network)";
    }
}