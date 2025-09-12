// src/main/java/com/snazzyatoms/proshield/commands/ClaimCommandHandler.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ClaimCommandHandler implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final GUIManager guiManager;
    private final CompassManager compassManager;
    private final MessagesUtil messages;

    public ClaimCommandHandler(ProShield plugin,
                               PlotManager plotManager,
                               ClaimRoleManager roleManager,
                               GUIManager guiManager,
                               CompassManager compassManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.guiManager = guiManager;
        this.compassManager = compassManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }

        String name = cmd.getName().toLowerCase();
        switch (name) {
            case "claim":    return handleClaim(player);
            case "unclaim":  return handleUnclaim(player);
            case "trust":    return handleTrust(player, args);
            case "untrust":  return handleUntrust(player, args);
            case "roles":    return handleRoles(player);
            case "transfer": return handleTransfer(player, args);
            case "flags":    return handleFlags(player);
            default: return false;
        }
    }

    /* -------------------------------------------------------
     * /claim
     * ------------------------------------------------------- */
    private boolean handleClaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        if (plotManager.getPlot(chunk) != null) {
            messages.send(player, "claim.already-owned",
                    Map.of("owner", plotManager.getPlot(chunk).getOwner().toString()));
            return true;
        }

        Plot plot = plotManager.createPlot(player.getUniqueId(), chunk);
        messages.send(player, "claim.success", Map.of("claim", plot.getDisplayNameSafe()));
        return true;
    }

    /* -------------------------------------------------------
     * /unclaim
     * ------------------------------------------------------- */
    private boolean handleUnclaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            messages.send(player, "claim.unclaimed");
            return true;
        }

        UUID playerId = player.getUniqueId();
        if (!plot.isOwner(playerId) && !roleManager.canUnclaim(playerId, plot.getId())) {
            messages.send(player, "error.no-permission");
            return true;
        }

        plotManager.removePlot(plot);
        messages.send(player, "claim.unclaimed");
        return true;
    }

    /* -------------------------------------------------------
     * /trust
     * ------------------------------------------------------- */
    private boolean handleTrust(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "trust.usage");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return true;
        }

        String targetName = args[0];
        UUID claimId = plot.getId();
        UUID targetId = plugin.getServer().getOfflinePlayer(targetName).getUniqueId();

        ClaimRole role = ClaimRole.TRUSTED;
        if (args.length >= 2) {
            try {
                role = ClaimRole.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                messages.send(player, "trust.invalid-role", Map.of("role", args[1]));
                return true;
            }
        }

        roleManager.setRole(claimId, targetId, role);
        messages.send(player, "trust.added", Map.of("player", targetName, "role", role.name(), "claim", plot.getDisplayNameSafe()));
        return true;
    }

    /* -------------------------------------------------------
     * /untrust
     * ------------------------------------------------------- */
    private boolean handleUntrust(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "untrust.usage");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return true;
        }

        String targetName = args[0];
        UUID claimId = plot.getId();
        UUID targetId = plugin.getServer().getOfflinePlayer(targetName).getUniqueId();

        roleManager.clearRole(claimId, targetId);
        messages.send(player, "untrust.removed", Map.of("player", targetName, "claim", plot.getDisplayNameSafe()));
        return true;
    }

    /* -------------------------------------------------------
     * /roles
     * ------------------------------------------------------- */
    private boolean handleRoles(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            messages.send(player, "error.no-claim");
            return true;
        }

        guiManager.openMenu(player, "roles");
        return true;
    }

    /* -------------------------------------------------------
     * /transfer
     * ------------------------------------------------------- */
    private boolean handleTransfer(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "transfer.usage");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return true;
        }

        UUID newOwner = plugin.getServer().getOfflinePlayer(args[0]).getUniqueId();
        plot.setOwner(newOwner);

        messages.send(player, "transfer.success", Map.of("player", args[0], "claim", plot.getDisplayNameSafe()));
        return true;
    }

    /* -------------------------------------------------------
     * /flags
     * ------------------------------------------------------- */
    private boolean handleFlags(Player player) {
        guiManager.openMenu(player, "flags");
        return true;
    }
}
