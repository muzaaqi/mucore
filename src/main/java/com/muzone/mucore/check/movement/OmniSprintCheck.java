package com.muzone.mucore.check.movement;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PlayerData;
import com.muzone.mucore.util.MathUtil;
import org.bukkit.entity.Player;

public class OmniSprintCheck extends Check {

    public OmniSprintCheck() {
        super("OmniSprint", "Prevents sprinting in impossible directions");
    }

    @Override
    public void handle(Player player, PlayerData data, PacketEvent event) {
        // FIX: Cek manual tipe paket
        PacketType type = event.getPacketType();
        if (type != PacketType.Play.Client.POSITION && 
            type != PacketType.Play.Client.POSITION_LOOK) return;
        
        if (data.isBedrock()) return; 
        if (!player.isSprinting()) return;

        double deltaX = event.getPacket().getDoubles().read(0) - data.getLastX();
        double deltaZ = event.getPacket().getDoubles().read(2) - data.getLastZ();
        
        if (Math.hypot(deltaX, deltaZ) < 0.2) return;

        double moveYaw = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        
        float faceYaw = event.getPacket().getFloat().read(0);

        // FIX: Casting ke (float) agar MathUtil tidak error
        double diff = Math.abs(MathUtil.getAngleDifference((float) moveYaw, faceYaw));

        if (diff > 62.0) {
            fail(player, data, "Angle: " + String.format("%.2f", diff));
            player.setSprinting(false);
        }
    }
}