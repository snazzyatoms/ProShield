package com.proshield.commands;

import com.proshield.ProShield;
import com.proshield.managers.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProShieldCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProShieldCommand(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /proshield <claim|check>");
            return true;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /proshield claim <radius>");
                return true;
            }

            int radius;
            try {
                radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Radius must be a number!");
                return true;
            }

            boolean success = plotManager.claimPlot(player, radius);
            if (success) {
                player.sendMessage(ChatColor.GREEN + "You successfully claimed a plot with radius " + radius + "!");
            } else {
                player.sendMessage(ChatColor.RED + "You already have a plot!");
            }
            return true;

        } else if (args[0].equalsIgnoreCase("check")) {
            PlotManager.Plot plot = plotManager.getPlot(player);
            if (plot == null) {
                player.sendMessage(ChatColor.RED + "You don't have a plot yet!");
            } else {
                player.sendMessage(ChatColor.GREEN + "Your plot is centered at " +
                        ChatColor.YELLOW + plot.getCenter().getBlockX() + ", " + plot.getCenter().getBlockZ() +
                        ChatColor.GREEN + " with radius " + ChatColor.YELLOW + plot.getRadius());
            }
            return true;
        }

        player.sendMessage(ChatColor.RED + "Unknown subcommand!");
        return true;
    }
}
