package com.muzone.mucore.check.movement;

import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.MuCore;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class SpeedCheck extends Check {

    public SpeedCheck() {
        super("Speed", "Detects horizontal movement exceeding vanilla limits");
    }

    @Override
    public void handle(Player player, PlayerData data, PacketEvent event) {
        if (!event.getPacketType().isFlying()) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.isFlying() || player.isGliding()) return;

        // 1. Ambil data koordinat RAW dari paket
        double x = event.getPacket().getDoubles().read(0);
        double z = event.getPacket().getDoubles().read(2);
        
        // 2. Hitung Delta XZ (Jarak Horizontal)
        double deltaX = x - data.getLastX();
        double deltaZ = z - data.getLastZ();
        double deltaXZ = Math.hypot(deltaX, deltaZ); 

        // 3. Kalkulasi Limit Kecepatan (Prediction Engine)
        double limit = 0.34; // Base sprint speed

        // Tambahan toleransi jika kena efek Speed
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int amplifier = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
            limit += 0.06 * (amplifier + 1);
        }

        // Tambahan jika melompat (Ice/Air friction)
        if (!event.getPacket().getBooleans().read(0)) { // If isAir
            limit += 0.3; 
        }

        // HYBRID: Bedrock Handling
        if (data.isBedrock()) {
            limit += 0.15; 
        }

        // 4. Bandingkan Realita vs Limit
        if (deltaXZ > limit && data.getLastDeltaXZ() > limit) {
            
            // Siapkan pesan detail untuk log
            String detail = String.format("Speed: %.2f (Limit: %.2f)", deltaXZ, limit);
            
            // --- REFACTOR: Panggil method fail() ---
            // Method ini otomatis:
            // 1. Tambah Violation Level (VL)
            // 2. Simpan ke Database
            // 3. Cek ActionManager (Kick/Ban/Alert otomatis)
            fail(player, data, detail);
            
            // --- Setback Logic (Pencegahan Fisik) ---
            // Kita tetap butuh setback manual di sini agar cheater tidak bisa maju
            if (data.getVL() > 5) { // VL 5 cukup untuk mulai teleport balik (Rubberband)
                event.setCancelled(true);
                
                // Teleport harus di Main Thread
                MuCore.getInstance().getServer().getScheduler().runTask(MuCore.getInstance(), () -> {
                    player.teleport(player.getLocation()); 
                });
            }
        }

        // Simpan data untuk tick selanjutnya
        data.setLastDeltaXZ(deltaXZ);
        data.updateLocation(x, data.getLastY(), z, data.getLastYaw());
    }
}