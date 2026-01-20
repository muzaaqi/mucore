package com.muzone.mucore.check.movement;

import com.comphenix.protocol.PacketType;
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
        // FIX: Cek manual tipe paket (pengganti isFlying)
        PacketType type = event.getPacketType();
        if (type != PacketType.Play.Client.POSITION && 
            type != PacketType.Play.Client.POSITION_LOOK) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE || player.isFlying() || player.isGliding()) return;

        double x = event.getPacket().getDoubles().read(0);
        double z = event.getPacket().getDoubles().read(2);
        
        double deltaX = x - data.getLastX();
        double deltaZ = z - data.getLastZ();
        double deltaXZ = Math.hypot(deltaX, deltaZ); 

        double limit = 0.34; 

        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int amplifier = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
            limit += 0.06 * (amplifier + 1);
        }

        if (!event.getPacket().getBooleans().read(0)) { 
            limit += 0.3; 
        }

        if (data.isBedrock()) {
            limit += 0.15; 
        }

        if (deltaXZ > limit && data.getLastDeltaXZ() > limit) {
            String detail = String.format("Speed: %.2f (Limit: %.2f)", deltaXZ, limit);
            fail(player, data, detail);
            
            if (data.getVL() > 5) {
                event.setCancelled(true);
                MuCore.getInstance().getServer().getScheduler().runTask(MuCore.getInstance(), () -> {
                    player.teleport(player.getLocation()); 
                });
            }
        }

        data.setLastDeltaXZ(deltaXZ);
        data.updateLocation(x, data.getLastY(), z, data.getLastYaw());
    }
}