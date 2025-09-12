package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TransferCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public TransferCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roles = roles;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.players-only");
            return true;
        }

        if (!player.hasPermission("proshield.use")) {
            messages.send(player, "error.no-permission");
            return true;
        }

        if (args.length < 1) {
            messages.send(player, "usage.transfer", "/" + label + " <newOwner> [oldOwner]");
            return true;
        }

        // Resolve new owner (offline safe)
        OfflinePlayer newOwnerOP = Bukkit.getOfflinePlayer(args[0]);
        if (newOwnerOP == null || newOwnerOP.getUniqueId() == null) {
            messages.send(player, "error.player-not-found", args[0]);
            return true;
        }
        UUID newOwner = newOwnerOP.getUniqueId();

        // Must be inside a claim
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.not-in-claim");
            return true;
        }

        UUID oldOwner = plot.getOwner();

        // ADMIN OVERRIDE MODE (explicit oldOwner provided)
        if (args.length >= 2 && player.hasPermission("proshield.admin")) {
            OfflinePlayer oldOwnerOP = Bukkit.getOfflinePlayer(args[1]);
            if (oldOwnerOP == null || oldOwnerOP.getUniqueId() == null) {
                messages.send(player, "error.player-not-found", args[1]);
                return true;
            }
            UUID requestedOldOwner = oldOwnerOP.getUniqueId();

            // Validate
            if (!plot.isOwner(requestedOldOwner)) {
                messages.send(player, "transfer.override-fail", oldOwnerOP.getName());
                return true;
            }

            // Override transfer
            doTransfer(plot, requestedOldOwner, newOwner, newOwnerOP, player, true);
            return true;
        }

        // NORMAL MODE (self-transfer)
        if (!plot.isOwner(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            messages.send(player, "error.cannot-transfer");
            return true;
        }

        if (plot.isOwner(newOwner)) {
            messages.send(player, "transfer.already-owner");
            return true;
        }

        // Perform normal transfer
        doTransfer(plot, oldOwner, newOwner, newOwnerOP, player, false);
        return true;
    }

    private void doTransfer(Plot plot, UUID oldOwner, UUID newOwner, OfflinePlayer newOwnerOP, Player executor, boolean override) {
        plot.setOwner(newOwner);

        // Clean up trusted list
        plot.getTrusted().remove(newOwner); // new owner shouldn’t be in trusted
        if (oldOwner != null && !oldOwner.equals(newOwner)) {
            plot.getTrusted().remove(oldOwner); // old owner loses all claim rights
        }

        // Clear all role assignments for safety
        roles.clearAllRoles(plot.getId());

        // Save claim
        plotManager.saveAsync(plot);

        // Feedback to executor
        String newName = newOwnerOP.getName() != null ? newOwnerOP.getName() : newOwner.toString();
        if (override) {
            messages.send(executor, "transfer.override-success", newName);
        } else {
            messages.send(executor, "transfer.success", newName);
        }

        // Notify new owner
        if (newOwnerOP.isOnline()) {
            Player np = newOwnerOP.getPlayer();
            if (np != null) {
                messages.send(np, "transfer.you-are-owner", plot.getDisplayNameSafe());
            }
        }
    }
}
