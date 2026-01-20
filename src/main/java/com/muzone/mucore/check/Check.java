package com.muzone.mucore.check;

import com.muzone.mucore.data.PlayerData;
import org.bukkit.entity.Player;

public abstract class Check {
    private final String name;
    private final String description;

    public Check(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    
    // Logika deteksi akan ditulis di sini oleh masing-masing modul
    public abstract void handle(Player player, PlayerData data, Object packet);
}