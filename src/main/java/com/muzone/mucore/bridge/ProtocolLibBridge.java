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
    // KITA GANTI NAMA VARIABEL JADI 'core' AGAR TIDAK BENTROK
    private final MuCore core;

    public ProtocolLibBridge(MuCore core) {
        this.core = core;
    }

    public void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                core, ListenerPriority.HIGHEST, PacketType.Play.Client.getInstance().values()) {
            
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer() == null) return;
                
                // Gunakan 'core' di sini, bukan 'plugin'
                // Karena 'plugin' di dalam sini dianggap sebagai Generic Plugin bawaan ProtocolLib
                PlayerData data = core.getPlayerManager().getData(event.getPlayer());
                
                if (data == null) return;

                // Gunakan 'core' lagi di sini
                for (Check check : core.getCheckManager().getChecks()) {
                    try {
                        check.handle(event.getPlayer(), data, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}