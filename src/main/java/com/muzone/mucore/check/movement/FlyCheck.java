package com.muzone.mucore.check.movement;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.MuCore;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType; // Import Potion yang benar

public class FlyCheck extends Check {

    public FlyCheck() {
        super("Fly", "Detects illegal flight and gravity manipulation");
    }

    @Override
    public void handle(Player player, PlayerData data, PacketEvent event) {
        // 1. FIX: Cek tipe paket secara manual (isFlying sering error di versi baru)
        PacketType type = event.getPacketType();
        if (type != PacketType.Play.Client.POSITION && 
            type != PacketType.Play.Client.POSITION_LOOK && 
            type != PacketType.Play.Client.FLYING && 
            type != PacketType.Play.Client.LOOK) {
            return;
        }

        // 2. SAFETY: Cek apakah paket punya koordinat?
        // Paket "FLYING" murni atau "LOOK" kadang tidak punya X/Y/Z, kalau dibaca akan error.
        if (event.getPacket().getDoubles().size() < 3) return;

        double x = event.getPacket().getDoubles().read(0);
        double y = event.getPacket().getDoubles().read(1);
        double z = event.getPacket().getDoubles().read(2);
        boolean onGround = event.getPacket().getBooleans().read(0);

        double lastY = data.getLastY();
        double deltaY = y - lastY;

        // --- CORE LOGIC ---

        if (deltaY > 0 && !player.isFlying() && !player.isGliding() && !onGround) {
            double limit = 0.5; // Batas lompatan normal

            // 3. FIX: Ganti JUMP menjadi JUMP_BOOST (Sesuai API 1.21)
            if (player.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
                limit += 0.1 * (player.getPotionEffect(PotionEffectType.JUMP_BOOST).getAmplifier() + 1);
            }

            // Toleransi Bedrock
            if (data.isBedrock()) {
                limit += 0.2;
            }

            if (deltaY > limit) {
                // 4. REFACTOR: Gunakan method fail() dari Check.java
                // Ini otomatis urus Database, VL, dan ActionManager (Kick/Ban)
                fail(player, data, "Abnormal vertical movement: " + String.format("%.2f", deltaY));

                // Logic Setback (Tarik Balik)
                if (data.getVL() > 10) {
                    Location ground = player.getLocation();
                    // Pastikan lastSafeY valid, jika 0 pakai Y sekarang
                    double safeY = (data.getLastSafeY() > 0) ? data.getLastSafeY() : y;
                    ground.setY(safeY); 
                    
                    MuCore.getInstance().getServer().getScheduler().runTask(MuCore.getInstance(), () -> {
                        player.teleport(ground);
                    });
                }
            }
        }

        // Simpan posisi untuk cek berikutnya
        data.setLastY(y);
        
        // Simpan Safe Y jika pemain menyentuh tanah (untuk titik setback)
        if (onGround) {
            data.setLastSafeY(y);
        }
    }
}