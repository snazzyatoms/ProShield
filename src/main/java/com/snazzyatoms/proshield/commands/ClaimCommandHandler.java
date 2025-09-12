package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClaimCommandHandler implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public ClaimCommandHandler(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        UUID playerId = player.getUniqueId();
        Chunk chunk = player.getLocation().getChunk();
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "claim" -> {
                if (plotManager.getPlot(chunk) != null) {
                    messages.send(player, "claim.already-owned");
                    return true;
                }
                plotManager.createPlot(playerId, chunk);
                messages.send(player, "claim.success");
            }
            case "unclaim" -> {
                Plot plot = plotManager.getPlot(chunk);
                if (plot == null) {
                    messages.send(player, "claim.not-owner");
                    return true;
                }
                if (!plot.isOwner(playerId) && !roleManager.canUnclaim(playerId, plot.getId())) {
                    messages.send(player, "error.no-permission");
                    return true;
                }
                plotManager.removePlot(plot);
                messages.send(player, "claim.unclaimed");
            }
            case "trust" -> {
                if (args.length < 1) {
                    player.sendMessage("§cUsage: /trust <player> [role]");
                    return true;
                }
                String target = args[0];
                String role = (args.length >= 2) ? args[1] : "trusted";
                // TODO: integrate with ClaimRoleManager
                player.sendMessage("§aTrusted " + target + " as " + role + ".");
            }
            case "untrust" -> {
                if (args.length < 1) {
                    player.sendMessage("§cUsage: /untrust <player>");
                    return true;
                }
                String target = args[0];
                // TODO: integrate with ClaimRoleManager
                player.sendMessage("§cUntrusted " + target + ".");
            }
            case "roles" -> {
                // TODO: open GUIManager roles menu
                player.sendMessage("§eOpening roles GUI...");
            }
            case "transfer" -> {
                if (args.length < 1) {
                    player.sendMessage("§cUsage: /transfer <player>");
                    return true;
                }
                String target = args[0];
                // TODO: handle ownership transfer
                player.sendMessage("§aTransferred claim to " + target + ".");
            }
            default -> player.sendMessage("§cUnknown claim command.");
        }
        return true;
    }
}
