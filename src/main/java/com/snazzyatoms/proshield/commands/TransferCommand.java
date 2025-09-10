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

    public TransferCommand(ProShield plugin, PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length < 1) {
            MessagesUtil.sendMessage(player, "command-transfer-usage");
            return true;
        }

        String targetName = args[0];
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null || !plot.isOwner(player.getUniqueId())) {
            MessagesUtil.sendMessage(player, "command-transfer-not-owner");
            return true;
        }

        boolean success = plotManager.transferOwnership(plot, targetName);
        if (success) {
            MessagesUtil.sendMessage(player, "command-transfer-success", "{player}", targetName);
        } else {
            MessagesUtil.sendMessage(player, "command-transfer-fail");
        }

        return true;
    }
}
