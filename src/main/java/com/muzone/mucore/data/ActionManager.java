package com.muzone.mucore.data;

import com.muzone.mucore.MuCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Set;

public class ActionManager {

    private final MuCore plugin;

    public ActionManager(MuCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Mengevaluasi apakah hukuman harus diberikan berdasarkan VL saat ini.
     * @param player Pemain yang melanggar
     * @param checkName Nama module (misal: "KillAura", "Fly")
     * @param vl Violation Level saat ini
     */
    public void evaluate(Player player, String checkName, double vl) {
        // Kita bulatkan VL ke bawah (floor) karena config biasanya integer (10, 20, etc)
        int intVl = (int) vl;
        
        // Cek action spesifik per module dulu
        if (!checkAndExecute(player, "checks." + checkName + ".actions", intVl, vl, checkName)) {
            // Jika tidak ada action spesifik di level ini, cek global actions
            checkAndExecute(player, "global_actions", intVl, vl, checkName);
        }
    }

    private boolean checkAndExecute(Player player, String path, int intVl, double realVl, String checkName) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        if (section == null) return false;

        // Cek apakah ada action untuk VL ini (misal: "20")
        String command = section.getString(String.valueOf(intVl));
        
        if (command != null) {
            executeCommand(player, command, realVl, checkName);
            return true;
        }
        return false;
    }

    private void executeCommand(Player player, String command, double vl, String checkName) {
        // Replace placeholders
        String finalCommand = command
                .replace("{player}", player.getName())
                .replace("{vl}", String.format("%.1f", vl))
                .replace("{check}", checkName)
                .replace("&", "§");

        // Jalankan di Main Thread (Sync) karena Bukkit Command tidak thread-safe
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (finalCommand.startsWith("alert ")) {
                // Khusus alert, kirim ke semua admin, bukan execute console command
                String msg = finalCommand.substring(6); // Hapus kata "alert "
                broadcastAdmin(msg);
            } else {
                // Eksekusi perintah console (Kick, Ban, dll)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            }
        });
    }

    private void broadcastAdmin(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("mucore.alerts")) {
                p.sendMessage(message);
            }
        }
        // Log ke console juga
        plugin.getLogger().info("[Alert] " + ChatColor.stripColor(message));
    }
}