package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /trust <player> [role]
 * Trusts a player into your claim with optional role.
 */
public class TrustCommand implements CommandExecutor {

    private final PlotManager plots;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public TrustCommand(ProShield plugin, PlotManager plots, ClaimRoleManager roleManager) {
        this.plots = plots;
        this.roleManager = roleManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return true;
        }

        if (args.length < 1) {
            messages.send(player, "commands.trust.usage");
            return true;
        }

        Plot plot = plots.getPlot(player.getLocation().getChunk());
        if (plot == null || !plot.isOwner(player.getUniqueId())) {
            messages.send(player, "commands.trust.not-owner");
            return true;
        }

        String targetName = args[0];
        ClaimRole role = (args.length > 1) ? ClaimRole.fromString(args[1]) : ClaimRole.MEMBER;

        roleManager.trustPlayer(plot, targetName, role);
        messages.send(player, "commands.trust.added", "%player%", targetName, "%role%", role.name());
        return true;
    }
}
