package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public ClaimCommand(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        this.messages = plugin.getMessagesUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "error.players-only");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();

        Plot existing = plots.getPlot(chunk);
        if (existing != null) {
            messages.send(player, "claim.already-claimed", existing.getOwnerNameSafe());
            return true;
        }

        Plot plot = new Plot(chunk, player.getUniqueId());
        plots.addPlot(plot);
        plots.saveAsync(plot);

        messages.send(player, "claim.success", chunk.getX() + "", chunk.getZ() + "");
        return true;
    }
}
