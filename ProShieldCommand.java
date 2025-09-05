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
            sender.sendMessage(ChatColor.RED + "Only players can use ProShield commands.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "ProShield Commands:");
            player.sendMessage(ChatColor.AQUA + "/proshield claim" + ChatColor.GRAY + " - Claim a plot.");
            player.sendMessage(ChatColor.AQUA + "/proshield check" + ChatColor.GRAY + " - Check your plot info.");
            player.sendMessage(ChatColor.AQUA + "/proshield reload" + ChatColor.GRAY + " - Reload the configuration.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim": {
                if (plotManager.hasPlot(player)) {
                    player.sendMessage(ChatColor.RED + "You already own a plot!");
                    return true;
                }

                // Pull default radius from config.yml
                int defaultRadius = plugin.getConfig().getInt("protection.default-radius", 10);

                boolean success = plotManager.claimPlot(player, defaultRadius);
                if (success) {
                    player.sendMessage(ChatColor.GREEN + "You successfully claimed a plot with radius " 
                            + ChatColor.AQUA + defaultRadius + ChatColor.GREEN + ".");
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to claim plot. The area might already be claimed.");
                }
                return true;
            }

            case "check": {
                if (!plotManager.hasPlot(player)) {
                    player.sendMessage(ChatColor.RED + "You don’t own a plot yet.");
                    return true;
                }

                String info = plotManager.getPlotInfo(player);
                player.sendMessage(ChatColor.YELLOW + "Your plot info: " + ChatColor.AQUA + info);
                return true;
            }

            case "reload": {
                if (!player.hasPermission("proshield.reload")) {
                    player.sendMessage(ChatColor.RED + "You don’t have permission to reload ProShield.");
                    return true;
                }

                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");
                return true;
            }

            default: {
                player.sendMessage(ChatColor.RED + "Unknown command. Use /proshield for help.");
                return true;
            }
        }
    }
}
