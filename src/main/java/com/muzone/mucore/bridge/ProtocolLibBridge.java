package com.muzone.mucore.bridge;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.MuCore;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PlayerData;

public class ProtocolLibBridge {
    private final MuCore plugin;

    public ProtocolLibBridge(MuCore plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // Mendengarkan SEMUA paket dari client
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin, ListenerPriority.HIGHEST, PacketType.Play.Client.getInstance().values()) {
            
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer() == null) return;
                
                // Ambil data pemain (Cache)
                PlayerData data = plugin.getPlayerManager().getData(event.getPlayer());
                if (data == null) return;

                // Loop semua module check yang terdaftar
                // Sistem ini membuat penambahan fitur baru sangat mudah
                for (Check check : plugin.getCheckManager().getChecks()) {
                    try {
                        check.handle(event.getPlayer(), data, event);
                    } catch (Exception e) {
                        // Mencegah satu error di modul merusak modul lain
                        plugin.getLogger().warning("Error in check " + check.getName() + ": " + e.getMessage());
                        e.printStackTrace(); 
                    }
                }
            }
        });
    }
}