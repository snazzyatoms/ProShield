package com.proshield.commands;

import com.proshield.ProShield;
import com.proshield.managers.PlotManager;
import org.bukkit.Location;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        PlotManager plotManager = plugin.getPlotManager();

        if (args.length == 0) {
            player.sendMessage("§aProShield Commands:");
            player.sendMessage("§e/proshield claim <radius> §7- Claim a plot at your location");
            player.sendMessage("§e/proshield info §7- View details of your claimed plot");
            return true;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /proshield claim <radius>");
                return true;
            }

            try {
                int radius = Integer.parseInt(args[1]);

                if (radius < 5) {
                    player.sendMessage("§cMinimum radius is 5!");
                    return true;
                }

                if (plotManager.claimPlot(player, radius)) {
                    player.sendMessage("§aPlot claimed successfully!");
                } else {
                    player.sendMessage("§cYou already own a plot!");
                }

            } catch (NumberFormatException e) {
                player.sendMessage("§cRadius must be a number!");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            if (!plotManager.hasPlot(player)) {
                player.sendMessage("§cYou don’t own a plot yet!");
                return true;
            }

            int radius = plotManager.getPlotRadius(player);
            Location center = plotManager.getPlotCenter(player);

            player.sendMessage("§aYour Plot Info:");
            player.sendMessage("§7Center: §e" +
                    center.getBlockX() + ", " +
                    center.getBlockY() + ", " +
                    center.getBlockZ() + " in world " + center.getWorld().getName());
            player.sendMessage("§7Radius: §e" + radius + " blocks");

            return true;
        }

        player.sendMessage("§cUnknown command. Type /proshield for help.");
        return true;
    }
}
