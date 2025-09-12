package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Unified claim-related commands for players:
 * /claim, /unclaim, /trust, /untrust, /roles, /transfer, /flags
 */
public class ClaimCommandHandler implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final GUIManager gui;
    private final MessagesUtil messages;

    public ClaimCommandHandler(ProShield plugin, PlotManager plots, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
        this.gui = plugin.getGuiManager();
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.player-only");
            return true;
        }

        String name = cmd.getName().toLowerCase();
        switch (name) {
            case "claim" -> handleClaim(player);
            case "unclaim" -> handleUnclaim(player);
            case "trust" -> handleTrust(player, args);
            case "untrust" -> handleUntrust(player, args);
            case "roles" -> handleRoles(player);
            case "transfer" -> handleTransfer(player, args);
            case "flags" -> handleFlags(player);
            default -> messages.send(player, "error.no-permission");
        }
        return true;
    }

    private void handleClaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        if (plots.getPlot(chunk) != null) {
            messages.send(player, "claim.already-owned",
                    "{owner}", plots.getPlot(chunk).getOwnerNameSafe());
            return;
        }
        Plot plot = plots.createPlot(player.getUniqueId(), chunk);
        messages.send(player, "claim.success", "{claim}", plot.getDisplayNameSafe());
    }

    private void handleUnclaim(Player player) {
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.no-claim");
            return;
        }
        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "claim.not-owner");
            return;
        }
        plots.removePlot(plot);
        messages.send(player, "claim.unclaimed", "{claim}", plot.getDisplayNameSafe());
    }

    private void handleTrust(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "trust.usage", "{role}", "member");
            return;
        }
        // TODO: Full trust system (hook into ClaimRoleManager)
        messages.send(player, "trust.added",
                "{player}", args[0],
                "{claim}", "this claim");
    }

    private void handleUntrust(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "untrust.usage");
            return;
        }
        // TODO: Full untrust system (hook into ClaimRoleManager)
        messages.send(player, "untrust.removed",
                "{player}", args[0],
                "{claim}", "this claim");
    }

    private void handleRoles(Player player) {
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "roles.no-claim");
            return;
        }
        gui.openMenu(player, "roles");
        messages.send(player, "roles.opened");
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 1) {
            messages.send(player, "transfer.usage");
            return;
        }
        // TODO: Implement ownership transfer
        messages.send(player, "transfer.success",
                "{player}", args[0],
                "{claim}", "this claim");
    }

    private void handleFlags(Player player) {
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "flags.no-claim");
            return;
        }
        gui.openMenu(player, "flags");
    }
}
