package com.muzone.mucore.data;

import com.muzone.mucore.data.PacketTracker;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final PacketTracker packetTracker;
    
    // Cache Posisi
    private double lastX, lastY, lastZ;
    private float lastYaw;
    private double lastDeltaXZ;
    
    // Safety
    private double lastSafeY;
    
    // Status
    private boolean alertsEnabled = true;
    private double violationLevel = 0.0;
    private boolean isBedrock = false; // Akan di-set oleh GeyserBridge

    // Combat
    private long lastAttackTime;
    private int attackCount;
    private long lastCpsReset;

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.packetTracker = new PacketTracker();
        this.lastSafeY = player.getLocation().getY();
        this.lastAttackTime = System.currentTimeMillis();
        this.lastCpsReset = System.currentTimeMillis();
        updateLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw());
    }

    public void updateLocation(double x, double y, double z, float yaw) {
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.lastYaw = yaw;
        // Update SafeY jika di tanah (bisa dikembangkan nanti)
    }

    public void addViolation(double amount) {
        this.violationLevel += amount;
    }

    // --- GETTERS & SETTERS ---
    
    public UUID getUuid() { return uuid; }
    public PacketTracker getPacketTracker() { return packetTracker; }
    
    public double getLastX() { return lastX; }
    public double getLastY() { return lastY; }
    public double getLastZ() { return lastZ; }
    public float getLastYaw() { return lastYaw; }
    
    public double getLastDeltaXZ() { return lastDeltaXZ; }
    public void setLastDeltaXZ(double delta) { this.lastDeltaXZ = delta; }
    
    public double getLastSafeY() { return lastSafeY; }
    public void setLastSafeY(double y) { this.lastSafeY = y; } // Tambahkan Setter ini!
    public void setLastY(double y) { this.lastY = y; } // Tambahkan Setter ini!

    public boolean isAlertsEnabled() { return alertsEnabled; }
    public void setAlertsEnabled(boolean enabled) { this.alertsEnabled = enabled; }
    
    public double getVL() { return violationLevel; }
    
    public boolean isBedrock() { return isBedrock; }
    public void setBedrock(boolean bedrock) { this.isBedrock = bedrock; }

    public long getLastAttackTime() { return lastAttackTime; }
    public void setLastAttackTime(long time) { this.lastAttackTime = time; }
    
    public int getAttackCount() { return attackCount; }
    public void incrementAttackCount() { this.attackCount++; }
    public void resetAttackCount() { this.attackCount = 0; }
    
    public long getLastCpsReset() { return lastCpsReset; }
    public void setLastCpsReset(long time) { this.lastCpsReset = time; }
}