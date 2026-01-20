package com.muzone.mucore.check.combat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.muzone.mucore.MuCore;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PlayerData;
import com.muzone.mucore.util.MathUtil; // Pastikan MathUtil diimport
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class ReachCheck extends Check {

    public ReachCheck() {
        super("Reach", "Detects hitting entities from impossible distances");
    }

    @Override
    public void handle(Player player, PlayerData data, PacketEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.USE_ENTITY) return;

        PacketContainer packet = event.getPacket();
        
        // Hanya cek saat menyerang (ATTACK)
        EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);
        if (action != EnumWrappers.EntityUseAction.ATTACK) return;

        Entity target = packet.getEntityModifier(event).read(0);
        if (target == null) return;

        // Bypass untuk Creative Mode (Reach bawaan Creative adalah ~5.0 blok)
        if (player.getGameMode() == GameMode.CREATIVE) return;

        // --- MATH CALCULATION ---
        
        // 1. Dapatkan posisi mata pemain (Eye Location)
        Vector eyeLocation = player.getEyeLocation().toVector();
        
        // 2. Dapatkan Hitbox (Bounding Box) target
        // Kita menggunakan BoundingBox agar perhitungan akurat ke "kulit" entity
        BoundingBox targetBox = target.getBoundingBox();
        
        // 3. Cari titik terdekat di hitbox target dari mata pemain
        // Ini menghindari false positive saat memukul kaki vs kepala
        double distance = getDistanceToBox(eyeLocation, targetBox);

        // --- THRESHOLD LOGIC ---
        
        // Batas Vanilla Survival = 3.0 blok
        double limit = 3.0;

        // Toleransi Lag & Movement (Ping Buffer)
        // Semakin tinggi ping, semakin besar kemungkinan posisi tidak sinkron
        int ping = player.getPing();
        double buffer = 0.4 + (ping * 0.002); // 0.002 per ms ping

        limit += buffer;

        // HYBRID: Bedrock Handling
        // Bedrock memiliki reach yang sedikit berbeda dan packet delay
        if (data.isBedrock()) {
            limit += 0.5; // Tambahan 0.5 blok untuk toleransi Touch Control
        }

        // --- JUDGMENT ---
        if (distance > limit) {
            String details = String.format("Range: %.2f (Limit: %.2f)", distance, limit);
            
            data.addViolation(1.0);
            
            // Log & Save
            MuCore.getInstance().getDatabase().saveViolation(
                player.getUniqueId().toString(), 
                "Reach", 
                data.getVL(), 
                details
            );

            // Cancel Hit (Mencegah damage masuk)
            // Hanya cancel jika pelanggaran sangat jelas (> limit + 0.2)
            if (distance > limit + 0.2) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Menghitung jarak terdekat dari sebuah titik (point) ke kotak (box).
     * Jika titik ada di dalam kotak, jaraknya 0.
     */
    private double getDistanceToBox(Vector point, BoundingBox box) {
        double dx = Math.max(0, Math.max(box.getMinX() - point.getX(), point.getX() - box.getMaxX()));
        double dy = Math.max(0, Math.max(box.getMinY() - point.getY(), point.getY() - box.getMaxY()));
        double dz = Math.max(0, Math.max(box.getMinZ() - point.getZ(), point.getZ() - box.getMaxZ()));
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}