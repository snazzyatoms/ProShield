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

/**
 * /untrust <player>
 *
 * Removes a player's trust from your current claim.
 */
public class UntrustCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil msg;

    public UntrustCommand(ProShield plugin, PlotManager plots, ClaimRoleManager roles) {
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
            msg.send(player, "untrust.usage");
            return true;
        }

        // Resolve target UUID
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID targetId = target.getUniqueId();
        if (targetId.equals(player.getUniqueId())) {
            msg.send(player, "untrust.cannot-self");
            return true;
        }

        // Ensure player is standing in a claim
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            msg.send(player, "untrust.no-claim");
            return true;
        }

        // Must be owner
        if (!plot.isOwner(player.getUniqueId())) {
            msg.send(player, "untrust.not-owner", plot.getDisplayNameSafe());
            return true;
        }

        // Remove trust
        boolean removed = roles.untrustPlayer(plot, targetId);
        if (!removed) {
            msg.send(player, "untrust.not-trusted", target.getName());
            return true;
        }

        msg.send(player, "untrust.success", target.getName());
        return true;
    }
}
