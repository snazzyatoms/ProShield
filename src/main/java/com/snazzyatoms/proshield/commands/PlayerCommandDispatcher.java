package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class PlayerCommandDispatcher implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public PlayerCommandDispatcher(ProShield plugin,
                                   PlotManager plotManager,
                                   ClaimRoleManager roleManager,
                                   MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "claim" -> {
                plotManager.createPlot(player, player.getLocation());
                messages.send(player, "&aClaimed this chunk.");
            }
            case "unclaim" -> {
                plotManager.removePlot(player.getLocation());
                messages.send(player, "&cUnclaimed this chunk.");
            }
            case "trust" -> {
                if (args.length < 1) {
                    messages.send(player, "&cUsage: /trust <player> [role]");
                    return true;
                }
                Plot plot = plotManager.getPlot(player.getLocation());
                if (plot == null) { messages.send(player, "&cYou are not in your claim."); return true; }
                if (!plot.isOwner(player.getUniqueId())) { messages.send(player, "&cOnly the owner can trust players."); return true; }

                String targetName = args[0];
                String role = (args.length >= 2 ? args[1] : "trusted");

                boolean ok = roleManager.trustPlayer(plot, targetName, role);
                if (ok) messages.send(player, "&aTrusted &e" + targetName + " &aas &6" + role + "&a.");
                else messages.send(player, "&e" + targetName + " &cis already trusted.");
            }
            case "untrust" -> {
                if (args.length < 1) {
                    messages.send(player, "&cUsage: /untrust <player>");
                    return true;
                }
                Plot plot = plotManager.getPlot(player.getLocation());
                if (plot == null) { messages.send(player, "&cYou are not in your claim."); return true; }
                if (!plot.isOwner(player.getUniqueId())) { messages.send(player, "&cOnly the owner can untrust players."); return true; }

                String targetName = args[0];
                boolean ok = roleManager.untrustPlayer(plot, targetName);
                if (ok) messages.send(player, "&cUntrusted &e" + targetName + "&c.");
                else messages.send(player, "&e" + targetName + " &cis not trusted.");
            }
            case "roles" -> {
                // Open the Untrust list, click a head to open the per-player Role Editor
                plugin.getGuiManager().openMenu(player, "untrust");
            }
            case "transfer" -> {
                if (args.length < 1) { messages.send(player, "&cUsage: /transfer <player>"); return true; }
                Plot plot = plotManager.getPlot(player.getLocation());
                if (plot == null) { messages.send(player, "&cYou are not in your claim."); return true; }
                if (!plot.isOwner(player.getUniqueId())) { messages.send(player, "&cOnly the owner can transfer ownership."); return true; }

                String newOwnerName = args[0];
                boolean ok = roleManager.transferOwnership(plot, newOwnerName);
                if (ok) messages.send(player, "&aOwnership transferred to &e" + newOwnerName + "&a.");
                else messages.send(player, "&cTransfer failed. Make sure the name is valid and not already the owner.");
            }
            default -> messages.send(player, "&cUnknown command.");
        }
        return true;
    }
}
