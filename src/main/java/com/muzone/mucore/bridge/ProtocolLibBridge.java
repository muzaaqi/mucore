package com.muzone.mucore.bridge;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.MuCore;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ProtocolLibBridge {
    private final MuCore core;

    public ProtocolLibBridge(MuCore core) {
        this.core = core;
    }

    public void register() {
        // HAPUS 'FLYING' AGAR TIDAK ADA WARNING
        List<PacketType> packetsToListen = Arrays.asList(
            // Movement
            PacketType.Play.Client.POSITION,
            PacketType.Play.Client.POSITION_LOOK,
            PacketType.Play.Client.LOOK,
            PacketType.Play.Client.ENTITY_ACTION, 
            
            // Combat
            PacketType.Play.Client.USE_ENTITY,
            PacketType.Play.Client.ARM_ANIMATION,
            
            // Interaction/Exploits
            PacketType.Play.Client.WINDOW_CLICK,
            PacketType.Play.Client.CUSTOM_PAYLOAD,
            PacketType.Play.Client.BLOCK_DIG,
            PacketType.Play.Client.BLOCK_PLACE
        );

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                core, ListenerPriority.HIGHEST, packetsToListen) {
            
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null) return;
                
                if (player.hasPermission("mucore.bypass")) return;

                if (player.getGameMode() == GameMode.CREATIVE || 
                    player.getGameMode() == GameMode.SPECTATOR) return;

                PlayerData data = core.getPlayerManager().getData(player);
                if (data == null) return;

                for (Check check : core.getCheckManager().getChecks()) {
                    if (player.hasPermission("mucore.bypass." + check.getName().toLowerCase())) continue;

                    try {
                        check.handle(player, data, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        
        // Listener Server Packet (Velocity) tetap dipisah
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                core, ListenerPriority.HIGHEST, PacketType.Play.Server.ENTITY_VELOCITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPlayer() == null) return;
                
                int entityId = event.getPacket().getIntegers().read(0);
                if (entityId != event.getPlayer().getEntityId()) return;

                PlayerData data = core.getPlayerManager().getData(event.getPlayer());
                if (data == null) return;

                double x = event.getPacket().getIntegers().read(1) / 8000.0;
                double y = event.getPacket().getIntegers().read(2) / 8000.0;
                double z = event.getPacket().getIntegers().read(3) / 8000.0;

                data.setVelocity(x, y, z);
            }
        });
    }
}