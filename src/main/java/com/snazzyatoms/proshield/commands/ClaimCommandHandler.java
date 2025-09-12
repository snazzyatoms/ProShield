// src/main/java/com/snazzyatoms/proshield/commands/ClaimCommandHandler.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClaimCommandHandler implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final GUIManager guiManager;
    private final MessagesUtil messages;

    public ClaimCommandHandler(ProShield plugin,
                               PlotManager plotManager,
                               ClaimRoleManager roleManager,
                               GUIManager guiManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.guiManager = guiManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        String cmd = command.getName().toLowerCase();
        switch (cmd) {
            case "claim" -> handleClaim(player);
            case "unclaim" -> handleUnclaim(player);
            case "trust" -> handleTrust(player, args);
            case "untrust" -> handleUntrust(player, args);
            case "roles" -> handleRoles(player);
            case "transfer" -> handleTransfer(player, args);
            default -> sender.sendMessage("Unknown command.");
        }
        return true;
    }

    /* -------------------------------------------------------
     * Command Handlers
     * ------------------------------------------------------- */
    private void handleClaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        UUID playerId = player.getUniqueId();

        Plot existing = plotManager.getPlot(chunk);
        if (existing != null) {
            messages.send(player, "claim.already-owned",
                    "Owner: " + plugin.getServer().getOfflinePlayer(existing.getOwner()).getName());
            return;
        }

        Plot newPlot = plotManager.createPlot(playerId, chunk);
        messages.send(player, "claim.success",
                "Claimed at chunk [" + chunk.getX() + "," + chunk.getZ() + "]");
    }

    private void handleUnclaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        UUID playerId = player.getUniqueId();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            messages.send(player, "claim.not-owner", "This land is not claimed.");
            return;
        }

        if (!plot.isOwner(playerId) && !roleManager.canUnclaim(playerId, plot.getId())) {
            messages.send(player, "error.no-permission");
            return;
        }

        plotManager.removePlot(plot);
        messages.send(player, "claim.unclaimed", "You unclaimed this chunk.");
    }

    private void handleTrust(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "trust.usage", "Usage: /trust <player> [role]");
            return;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null) {
            messages.send(player, "error.player-not-found", args[0]);
            return;
        }

        String roleName = (args.length >= 2) ? args[1] : "trusted";
        ClaimRole role = ClaimRole.fromName(roleName);

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return;
        }

        roleManager.setRole(plot.getId(), target.getUniqueId(), role);
        messages.send(player, "trust.added", target.getName() + " → " + role.getDisplayName());
    }

    private void handleUntrust(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "untrust.usage", "Usage: /untrust <player>");
            return;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null) {
            messages.send(player, "error.player-not-found", args[0]);
            return;
        }

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return;
        }

        roleManager.clearRole(plot.getId(), target.getUniqueId());
        messages.send(player, "untrust.removed", target.getName());
    }

    private void handleRoles(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "roles.no-claim", "You must be in a claim to manage roles.");
            return;
        }

        guiManager.openMenu(player, "roles");
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "transfer.usage", "Usage: /transfer <player>");
            return;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null) {
            messages.send(player, "error.player-not-found", args[0]);
            return;
        }

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "error.not-owner");
            return;
        }

        plot.setOwner(target.getUniqueId());
        messages.send(player, "transfer.success", "Ownership transferred to " + target.getName());
    }
}
