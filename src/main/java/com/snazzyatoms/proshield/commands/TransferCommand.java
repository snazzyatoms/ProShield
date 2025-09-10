package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TransferCommand implements CommandExecutor {

    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public TransferCommand(ProShield plugin, PlotManager plotManager) {
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "general.players-only");
            return true;
        }
        if (args.length < 1) {
            messages.send(player, "transfer.usage");
            return true;
        }

        String targetName = args[0];
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null || !plot.isOwner(player.getUniqueId())) {
            messages.send(player, "transfer.not-owner");
            return true;
        }

        plotManager.transferOwnership(plot, targetName);
        messages.send(player, "transfer.success", "%player%", targetName);
        return true;
    }
}
