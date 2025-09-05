package com.proshield.commands;

import com.proshield.managers.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

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
            player.sendMessage(ChatColor.YELLOW + "Usage: /proshield <claim|check|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim":
                Location loc = player.getLocation();
                boolean success = plotManager.claimPlot(player, loc, 10); // Default radius
                if (success) {
                    player.sendMessage(ChatColor.GREEN + "You claimed a plot at your location with radius 10!");
                } else {
                    player.sendMessage(ChatColor.RED + "You already own a plot!");
                }
                return true;

            case "check":
                Location playerLoc = player.getLocation();

                if (!plotManager.isInsideAnyPlot(playerLoc)) {
                    player.sendMessage(ChatColor.YELLOW + "You are not standing inside any plot.");
                    return true;
                }

                if (plotManager.isInsideOwnPlot(player, playerLoc)) {
                    PlotManager.Plot plot = plotManager.getPlot(player);
                    if (plot != null) {
                        player.sendMessage(ChatColor.GREEN + "You are inside your plot!");
                        player.sendMessage(ChatColor.AQUA + "Center: " + ChatColor.WHITE + plot.getCenter().getBlockX() + ", " +
                                plot.getCenter().getBlockY() + ", " + plot.getCenter().getBlockZ());
                        player.sendMessage(ChatColor.AQUA + "Radius: " + ChatColor.WHITE + plot.getRadius());
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You are inside another player's plot!");
                }
                return true;

            case "reload":
                if (!player.hasPermission("proshield.reload")) {
                    player.sendMessage(ChatColor.RED + "You don’t have permission to reload ProShield.");
                    return true;
                }
                plotManager.getPlugin().reloadConfig();
                player.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");
                return true;

            default:
                player.sendMessage(ChatColor.YELLOW + "Unknown subcommand. Use: /proshield <claim|check|reload>");
                return true;
        }
    }
}
