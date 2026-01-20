package com.muzone.mucore.data;

import org.bukkit.entity.Player;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final PacketTracker packetTracker;
    
    // Cache Posisi
    private double lastX, lastY, lastZ;
    private float lastYaw;
    private double lastDeltaXZ;
    
    // Safety & Setback
    private double lastSafeY;
    
    // Status
    private boolean alertsEnabled = true;
    private double violationLevel = 0.0;
    private boolean isBedrock = false;

    // Combat
    private long lastAttackTime;
    private int attackCount;
    private long lastCpsReset;

    // --- VELOCITY TRACKING (Ini yang kurang tadi) ---
    private double velocityX, velocityY, velocityZ;
    private int velocityTicks; 
    private boolean waitingForVelocity;

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
    public void setLastSafeY(double y) { this.lastSafeY = y; }
    public void setLastY(double y) { this.lastY = y; }

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

    // --- VELOCITY METHODS (PENTING) ---
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public double getVelocityZ() { return velocityZ; }
    public int getVelocityTicks() { return velocityTicks; }
    
    public void setVelocity(double x, double y, double z) {
        this.velocityX = x;
        this.velocityY = y;
        this.velocityZ = z;
        // Estimasi durasi knockback (X+Y+Z dibagi rata + 20 ticks toleransi)
        this.velocityTicks = (int) (((Math.abs(x) + Math.abs(y) + Math.abs(z)) / 2) + 20);
        this.waitingForVelocity = true;
    }
    
    public void decrementVelocityTicks() {
        if (velocityTicks > 0) velocityTicks--;
    }
    
    public boolean isWaitingForVelocity() { return waitingForVelocity; }
    public void setWaitingForVelocity(boolean wait) { this.waitingForVelocity = wait; }
}