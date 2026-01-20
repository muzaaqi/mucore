package com.muzone.mucore.check.movement;

import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.MuCore;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
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
        double deltaXZ = Math.hypot(deltaX, deltaZ); // Rumus Pitagoras

        // 3. Kalkulasi Limit Kecepatan (Prediction Engine)
        double limit = 0.34; // Base sprint speed vanilla + toleransi kecil

        // Tambahan toleransi jika kena efek Speed
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int amplifier = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
            limit += 0.06 * (amplifier + 1);
        }

        // Tambahan jika melompat (Ice/Air friction)
        if (!event.getPacket().getBooleans().read(0)) { // If isAir
            limit += 0.3; // Momentum lompat
        }

        // HYBRID: Bedrock Handling
        if (data.isBedrock()) {
            limit += 0.15; // Geyser movement sering "bursty" atau tidak stabil
        }

        // 4. Bandingkan Realita vs Limit
        // Kita juga cek apakah deltaXZ konsisten tinggi (bukan lag spike sesaat)
        if (deltaXZ > limit && data.getLastDeltaXZ() > limit) {
            
            // Output detail untuk debug admin
            String detail = String.format("Speed: %.2f (Limit: %.2f)", deltaXZ, limit);
            
            data.addViolation(1.0);
            MuCore.getInstance().getDatabase().saveViolation(player.getUniqueId().toString(), "Speed", data.getVL(), detail);
            
            // Alert Admin
            // (Nanti akan ditangani oleh sistem Alert)
            
            // Setback (Tarik mundur pemain)
            if (data.getVL() > 15) {
                event.setCancelled(true);
                // Teleport dilakukan via Scheduler utama agar thread safe
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