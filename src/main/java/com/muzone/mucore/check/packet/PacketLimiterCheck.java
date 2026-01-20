package com.muzone.mucore.check.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.MuCore;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PlayerData;
import com.muzone.mucore.data.PacketTracker;
import org.bukkit.entity.Player;

public class PacketLimiterCheck extends Check {

    public PacketLimiterCheck() {
        super("PacketLimiter", "Prevents server crashes and lag machines");
    }

    @Override
    public void handle(Player player, PlayerData data, PacketEvent event) {
        PacketTracker tracker = data.getPacketTracker();
        boolean isReset = tracker.incrementAndCheckReset();
        
        // 1. Dapatkan Limit dari Config
        // (Dalam production, cache nilai config ini di variabel static/memory agar cepat)
        boolean isBedrock = data.isBedrock();
        int globalLimit = MuCore.getInstance().getConfig().getInt(
            "checks.packet_limiter.global_limit." + (isBedrock ? "bedrock" : "java")
        );

        // 2. Global Check (Total paket per detik)
        int currentGlobal = tracker.incrementGlobal();
        if (currentGlobal > globalLimit) {
            cancelAndPunish(player, data, event, "Global Packet Spam (Rate: " + currentGlobal + "/s)");
            return;
        }

        // 3. Specific Check (Untuk paket berisiko tinggi)
        PacketType type = event.getPacketType();
        String configKey = null;

        if (type == PacketType.Play.Client.WINDOW_CLICK) configKey = "WINDOW_CLICK";
        else if (type == PacketType.Play.Client.CUSTOM_PAYLOAD) configKey = "CUSTOM_PAYLOAD";
        else if (type.isFlying()) configKey = "FLYING"; // Covers Position, Look, Pos+Look

        if (configKey != null) {
            int specificLimit = MuCore.getInstance().getConfig().getInt(
                "checks.packet_limiter.specific_limits." + configKey + "." + (isBedrock ? "bedrock" : "java")
            );
            
            int currentSpecific = tracker.incrementSpecific(type);
            
            if (currentSpecific > specificLimit) {
                // Khusus Bedrock: Flying packet sering dikirim dalam batch, kita beri sedikit toleransi
                if (isBedrock && configKey.equals("FLYING") && currentSpecific < specificLimit + 20) {
                    return; 
                }
                
                cancelAndPunish(player, data, event, "Spamming " + configKey);
            }
        }
    }

    private void cancelAndPunish(Player player, PlayerData data, PacketEvent event, String reason) {
        // Batalkan paket agar server tidak crash
        event.setCancelled(true);
        
        // Tambah Violation Level
        data.addViolation(1.0);
        
        // Log ke console (Async log disarankan)
        MuCore.getInstance().getLogger().warning(
            "[MuCore/Exploit] Blocked packet from " + player.getName() + ": " + reason
        );

        // Kick jika sudah keterlaluan (Mencegah Lag Machine terus menerus)
        if (data.getVL() > MuCore.getInstance().getConfig().getInt("checks.packet_limiter.cancel_threshold_vl")) {
            // Jalankan kick di main thread karena ProtocolLib berjalan async
            MuCore.getInstance().getServer().getScheduler().runTask(MuCore.getInstance(), () -> {
                player.kickPlayer("§c[MuCore] \n§fConnection limits exceeded.\n§7Stop using exploits.");
            });
        }
    }
}