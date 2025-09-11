// src/main/java/com/snazzyatoms/proshield/commands/UntrustCommand.java
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

import java.util.Optional;
import java.util.UUID;

public class UntrustCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public UntrustCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roles) {
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
            messages.send(player, "usage.untrust", "/" + label + " <player>");
            return true;
        }

        // Resolve target (allow offline)
        OfflinePlayer targetOP = Bukkit.getPlayerExact(args[0]);
        if (targetOP == null) {
            targetOP = Bukkit.getOfflinePlayer(args[0]);
            if (targetOP == null || targetOP.getUniqueId() == null) {
                messages.send(player, "error.player-not-found", args[0]);
                return true;
            }
        }

        UUID target = targetOP.getUniqueId();

        // Must be in a claim
        Optional<Plot> claimOpt = plotManager.getClaim(player.getLocation());
        if (claimOpt.isEmpty()) {
            messages.send(player, "error.not-in-claim");
            return true;
        }
        Plot plot = claimOpt.get();

        // Only owner/admin can modify trust
        if (!plot.isOwner(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            messages.send(player, "error.cannot-modify-claim");
            return true;
        }

        // Ensure target is trusted
        if (!plot.getTrusted().containsKey(target)) {
            String targetName = targetOP.getName() != null ? targetOP.getName() : target.toString();
            messages.send(player, "untrust.not-trusted", targetName);
            return true;
        }

        // Delegate to role manager (handles removal + async save)
        roles.untrustPlayer(plot, target);

        // Feedback
        String targetName = targetOP.getName() != null ? targetOP.getName() : target.toString();
        messages.send(player, "untrust.removed", targetName);

        // Notify target if online
        if (targetOP.isOnline()) {
            Player tp = targetOP.getPlayer();
            if (tp != null) {
                messages.send(tp, "untrust.you-were-untrusted", plot.getDisplayNameSafe());
            }
        }

        return true;
    }
}
