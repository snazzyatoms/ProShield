package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.RolePermissions;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            messages.send(sender, "error.player-only");
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

        // Check permissions (owner always bypasses, else need manageTrust)
        if (!plot.isOwner(player.getUniqueId())) {
            ClaimRole role = roles.getRole(plot, player.getUniqueId());
            RolePermissions perms = roles.getRolePermissions(plot.getId(), role.name().toLowerCase());

            if (!perms.canManageTrust()) {
                messages.send(player, "error.cannot-modify-claim");
                return true;
            }
        }

        // Ensure target is trusted
        if (!plot.getTrusted().containsKey(target)) {
            String targetName = targetOP.getName() != null ? targetOP.getName() : target.toString();
            messages.send(player, "untrust.not-trusted", targetName);
            return true;
        }

        // Remove via RoleManager (handles save)
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
