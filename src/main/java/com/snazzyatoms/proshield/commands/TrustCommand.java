// src/main/java/com/snazzyatoms/proshield/commands/TrustCommand.java
package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class TrustCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public TrustCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roles) {
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
            messages.send(player, "usage.trust", "/" + label + " <player> [role]");
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
        if (player.getUniqueId().equals(target)) {
            messages.send(player, "trust.cannot-self");
            return true;
        }

        // Role (optional; default BUILDER)
        ClaimRole role = ClaimRole.BUILDER;
        if (args.length >= 2) {
            String r = args[1].toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
            try {
                role = ClaimRole.valueOf(r);
            } catch (IllegalArgumentException iae) {
                messages.send(player, "trust.invalid-role", args[1]);
                return true;
            }
        }

        // Must be inside a claim
        Optional<Plot> claimOpt = plotManager.getClaim(player.getLocation());
        if (claimOpt.isEmpty()) {
            messages.send(player, "error.not-in-claim");
            return true;
        }
        Plot plot = claimOpt.get();

        // Only owner/admin can manage trust
        if (!plot.isOwner(player.getUniqueId()) && !player.hasPermission("proshield.admin")) {
            messages.send(player, "error.cannot-modify-claim");
            return true;
        }

        // Already trusted?
        if (plot.getTrusted().containsKey(target)) {
            messages.send(player, "trust.already-trusted",
                    targetOP.getName() != null ? targetOP.getName() : target.toString());
            return true;
        }

        // Use ClaimRoleManager to apply trust & save
        roles.trustPlayer(plot, target, role);

        // Feedback
        String targetName = targetOP.getName() != null ? targetOP.getName() : target.toString();
        messages.send(player, "trust.added", targetName, role.name());

        // Notify target if online
        if (targetOP.isOnline()) {
            Player tp = targetOP.getPlayer();
            if (tp != null) {
                messages.send(tp, "trust.you-were-trusted", plot.getDisplayNameSafe(), role.name());
            }
        }

        return true;
    }
}
