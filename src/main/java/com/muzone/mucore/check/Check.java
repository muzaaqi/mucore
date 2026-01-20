package com.muzone.mucore.check;

import com.comphenix.protocol.events.PacketEvent; // Import Penting
import com.muzone.mucore.MuCore; // Import Penting
import com.muzone.mucore.data.PlayerData; // Import Penting
import org.bukkit.entity.Player;

public abstract class Check {
    private final String name;
    private final String description;

    public Check(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }

    // Abstract method yang wajib diikuti subclass
    public abstract void handle(Player player, PlayerData data, PacketEvent event);

    protected void fail(Player player, PlayerData data, String details) {
        data.addViolation(1.0);
        
        // Akses database via MuCore instance
        if (MuCore.getInstance().getDatabase() != null) {
            MuCore.getInstance().getDatabase().saveViolation(
                player.getUniqueId().toString(),
                this.name,
                data.getVL(),
                details
            );
        }
        
        MuCore.getInstance().getActionManager().evaluate(player, this.name, data.getVL());
    }
}