package com.muzone.mucore.bridge;

import org.bukkit.Bukkit;
import org.geysermc.floodgate.api.FloodgateApi;
import java.util.UUID;

public class GeyserBridge {

    private static boolean floodgateEnabled = false;

    // Dipanggil saat onEnable di MuCore
    public static void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            floodgateEnabled = true;
        }
    }

    public static boolean isBedrock(UUID uuid) {
        if (!floodgateEnabled) return false;
        try {
            // Cek apakah UUID ini milik pemain Bedrock via Floodgate API
            return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
        } catch (Exception e) {
            return false;
        }
    }
}