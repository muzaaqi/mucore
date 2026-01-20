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
        // Mengambil setting dari config (lihat langkah 3)
        String type = plugin.getConfigManager().getString("database.storage-type");

        if (type != null && type.equalsIgnoreCase("MYSQL")) {
            plugin.getLogger().info("Database selected: MySQL (Network Mode)");
            this.database = new MySQLDatabase(plugin);
        } else {
            plugin.getLogger().info("Database selected: H2 (Local File Mode)");
            this.database = new H2Database(plugin);
        }

        try {
            this.database.connect();
            this.database.initTables(); // PENTING: Jangan lupa buat tabelnya!
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