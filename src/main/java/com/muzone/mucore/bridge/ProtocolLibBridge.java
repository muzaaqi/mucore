package com.muzone.mucore.bridge;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.muzone.mucore.MuCore;
import com.muzone.mucore.check.Check;
import com.muzone.mucore.data.PlayerData;
import org.bukkit.GameMode; // Tambahan
import org.bukkit.entity.Player; // Tambahan

public class ProtocolLibBridge {
    private final MuCore core;

    public ProtocolLibBridge(MuCore core) {
        this.core = core;
    }

    public void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                core, ListenerPriority.HIGHEST, PacketType.Play.Client.getInstance().values()) {
            
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null) return;
                
                // --- INTEGRASI LUCKPERMS / PERMISSION ---
                // Jika pemain punya permission bypass, jangan dicek sama sekali.
                // Admin/Staff tidak akan kena kick.
                if (player.hasPermission("mucore.bypass")) return;

                // Optimasi: Jangan cek pemain Creative/Spectator
                if (player.getGameMode() == GameMode.CREATIVE || 
                    player.getGameMode() == GameMode.SPECTATOR) return;

                PlayerData data = core.getPlayerManager().getData(player);
                if (data == null) return;

                for (Check check : core.getCheckManager().getChecks()) {
                    // Fitur Bypass Per-Check (Opsional)
                    // Contoh: mucore.bypass.fly
                    if (player.hasPermission("mucore.bypass." + check.getName().toLowerCase())) continue;

                    try {
                        check.handle(player, data, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}