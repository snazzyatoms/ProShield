package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TransferCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public TransferCommand(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "general.no-console");
            return true;
        }

        Plot plot = plotManager.getPlot(player.getLocation().getChunk());
        if (plot == null) {
            messages.send(player, "transfer.no-claim");
            return true;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            messages.send(player, "transfer.not-owner");
            return true;
        }

        if (args.length < 1) {
            messages.send(player, "transfer.usage");
            return true;
        }

        String newOwner = args[0];
        plotManager.transferOwnership(plot, newOwner);
        messages.send(player, "transfer.success", newOwner);
        return true;
    }
}
