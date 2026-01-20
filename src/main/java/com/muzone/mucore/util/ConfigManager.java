package com.muzone.mucore.util;

import com.muzone.mucore.MuCore;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final MuCore plugin;

    public ConfigManager(MuCore plugin) {
        this.plugin = plugin;
        
        // Logic asli Anda (Preserved)
        plugin.saveDefaultConfig(); // Membuat config.yml jika belum ada
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }
    
    // --- Helper Methods ---

    // 1. Get String (Dengan Color Translate - Kode Asli Anda)
    public String getString(String path) {
        String val = plugin.getConfig().getString(path);
        return val != null ? val.replace("&", "§") : path;
    }

    // 2. Get Boolean (DIBUTUHKAN oleh WebhookManager)
    public boolean getBoolean(String path) {
        // Default false jika tidak ada di config
        return plugin.getConfig().getBoolean(path, false);
    }

    // 3. Get Integer (DIBUTUHKAN oleh WebhookManager & PacketLimiter)
    public int getInt(String path) {
        // Default 0 jika tidak ada di config
        return plugin.getConfig().getInt(path, 0);
    }

    // 4. Get Double (Persiapan untuk Speed Check limits nanti)
    public double getDouble(String path) {
        return plugin.getConfig().getDouble(path, 0.0);
    }
}