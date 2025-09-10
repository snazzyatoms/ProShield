package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * /transfer <player>
 *
 * Transfers ownership of the current claim to another player.
 */
public class TransferCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;
    private final MessagesUtil msg;

    public TransferCommand(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        this.msg = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            msg.send(sender, "error.player-only");
            return true;
        }

        if (args.length < 1) {
            msg.send(player, "transfer.usage");
            return true;
        }

        // Target player resolution
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID targetId = target.getUniqueId();
        if (targetId.equals(player.getUniqueId())) {
            msg.send(player, "transfer.cannot-self");
            return true;
        }

        // Must be in a claim
        Plot plot = plots.getPlot(player.getLocation());
        if (plot == null) {
            msg.send(player, "transfer.no-claim");
            return true;
        }

        // Must be owner
        if (!plot.isOwner(player.getUniqueId())) {
            msg.send(player, "transfer.not-owner", plot.getDisplayNameSafe());
            return true;
        }

        // Transfer ownership
        plot.setOwner(targetId);
        plots.savePlot(plot); // ensure persistence

        msg.send(player, "transfer.success", target.getName());
        if (target.isOnline()) {
            msg.send(target.getPlayer(), "transfer.received", player.getName());
        }

        return true;
    }
}
