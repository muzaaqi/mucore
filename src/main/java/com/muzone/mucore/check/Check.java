package com.muzone.mucore.check;

import com.muzone.mucore.data.PlayerData;
import org.bukkit.entity.Player;

public abstract class Check {
    private final String name;
    private final String description;

    public Check(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    
    // Logika deteksi akan ditulis di sini oleh masing-masing modul
    public abstract void handle(Player player, PlayerData data, Object packet, PacketEvent event);

    /**
     * Method sentral untuk menangani pelanggaran.
     * Otomatis menambah VL, Log Database, dan Cek Hukuman.
     */
    protected void fail(Player player, PlayerData data, String details) {
        // 1. Tambah VL
        data.addViolation(1.0);
        
        // 2. Simpan ke Database (Async)
        MuCore.getInstance().getDatabase().saveViolation(
            player.getUniqueId().toString(),
            this.name,
            data.getVL(),
            details
        );
        
        // 3. Evaluasi Hukuman (Kick/Ban/Alert)
        MuCore.getInstance().getActionManager().evaluate(player, this.name, data.getVL());
    }
}