package com.muzone.mucore;

import com.muzone.mucore.bridge.ProtocolLibBridge;
import com.muzone.mucore.check.CheckManager;
import com.muzone.mucore.data.PlayerManager;
import com.muzone.mucore.util.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MuCore extends JavaPlugin {
    private static MuCore instance;
    private ConfigManager configManager;
    private PlayerManager playerManager;
    private CheckManager checkManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // 1. Load Configurations
        this.configManager = new ConfigManager(this);
        
        // 2. Initialize Data & Managers
        this.playerManager = new PlayerManager();
        this.checkManager = new CheckManager(this);
        
        // 3. Register Bridges (ProtocolLib, Geyser)
        new ProtocolLibBridge(this).register();
        
        getLogger().info("MUCORE AntiCheat has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Cleanup data and save to DB
    }

    public static MuCore getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
}