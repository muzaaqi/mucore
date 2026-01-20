package com.muzone.mucore.data;

import com.muzone.mucore.MuCore;
import com.muzone.mucore.data.database.Database;
import com.muzone.mucore.data.database.H2Database;
import com.muzone.mucore.data.database.MySQLDatabase;

public class DataManager {

    private final MuCore plugin;
    private Database database;

    public DataManager(MuCore plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        String type = plugin.getConfigManager().getString("settings.storage-type");

        // Logic Seleksi Otomatis
        if (type != null && type.equalsIgnoreCase("MYSQL")) {
            // Gunakan MySQL jika diminta secara eksplisit
            plugin.getLogger().info("Database selected: MySQL (Network Mode)");
            this.database = new MySQLDatabase(plugin);
        } else {
            // DEFAULT ke H2 (Local File)
            // Ini menangani config "H2" atau jika user salah ketik
            plugin.getLogger().info("Database selected: H2 (Local File Mode)");
            this.database = new H2Database(plugin);
        }

        try {
            this.database.connect();
        } catch (Exception e) {
            plugin.getLogger().severe("CRITICAL: Failed to initialize database!");
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