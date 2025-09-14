// src/main/java/com/snazzyatoms/proshield/commands/PlayerCommandDispatcher.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommandDispatcher {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public PlayerCommandDispatcher(ProShield plugin, PlotManager plotManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = messages;
    }

    public boolean handleCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        if (args.length == 0) {
            messages.send(player, "&eUse /proshield help for commands.");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "claim" -> {
                Location loc = player.getLocation();
                if (plotManager.getPlot(loc) != null) {
                    messages.send(player, "&cThis chunk is already claimed.");
                    return true;
                }

                // ✅ Fix: pass UUID instead of Player
                Plot plot = plotManager.createPlot(player.getUniqueId(), loc);
                messages.send(player, "&aClaimed land at &7" + loc.getBlockX() + "," + loc.getBlockZ());
                plugin.getLogger().info(player.getName() + " claimed land at " + loc);
            }

            case "unclaim" -> {
                Location loc = player.getLocation();
                Plot plot = plotManager.getPlot(loc);
                if (plot == null) {
                    messages.send(player, "&cNo claim here.");
                    return true;
                }
                if (!plot.isOwner(player.getUniqueId())) {
                    messages.send(player, "&cOnly the owner can unclaim this land.");
                    return true;
                }

                // ✅ Fix: removePlot by Location
                plotManager.removePlot(loc);
                messages.send(player, "&cUnclaimed land at &7" + loc.getBlockX() + "," + loc.getBlockZ());
            }

            case "info" -> {
                Location loc = player.getLocation();
                Plot plot = plotManager.getPlot(loc);
                if (plot == null) {
                    messages.send(player, "&7No claim at this location.");
                    return true;
                }

                String ownerName = plugin.getServer().getOfflinePlayer(plot.getOwner()).getName();
                messages.send(player, "&eOwner: &6" + (ownerName != null ? ownerName : plot.getOwner()));
                messages.send(player, "&eFlags: &7" + plot.getFlags());
                messages.send(player, "&eTrusted: &7" + plot.getTrusted().size() + " players");
            }

            default -> messages.send(player, "&cUnknown command. Use &e/proshield help");
        }

        return true;
    }
}
