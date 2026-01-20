package com.muzone.mucore.check.movement;

import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FlyCheck extends Check {

    public FlyCheck() {
        super("Fly", "Detects illegal flight and gravity manipulation");
    }

    @Override
    public void handle(Player player, PlayerData data, PacketEvent event) {
        // Kita hanya memproses paket pergerakan
        if (!event.getPacketType().isFlying()) return;

        // Dapatkan data posisi dari paket (bukan dari Bukkit API untuk akurasi raw)
        double x = event.getPacket().getDoubles().read(0);
        double y = event.getPacket().getDoubles().read(1);
        double z = event.getPacket().getDoubles().read(2);
        boolean onGround = event.getPacket().getBooleans().read(0);

        // Ambil lokasi terakhir dari PlayerData (Cache)
        double lastY = data.getLastY();
        
        // Hitung selisih vertikal (Delta Y)
        double deltaY = y - lastY;

        // --- CORE LOGIC ---
        
        // Jika pemain naik (DeltaY > 0) padahal server bilang dia tidak di tanah
        // Dan tidak sedang terbang (Creative/Elytra)
        if (deltaY > 0 && !player.isFlying() && !player.isGliding() && !onGround) {
            
            // Prediksi ambang batas (Threshold)
            // Di Minecraft, lompatan biasa max sekitar 0.42 blok
            double limit = 0.5; 
            
            // Efek Potion Jump Boost menambah limit
            if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP)) {
                limit += 0.1 * (player.getPotionEffect(org.bukkit.potion.PotionEffectType.JUMP).getAmplifier() + 1);
            }

            // HYBRID ADJUSTMENT: Toleransi untuk Bedrock
            if (data.isBedrock()) {
                limit += 0.2; // Tambahan toleransi 0.2 block untuk lag kompensasi Geyser
            }

            if (deltaY > limit) {
                // Suspicious!
                handleViolation(player, data, "Abnormal vertical movement: " + String.format("%.2f", deltaY));
            }
        }

        // Simpan posisi sekarang untuk pengecekan paket berikutnya
        data.setLastY(y);
    }

    private void handleViolation(Player player, PlayerData data, String details) {
        data.addViolation(1.0);
        
        // Simpan ke Database yang baru kita buat
        MuCore.getInstance().getDatabase().saveViolation(
            player.getUniqueId().toString(), 
            "Fly", 
            data.getVL(), 
            details
        );

        // Alert Admin & Tindakan (Setback)
        if (data.getVL() > 10) {
            // Teleport pemain ke tanah (Setback simple)
            Location ground = player.getLocation();
            ground.setY(data.getLastSafeY()); // Asumsi kita menyimpan LastSafeY di PlayerData
            
            // Main Thread Task untuk teleport
            MuCore.getInstance().getServer().getScheduler().runTask(MuCore.getInstance(), () -> {
                player.teleport(ground);
            });
        }
    }
}