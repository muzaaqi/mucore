package com.muzone.mucore.data;

import com.muzone.mucore.MuCore;
import com.muzone.mucore.data.database.Database;
import com.muzone.mucore.data.database.MySQLDatabase;

public class DataManager {

    private final MuCore plugin;
    private Database database;

    public DataManager(MuCore plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        // Cek tipe database dari config.yml
        String type = plugin.getConfigManager().getString("settings.storage-type");

        // Logic pemilihan Database
        if (type != null && type.equalsIgnoreCase("MYSQL")) {
            this.database = new MySQLDatabase(plugin);
        } else {
            // Default ke MySQL untuk saat ini (atau H2 jika Anda nanti membuatnya)
            // Untuk Enterprise, biasanya fallback ke SQLite/H2 local file
            plugin.getLogger().info("Using Database: MySQL (Default)");
            this.database = new MySQLDatabase(plugin);
        }

        // Lakukan koneksi
        try {
            this.database.connect();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to database! Plugin functionality will be limited.");
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (this.database != null) {
            this.database.disconnect();
        }
    }

    public Database getDatabase() {
        return this.database;
    }
}