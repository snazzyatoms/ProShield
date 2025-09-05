package com.proshield.commands;

import com.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "ProShield Commands:");
            sender.sendMessage(ChatColor.AQUA + "/proshield reload" + ChatColor.GRAY + " - Reloads the plugin configuration");
            sender.sendMessage(ChatColor.AQUA + "/proshield claim <radius>" + ChatColor.GRAY + " - Claim a plot");
            sender.sendMessage(ChatColor.AQUA + "/proshield check" + ChatColor.GRAY + " - Check your plot info");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("proshield.reload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                return true;
            }

            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");

            // Re-register listeners so they pick up the new config values
            plugin.getServer().getPluginManager().registerEvents(
                new com.proshield.listeners.PlotProtectionListener(plugin), plugin
            );

            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown command. Type /proshield for help.");
        return true;
    }
}
