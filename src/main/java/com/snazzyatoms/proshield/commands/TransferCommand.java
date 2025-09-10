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
 * Transfers ownership of your claim to another player.
 */
public class TransferCommand implements CommandExecutor {

    private final PlotManager plots;
    private final MessagesUtil messages;

    public TransferCommand(ProShield plugin, PlotManager plots) {
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
            messages.send(player, "commands.transfer.usage");
            return true;
        }

        Plot plot = plots.getPlot(player.getLocation().getChunk());
        if (plot == null || !plot.isOwner(player.getUniqueId())) {
            messages.send(player, "commands.transfer.not-owner");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null) {
            messages.send(player, "commands.transfer.invalid", "%player%", args[0]);
            return true;
        }

        UUID newOwner = target.getUniqueId();
        plots.transferOwnership(plot, newOwner);
        messages.send(player, "commands.transfer.success", "%player%", target.getName());
        return true;
    }
}
