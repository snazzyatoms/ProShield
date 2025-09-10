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

public class UntrustCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public UntrustCommand(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length < 1) {
            MessagesUtil.sendMessage(player, "command-untrust-usage");
            return true;
        }

        String targetName = args[0];
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null || !plot.isOwner(player.getUniqueId())) {
            MessagesUtil.sendMessage(player, "no-permission");
            return true;
        }

        if (!plot.isTrusted(targetName)) {
            MessagesUtil.sendMessage(player, "command-untrust-not-found", "{player}", targetName);
            return true;
        }

        plot.removeTrusted(targetName);
        MessagesUtil.sendMessage(player, "command-untrust-success", "{player}", targetName);
        return true;
    }
}
