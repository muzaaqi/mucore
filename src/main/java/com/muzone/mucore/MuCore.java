package com.muzone.mucore;

import com.muzone.mucore.bridge.GeyserBridge;
import com.muzone.mucore.bridge.ProtocolLibBridge;
import com.muzone.mucore.check.CheckManager;
import com.muzone.mucore.check.combat.KillAuraCheck;
import com.muzone.mucore.check.combat.ReachCheck;
import com.muzone.mucore.check.movement.FlyCheck;
import com.muzone.mucore.check.movement.OmniSprintCheck;
import com.muzone.mucore.check.movement.SpeedCheck;
import com.muzone.mucore.check.packet.PacketLimiterCheck;
import com.muzone.mucore.command.MuCommand;
import com.muzone.mucore.data.ActionManager;
import com.muzone.mucore.data.DataManager;
import com.muzone.mucore.data.PlayerManager;
import com.muzone.mucore.data.database.Database;
import com.muzone.mucore.util.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MuCore extends JavaPlugin {
    private static MuCore instance;

    // Managers
    private ConfigManager configManager;
    private DataManager dataManager;      // Database Connection Manager
    private PlayerManager playerManager;  // Player Data Cache
    private ActionManager actionManager;  // Punishment System
    private CheckManager checkManager;    // Module Registry

    @Override
    public void onEnable() {
        instance = this;

        // 1. Load Configurations
        this.configManager = new ConfigManager(this);

        // 2. Initialize Database (PENTING: Sebelum manager lain)
        this.dataManager = new DataManager(this);

        // 3. Initialize Logic Managers
        this.playerManager = new PlayerManager();
        this.actionManager = new ActionManager(this);
        this.checkManager = new CheckManager(this);

        // 4. Initialize Bridges (Support for Bedrock & Packet Events)
        GeyserBridge.init(); // Deteksi Geyser/Floodgate
        new ProtocolLibBridge(this).register(); // Hook ke ProtocolLib

        // 5. Register Checks (Daftarkan semua modul deteksi di sini)
        // Packet Limiter (Anti Crash)
        checkManager.register(new PacketLimiterCheck());
        
        // Movement Checks
        checkManager.register(new FlyCheck());
        checkManager.register(new SpeedCheck());
        checkManager.register(new OmniSprintCheck());
        
        // Combat Checks
        checkManager.register(new KillAuraCheck());
        checkManager.register(new ReachCheck());

        // 6. Register Commands
        getCommand("mucore").setExecutor(new MuCommand(this));

        getLogger().info("MUCORE AntiCheat (Enterprise Edition) has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Tutup koneksi database dengan aman saat reload/stop
        if (dataManager != null) {
            dataManager.shutdown();
        }
        getLogger().info("MUCORE AntiCheat disabled.");
    }

    // --- Public Getters (API) ---
    
    public static MuCore getInstance() { return instance; }
    
    public ConfigManager getConfigManager() { return configManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public ActionManager getActionManager() { return actionManager; }
    public CheckManager getCheckManager() { return checkManager; }

    // Helper method untuk mempermudah akses database dari Check.java
    public Database getDatabase() {
        if (dataManager == null) return null;
        return dataManager.getDatabase();
    }
}