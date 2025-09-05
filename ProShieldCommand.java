package com.proshield.commands;

import com.proshield.managers.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProShieldCommand implements CommandExecutor, TabCompleter {

    private final PlotManager plotManager;

    public ProShieldCommand(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim":
                Location loc = player.getLocation();
                boolean success = plotManager.claimPlot(player, loc, 10); // Default radius
                if (success) {
                    player.sendMessage(ChatColor.GREEN + "✅ You claimed a plot at your location with radius 10!");
                } else {
                    player.sendMessage(ChatColor.RED + "❌ You already own a plot!");
                }
                return true;

            case "check":
                Location playerLoc = player.getLocation();

                if (!plotManager.isInsideAnyPlot(playerLoc)) {
                    player.sendMessage(ChatColor.YELLOW + "ℹ️ You are not standing inside any plot.");
                    return true;
                }

                if (plotManager.isInsideOwnPlot(player, playerLoc)) {
                    PlotManager.Plot plot = plotManager.getPlot(player);
                    if (plot != null) {
                        player.sendMessage(ChatColor.GREEN + "✅ You are inside your plot!");
                        player.sendMessage(ChatColor.AQUA + "Center: " + ChatColor.WHITE +
                                plot.getCenter().getBlockX() + ", " +
                                plot.getCenter().getBlockY() + ", " +
                                plot.getCenter().getBlockZ());
                        player.sendMessage(ChatColor.AQUA + "Radius: " + ChatColor.WHITE + plot.getRadius());
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "⚠️ You are inside another player's plot!");
                }
                return true;

            case "reload":
                if (!player.hasPermission("proshield.reload")) {
                    player.sendMessage(ChatColor.RED + "❌ You don’t have permission to reload ProShield.");
                    return true;
                }
                plotManager.getPlugin().reloadConfig();
                player.sendMessage(ChatColor.GREEN + "🔄 ProShield configuration reloaded.");
                return true;

            case "help":
                sendHelp(player);
                return true;

            default:
                player.sendMessage(ChatColor.YELLOW + "Unknown subcommand. Use /proshield help for available commands.");
                return true;
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "===== ProShield Commands =====");
        player.sendMessage(ChatColor.YELLOW + "/proshield claim" + ChatColor.WHITE + " - Claim a new plot");
        player.sendMessage(ChatColor.YELLOW + "/proshield check" + ChatColor.WHITE + " - Check your plot status");
        if (player.hasPermission("proshield.reload")) {
            player.sendMessage(ChatColor.YELLOW + "/proshield reload" + ChatColor.WHITE + " - Reload configuration");
        }
        player.sendMessage(ChatColor.YELLOW + "/proshield help" + ChatColor.WHITE + " - Show this help menu");
        player.sendMessage(ChatColor.GOLD + "==============================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList("claim", "check", "help", "reload"));

            if (!(sender.hasPermission("proshield.reload"))) {
                options.remove("reload"); // Only show reload if they have permission
            }

            return options;
        }
        return Collections.emptyList();
    }
}
