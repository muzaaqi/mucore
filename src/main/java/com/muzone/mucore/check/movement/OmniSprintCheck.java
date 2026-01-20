package com.muzone.mucore.check.movement;

import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.MuCore;
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
        if (!event.getPacketType().isFlying()) return;
        
        // Bedrock Player bisa sprint 360 derajat karena controller analog
        // Jadi kita SKIP check ini untuk Bedrock agar tidak false positive
        if (data.isBedrock()) return; 

        if (!player.isSprinting()) return;

        double deltaX = event.getPacket().getDoubles().read(0) - data.getLastX();
        double deltaZ = event.getPacket().getDoubles().read(2) - data.getLastZ();
        
        // Lewati jika pergerakan sangat kecil
        if (Math.hypot(deltaX, deltaZ) < 0.2) return;

        // Hitung arah gerakan (Move Yaw)
        double moveYaw = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        
        // Ambil arah pandangan pemain (Face Yaw)
        float faceYaw = event.getPacket().getFloat().read(0);

        // Hitung selisih sudut (Delta Angle)
        double diff = Math.abs(MathUtil.getAngleDifference(moveYaw, faceYaw));

        // Vanilla: Max sudut sprint sekitar 60 derajat (depan kiri - depan kanan)
        // OmniSprint Meteor biasanya 90-180 derajat (samping/belakang)
        if (diff > 62.0) {
            data.addViolation(1.0);
            MuCore.getInstance().getDatabase().saveViolation(
                player.getUniqueId().toString(), "OmniSprint", data.getVL(), "Angle: " + String.format("%.2f", diff)
            );
            
            // Batalkan sprint mereka
            player.setSprinting(false);
        }
    }
}