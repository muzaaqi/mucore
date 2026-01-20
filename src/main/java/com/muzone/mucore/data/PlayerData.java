package com.muzone.mucore.data;

import com.muzone.mucore.bridge.GeyserBridge;
import org.bukkit.entity.Player;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final boolean isBedrock;
    private double violationLevel = 0;
    
    // Movement cache
    private double lastPosX, lastPosY, lastPosZ;
    
    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.isBedrock = GeyserBridge.isBedrock(uuid);
    }

    public void addViolation(double amount) { this.violationLevel += amount; }
    public boolean isBedrock() { return isBedrock; }
    public double getVL() { return violationLevel; }
    // Getter & Setter lainnya...
}