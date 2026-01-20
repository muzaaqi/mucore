package com.muzone.mucore.data;

import com.muzone.mucore.MuCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager implements Listener {
    
    // ConcurrentHashMap agar aman diakses thread lain (Async Check)
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PlayerManager() {
        // Register event listener untuk Join/Quit
        Bukkit.getPluginManager().registerEvents(this, MuCore.getInstance());
    }

    public PlayerData getData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }
    
    public PlayerData getData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Buat data baru saat pemain masuk
        PlayerData data = new PlayerData(event.getPlayer());
        playerDataMap.put(event.getPlayer().getUniqueId(), data);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Hapus data dari RAM saat keluar agar hemat memory
        playerDataMap.remove(event.getPlayer().getUniqueId());
        
        // TODO: Disini nanti kita bisa simpan data terakhir ke Database
    }
}