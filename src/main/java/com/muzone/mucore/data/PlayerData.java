public class PlayerData {
    // ... variable yang sudah ada sebelumnya ...

    private double lastDeltaXZ; // Kecepatan horizontal terakhir
    private long lastFlyingPacket; // Timestamp paket terakhir (untuk timer check)
    private boolean alertsEnabled = true; // Untuk command manager nanti

    // Cache posisi (X, Y, Z, Yaw)
    private double lastX, lastY, lastZ;
    private float lastYaw;

    // ... constructor ...

    // Method untuk update posisi setiap kali paket diterima
    public void updateLocation(double x, double y, double z, float yaw) {
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.lastYaw = yaw;
        this.lastFlyingPacket = System.currentTimeMillis();
    }
    
    // Getter & Setter
    public double getLastDeltaXZ() { return lastDeltaXZ; }
    public void setLastDeltaXZ(double delta) { this.lastDeltaXZ = delta; }
    public boolean isAlertsEnabled() { return alertsEnabled; }
    public void setAlertsEnabled(boolean enabled) { this.alertsEnabled = enabled; }
    
    // ... getter lainnya ...
}