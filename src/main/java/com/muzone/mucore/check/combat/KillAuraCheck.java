package com.muzone.mucore.check.combat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.muzone.mucore.MuCore;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PlayerData;
import com.muzone.mucore.util.MathUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KillAuraCheck extends Check {

    public KillAuraCheck() {
        super("KillAura", "Detects combat hacks (Angle & AutoClicker)");
    }

    @Override
    public void handle(Player player, PlayerData data, PacketEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.USE_ENTITY) return;

        PacketContainer packet = event.getPacket();
        
        // Pastikan ini adalah aksi serangan (ATTACK), bukan interaksi (INTERACT/AT)
        // ProtocolLib menggunakan EnumWrappers untuk kompatibilitas versi
        EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);
        if (action != EnumWrappers.EntityUseAction.ATTACK) return;

        // Dapatkan entity target
        int targetId = packet.getIntegers().read(0);
        Entity target = packet.getEntityModifier(event).read(0);

        if (target == null) return; // Target invalid atau sudah mati

        // --- CHECK 1: AutoClicker / CPS (Clicks Per Second) ---
        long now = System.currentTimeMillis();
        
        // Reset hitungan setiap detik (1000ms)
        if (now - data.getLastCpsReset() > 1000) {
            data.setLastCpsReset(now);
            data.resetAttackCount();
        }
        data.incrementAttackCount();

        int cpsLimit = 15; // Limit wajar manusia
        if (data.isBedrock()) cpsLimit = 18; // Bedrock touch spam bisa lebih cepat

        if (data.getAttackCount() > cpsLimit) {
            data.addViolation(1.0);
            MuCore.getInstance().getDatabase().saveViolation(
                player.getUniqueId().toString(), 
                "AutoClicker", 
                data.getVL(), 
                "CPS: " + data.getAttackCount()
            );
            // Cancel attack jika terlalu brutal
            if (data.getAttackCount() > cpsLimit + 5) {
                event.setCancelled(true);
            }
        }

        // --- CHECK 2: Angle (Arah Pandangan vs Posisi Target) ---
        // KillAura sering memukul tanpa melihat target (Head Snap)
        
        Vector playerLoc = player.getEyeLocation().toVector();
        Vector targetLoc = target.getLocation().toVector();
        
        // Vector arah dari pemain ke target
        Vector directionToTarget = targetLoc.clone().subtract(playerLoc);
        
        // Ubah vector arah menjadi Yaw (Derajat)
        float targetYaw = MathUtil.getYawFromVector(directionToTarget.getX(), directionToTarget.getZ());
        float playerYaw = player.getLocation().getYaw();

        // Hitung selisih sudut
        double angleDiff = MathUtil.getAngleDifference(playerYaw, targetYaw);

        // Ambang batas (Threshold)
        // Manusia biasanya memukul target di depan mereka (sudut kecil)
        double maxAngle = 60.0; // Field of View wajar
        
        if (data.isBedrock()) maxAngle += 15.0; // Toleransi lag rotasi Bedrock

        // Jarak juga berpengaruh, makin dekat makin besar toleransi sudut
        double distance = playerLoc.distance(targetLoc);
        if (distance < 1.0) maxAngle += 20.0;

        if (angleDiff > maxAngle) {
            data.addViolation(1.0);
            MuCore.getInstance().getDatabase().saveViolation(
                player.getUniqueId().toString(), 
                "KillAura (Angle)", 
                data.getVL(), 
                "Diff: " + String.format("%.2f", angleDiff) + "°"
            );
            
            // Batalkan serangan tidak masuk akal (misal: pukul belakang)
            if (angleDiff > 90) { 
                event.setCancelled(true);
            }
        }
        
        data.setLastAttackTime(now);
    }
}