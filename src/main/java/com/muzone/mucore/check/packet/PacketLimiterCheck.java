package com.muzone.mucore.check.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.MuCore;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PacketTracker;
import com.muzone.mucore.data.PlayerData;
import org.bukkit.entity.Player;

public class PacketLimiterCheck extends Check {

    public PacketLimiterCheck() {
        super("PacketLimiter", "Prevents server crashes and lag machines");
    }

    @Override
    public void handle(Player player, PlayerData data, PacketEvent event) {
        PacketTracker tracker = data.getPacketTracker();
        tracker.incrementAndCheckReset();
        
        boolean isBedrock = data.isBedrock();
        int globalLimit = MuCore.getInstance().getConfig().getInt(
            "checks.packet_limiter.global_limit." + (isBedrock ? "bedrock" : "java")
        );

        int currentGlobal = tracker.incrementGlobal();
        if (currentGlobal > globalLimit) {
            event.setCancelled(true);
            return;
        }

        PacketType type = event.getPacketType();
        String configKey = null;

        if (type == PacketType.Play.Client.WINDOW_CLICK) configKey = "WINDOW_CLICK";
        else if (type == PacketType.Play.Client.CUSTOM_PAYLOAD) configKey = "CUSTOM_PAYLOAD";
        
        // FIX: Cek manual Flying Packet (isFlying dihapus)
        else if (type == PacketType.Play.Client.FLYING || 
                type == PacketType.Play.Client.POSITION || 
                type == PacketType.Play.Client.LOOK || 
                type == PacketType.Play.Client.POSITION_LOOK) {
            configKey = "FLYING";
        }

        if (configKey != null) {
            int specificLimit = MuCore.getInstance().getConfig().getInt(
                "checks.packet_limiter.specific_limits." + configKey + "." + (isBedrock ? "bedrock" : "java")
            );
            
            int currentSpecific = tracker.incrementSpecific(type);
            
            if (currentSpecific > specificLimit) {
                if (isBedrock && configKey.equals("FLYING") && currentSpecific < specificLimit + 20) {
                    return; 
                }
                
                fail(player, data, "Spamming " + configKey);
                event.setCancelled(true);
            }
        }
    }
}