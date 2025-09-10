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

import java.util.UUID;

/**
 * /trust <player> [role]
 *
 * Trusts a player in your current claim.
 * Default role = MEMBER if not specified.
 */
public class TrustCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil msg;

    public TrustCommand(ProShield plugin, PlotManager plots, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
        this.msg = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            msg.send(sender, "error.player-only");
            return true;
        }

        if (args.length < 1) {
            msg.send(player, "trust.usage");
            return true;
        }

        // Resolve target player
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID targetId = target.getUniqueId();
        if (targetId.equals(player.getUniqueId())) {
            msg.send(player, "trust.cannot-self");
            return true;
        }

        // Get role (default MEMBER if not specified)
        ClaimRole role = ClaimRole.MEMBER;
        if (args.length >= 2) {
            try {
                role = ClaimRole.fromString(args[1]);
            } catch (IllegalArgumentException ex) {
                msg.send(player, "trust.invalid-role", args[1]);
                return true;
            }
        }

        // Ensure player is standing in a claim
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            msg.send(player, "trust.no-claim");
            return true;
        }

        // Must be owner of the claim
        if (!plot.isOwner(player.getUniqueId())) {
            msg.send(player, "trust.not-owner", plot.getDisplayNameSafe());
            return true;
        }

        // Apply trust
        boolean success = roles.trustPlayer(plot, targetId, role);
        if (!success) {
            msg.send(player, "trust.failed", target.getName());
            return true;
        }

        msg.send(player, "trust.success", target.getName(), role.name());
        return true;
    }
}
