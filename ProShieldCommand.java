package com.proshield;

import com.proshield.managers.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles all /proshield commands
 */
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
            sender.sendMessage(ChatColor.RED + "Only players can use ProShield commands.");
            return true;
        }

        Player player = (Player) sender;

        // Show help if no args
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                if (!player.hasPermission("proshield.reload")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to reload ProShield.");
                    return true;
                }
                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "✅ ProShield configuration reloaded!");
                return true;

            case "claim":
                int radius = plugin.getConfig().getInt("protection.default-radius", 10);

                if (args.length >= 2) {
                    try {
                        radius = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid radius. Please use a number.");
                        return true;
                    }
                }

                if (plotManager.claimPlot(player, radius)) {
                    player.sendMessage(ChatColor.GREEN + "✅ You successfully claimed a plot with radius " + radius + ".");
                }
                return true;

            case "check":
                if (plotManager.isInsideOwnPlot(player, player.getLocation())) {
                    player.sendMessage(ChatColor.GREEN + "✅ You are inside your claimed plot.");
                } else {
                    player.sendMessage(ChatColor.RED + "❌ You are not inside your plot.");
                }
                return true;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Type /proshield for help.");
                return true;
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.YELLOW + "----- ProShield Commands -----");
        player.sendMessage(ChatColor.AQUA + "/proshield reload" + ChatColor.GRAY + " - Reload the config");
        player.sendMessage(ChatColor.AQUA + "/proshield claim [radius]" + ChatColor.GRAY + " - Claim a plot");
        player.sendMessage(ChatColor.AQUA + "/proshield check" + ChatColor.GRAY + " - Check if you’re inside your plot");
    }
}
