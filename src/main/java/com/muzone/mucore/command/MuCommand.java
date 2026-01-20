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
        if (!sender.hasPermission("mucore.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "MuSentry (MuCore) v1.0 running.");
            sender.sendMessage(ChatColor.GRAY + "/mucore alerts - Toggle alerts");
            sender.sendMessage(ChatColor.GRAY + "/mucore info <player> - Check stats");
            return true;
        }

        if (args[0].equalsIgnoreCase("alerts")) {
            if (!(sender instanceof Player)) return true;
            Player player = (Player) sender;
            PlayerData data = plugin.getPlayerManager().getData(player);
            
            if (data != null) {
                boolean newState = !data.isAlertsEnabled();
                data.setAlertsEnabled(newState);
                player.sendMessage(ChatColor.AQUA + "MuSentry Alerts: " + (newState ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /mucore info <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            PlayerData data = plugin.getPlayerManager().getData(target);
            if (data != null) {
                sender.sendMessage(ChatColor.DARK_AQUA + "--- " + target.getName() + " ---");
                sender.sendMessage(ChatColor.GRAY + "Platform: " + (data.isBedrock() ? ChatColor.GREEN + "Bedrock" : ChatColor.YELLOW + "Java"));
                sender.sendMessage(ChatColor.GRAY + "Total Violation Level: " + ChatColor.RED + data.getVL());
            }
            return true;
        }

        return true;
    }
}