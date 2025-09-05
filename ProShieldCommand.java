package com.proshield;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // No args → show help
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "=== ProShield Commands ===");
            sender.sendMessage(ChatColor.AQUA + "/proshield reload " + ChatColor.GRAY + " - Reloads the plugin configuration");
            sender.sendMessage(ChatColor.AQUA + "/proshield claim <radius> " + ChatColor.GRAY + " - Claim a plot");
            sender.sendMessage(ChatColor.AQUA + "/proshield check " + ChatColor.GRAY + " - Check your plot info");
            return true;
        }

        // Handle `/proshield reload`
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("proshield.reload")) {
                sender.sendMessage(ChatColor.RED + "You don’t have permission to use this command.");
                return true;
            }

            // Reload config only (listeners stay registered)
            plugin.reloadConfig();
            plugin.saveConfig();

            sender.sendMessage(ChatColor.GREEN + "✅ ProShield configuration reloaded!");

            // Log who executed it
            String executor = (sender instanceof Player) ? sender.getName() : "CONSOLE";
            plugin.getLogger().info("[ProShield] Configuration was reloaded by " + executor);

            return true;
        }

        // Unknown command
        sender.sendMessage(ChatColor.RED + "Unknown command. Type /proshiel
