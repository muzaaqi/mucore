package com.muzone.mucore.util;

import com.muzone.mucore.MuCore;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final MuCore plugin;

    public ConfigManager(MuCore plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig(); // Membuat config.yml jika belum ada
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }
    
    // Helper untuk mengambil String berwarna
    public String getString(String path) {
        String val = plugin.getConfig().getString(path);
        return val != null ? val.replace("&", "§") : path;
    }
}