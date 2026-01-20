package com.muzone.mucore.command;

import com.muzone.mucore.MuCore;
import com.muzone.mucore.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuCommand implements CommandExecutor {

    private final MuCore plugin;

    public MuCommand(MuCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Cek Permission Admin
        if (!sender.hasPermission("mucore.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        // Help Menu (Jika tanpa argumen)
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            // 1. Toggle Alerts (Notifikasi Chat)
            case "alerts":
            case "notify":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command is for players only.");
                    return true;
                }
                toggleAlerts((Player) sender);
                break;

            // 2. Info Player (Fitur Anda - Dipertahankan)
            case "info":
            case "check":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /mucore info <player>");
                    return true;
                }
                checkPlayerInfo(sender, args[1]);
                break;

            // 3. Reload Config (PENTING untuk Webhook)
            case "reload":
                plugin.getConfigManager().reload();
                sender.sendMessage(ChatColor.GREEN + "MuCore configuration and webhooks reloaded successfully.");
                break;
                
            // 4. Status Server (Cek Database & Webhook)
            case "status":
                showStatus(sender);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    // --- LOGIC METHODS ---

    private void toggleAlerts(Player player) {
        PlayerData data = plugin.getPlayerManager().getData(player);
        if (data == null) return;

        boolean newState = !data.isAlertsEnabled();
        data.setAlertsEnabled(newState);
        
        String status = newState ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
        player.sendMessage(ChatColor.AQUA + "MuSentry Alerts: " + status);
    }

    private void checkPlayerInfo(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + targetName + "' not found.");
            return;
        }
        
        PlayerData data = plugin.getPlayerManager().getData(target);
        if (data != null) {
            sender.sendMessage(ChatColor.DARK_AQUA + "--- " + target.getName() + " ---");
            // Deteksi Platform (Java/Bedrock)
            sender.sendMessage(ChatColor.GRAY + "Platform: " + (data.isBedrock() ? ChatColor.GREEN + "Bedrock (Geyser)" : ChatColor.YELLOW + "Java Edition"));
            // Ping Realtime
            sender.sendMessage(ChatColor.GRAY + "Ping: " + ChatColor.WHITE + target.getPing() + "ms");
            // Total Violation
            sender.sendMessage(ChatColor.GRAY + "Total VL: " + ChatColor.RED + String.format("%.1f", data.getVL()));
            // Status Alerts
            sender.sendMessage(ChatColor.GRAY + "Receiving Alerts: " + (data.isAlertsEnabled() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        } else {
            sender.sendMessage(ChatColor.RED + "No data found for this player.");
        }
    }

    private void showStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== MuCore System Status ===");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Database: " + ChatColor.WHITE + plugin.getDatabase().getType());
        
        // Cek Webhook
        boolean webhook = plugin.getConfigManager().getBoolean("webhook.enabled");
        sender.sendMessage(ChatColor.YELLOW + "Webhook: " + (webhook ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        
        // Cek Memory Usage (Opsional, biar keren)
        long freeMem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        sender.sendMessage(ChatColor.YELLOW + "Free Memory: " + ChatColor.WHITE + freeMem + " MB");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "MuSentry (MuCore) Commands:");
        sender.sendMessage(ChatColor.GRAY + "/mucore alerts " + ChatColor.WHITE + "- Toggle notifications");
        sender.sendMessage(ChatColor.GRAY + "/mucore info <player> " + ChatColor.WHITE + "- View player stats");
        sender.sendMessage(ChatColor.GRAY + "/mucore reload " + ChatColor.WHITE + "- Reload config & webhook");
        sender.sendMessage(ChatColor.GRAY + "/mucore status " + ChatColor.WHITE + "- Check system health");
    }
}