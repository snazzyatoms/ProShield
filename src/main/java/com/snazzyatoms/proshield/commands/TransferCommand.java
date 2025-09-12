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
            messages.send(player, "usage.transfer", "/" + label + " <player>");
            return true;
        }

        // Resolve target (offline safe)
        OfflinePlayer targetOP = Bukkit.getPlayerExact(args[0]);
        if (targetOP == null) {
            targetOP = Bukkit.getOfflinePlayer(args[0]);
            if (targetOP == null || targetOP.getUniqueId() == null) {
                messages.send(player, "error.player-not-found", args[0]);
                return true;
            }
        }

        UUID target = targetOP.getUniqueId();

        // Must be inside a claim
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "error.not-in-claim");
            return true;
        }

        // Only owner or admin can transfer
        if (!plot.isOwner(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            messages.send(player, "error.cannot-transfer");
            return true;
        }

        // Prevent transferring to self
        if (plot.isOwner(target)) {
            messages.send(player, "transfer.already-owner");
            return true;
        }

        // Perform transfer
        UUID oldOwner = plot.getOwner();
        plot.setOwner(target);

        // Clean up trusted list
        plot.getTrusted().remove(target); // remove new owner from trusted (redundant)
        if (oldOwner != null && !oldOwner.equals(target)) {
            plot.getTrusted().put(oldOwner, null); // optionally demote old owner to "trusted" or clear entirely
        }

        // Clear & reset roles for safety
        roles.clearAllRoles(plot.getId());
        // The new owner bypasses all role checks anyway.

        plotManager.saveAsync(plot);

        // Feedback
        String targetName = targetOP.getName() != null ? targetOP.getName() : target.toString();
        messages.send(player, "transfer.success", targetName);

        // Notify target if online
        if (targetOP.isOnline()) {
            Player tp = targetOP.getPlayer();
            if (tp != null) {
                messages.send(tp, "transfer.you-are-owner", plot.getDisplayNameSafe());
            }
        }

        return true;
    }
}
