package com.yourname.proshield;

import org.bukkit.ChatColor;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use ProShield commands!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /proshield <claim|info|reload>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "claim":
                if (plotManager.hasPlot(player)) {
                    player.sendMessage(ChatColor.RED + "You already own a plot!");
                    return true;
                }

                int radius;
                boolean allowCustomRadius = plugin.getConfig().getBoolean("protection.allow-custom-claim-radius", false);

                if (args.length >= 2 && allowCustomRadius) {
                    try {
                        radius = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid radius. Please enter a number.");
                        return true;
                    }
                } else {
                    radius = plugin.getConfig().getInt("protection.default-radius", 10);
                }

                boolean success = plotManager.claimPlot(player, radius);
                if (!success) {
                    int minGap = plugin.getConfig().getInt("protection.min-gap", 10);
                    player.sendMessage(ChatColor.RED + "Claim failed. Plots must be at least "
                            + minGap + " blocks apart.");
                }
                return true;

            case "info":
                player.sendMessage(ChatColor.AQUA + "Your plot: " + plotManager.getPlotInfo(player));
                return true;

            case "reload":
                if (player.hasPermission("proshield.reload")) {
                    plugin.reloadConfig();
                    player.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");
                } else {
                    player.sendMessage(ChatColor.RED + "You don’t have permission to reload ProShield.");
                }
                return true;

            default:
                player.sendMessage(ChatColor.YELLOW + "Unknown subcommand. Use: /proshield <claim|info|reload>");
                return true;
        }
    }
}
