package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /untrust <player>
 * Removes trust from a player in your claim.
 */
public class UntrustCommand implements CommandExecutor {

    private final PlotManager plots;
    private final MessagesUtil messages;

    public UntrustCommand(ProShield plugin, PlotManager plots) {
        this.plots = plots;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return true;
        }

        if (args.length < 1) {
            messages.send(player, "commands.untrust.usage");
            return true;
        }

        Plot plot = plots.getPlot(player.getLocation().getChunk());
        if (plot == null || !plot.isOwner(player.getUniqueId())) {
            messages.send(player, "commands.untrust.not-owner");
            return true;
        }

        String targetName = args[0];
        boolean removed = plot.removeTrusted(targetName);
        if (removed) {
            messages.send(player, "commands.untrust.removed", "%player%", targetName);
        } else {
            messages.send(player, "commands.untrust.not-found", "%player%", targetName);
        }
        return true;
    }
}
