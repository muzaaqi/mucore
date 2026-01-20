package com.muzone.mucore.bridge;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.MuCore;
import com.muzone.mucore.check.packet.PacketLimiterCheck;
import com.muzone.mucore.data.PlayerData;

public class ProtocolLibBridge {
    private final MuCore plugin;
    private final PacketLimiterCheck packetLimiter;

    public ProtocolLibBridge(MuCore plugin) {
        this.plugin = plugin;
        this.packetLimiter = new PacketLimiterCheck();
    }

    public void register() {
        // Kita mendengarkan SEMUA paket dari client
        // Priority HIGHEST berarti kita memproses paling awal sebelum plugin lain
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin, ListenerPriority.HIGHEST, PacketType.Play.Client.getInstance().values()) {
            
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer() == null) return;
                
                PlayerData data = plugin.getPlayerManager().getData(event.getPlayer());
                if (data == null) return;

                // Jalankan Packet Limiter
                packetLimiter.handle(event.getPlayer(), data, event);
            }
        });
    }
}