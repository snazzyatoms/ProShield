// path: src/main/java/com/snazzyatoms/proshield/commands/ProShieldCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProShieldCommand(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "ProShield is running!");
            return true;
        }

        if (args[0].equalsIgnoreCase("compass")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Players only.");
                return true;
            }
            if (!p.hasPermission("proshield.compass")) {
                p.sendMessage(prefix() + ChatColor.RED + plugin.getAdminConfig().getString("messages.no-permission", "No permission."));
                return true;
            }
            p.getInventory().addItem(GUIManager.createAdminCompass());
            p.sendMessage(prefix() + ChatColor.GREEN + plugin.getAdminConfig().getString("messages.compass-given", "Admin compass added."));
            return true;
        }

        if (args[0].equalsIgnoreCase("admin")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Players only.");
                return true;
            }
            if (!p.hasPermission("proshield.admin")) {
                p.sendMessage(prefix() + ChatColor.RED + plugin.getAdminConfig().getString("messages.no-permission", "No permission."));
                return true;
            }
            plugin.getGuiManager().openAdminMenu(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("proshield.admin.reload")) {
                sender.sendMessage(prefix() + ChatColor.RED + plugin.getAdminConfig().getString("messages.no-permission", "No permission."));
                return true;
            }
            plugin.reloadAllConfigs();
            sender.sendMessage(prefix() + ChatColor.GREEN + plugin.getAdminConfig().getString("messages.reloaded", "Configs reloaded."));
            return true;
        }

        // basic player claim helpers kept as before (optional)
        if (sender instanceof Player p) {
            Location loc = p.getLocation();
            switch (args[0].toLowerCase()) {
                case "claim" -> {
                    if (plotManager.createClaim(p.getUniqueId(), loc)) {
                        p.sendMessage(prefix() + ChatColor.GREEN + "Chunk claimed.");
                    } else {
                        p.sendMessage(prefix() + ChatColor.RED + "This chunk is already claimed.");
                    }
                    return true;
                }
                case "unclaim" -> {
                    if (plotManager.removeClaim(p.getUniqueId(), loc)) {
                        p.sendMessage(prefix() + ChatColor.GREEN + "Chunk unclaimed.");
                    } else {
                        p.sendMessage(prefix() + ChatColor.RED + "You do not own this chunk.");
                    }
                    return true;
                }
            }
        }

        sender.sendMessage(prefix() + ChatColor.YELLOW + "Usage: /proshield [admin|compass|claim|unclaim|reload]");
        return true;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&', plugin.getAdminConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
