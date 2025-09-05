package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
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
            sender.sendMessage(ChatColor.GOLD + "⚔ ProShield Commands:");
            sender.sendMessage(ChatColor.YELLOW + "/proshield help " + ChatColor.GRAY + "- Show help menu");
            sender.sendMessage(ChatColor.YELLOW + "/proshield info " + ChatColor.GRAY + "- Plugin info");
            sender.sendMessage(ChatColor.YELLOW + "/proshield reload " + ChatColor.GRAY + "- Reload config");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (sender.hasPermission("proshield.admin")) {
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "✅ ProShield config reloaded.");
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permission.");
                }
                return true;

            case "info":
                sender.sendMessage(ChatColor.GOLD + "🛡 ProShield " + plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.GRAY + "Author: " + plugin.getDescription().getAuthors());
                sender.sendMessage(ChatColor.GRAY + "Description: " + plugin.getDescription().getDescription());
                return true;

            case "help":
                sender.sendMessage(ChatColor.GOLD + "⚔ ProShield Help Menu:");
                sender.sendMessage(ChatColor.YELLOW + "/proshield help " + ChatColor.GRAY + "- Show this help menu");
                sender.sendMessage(ChatColor.YELLOW + "/proshield info " + ChatColor.GRAY + "- Plugin info");
                sender.sendMessage(ChatColor.YELLOW + "/proshield reload " + ChatColor.GRAY + "- Reload config");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /proshield help.");
                return true;
        }
    }
}
