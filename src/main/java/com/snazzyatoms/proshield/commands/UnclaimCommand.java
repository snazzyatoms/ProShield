package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UnclaimCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public UnclaimCommand(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can unclaim land.");
            return true;
        }

        UUID playerId = player.getUniqueId();
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlotAt(chunk);

        if (plot == null) {
            messages.send(player, "not_claimed");
            return true;
        }

        if (!plot.isOwner(playerId) && !roleManager.canUnclaim(playerId, plot.getId())) {
            messages.send(player, "no_permission_unclaim");
            return true;
        }

        plotManager.removePlot(plot);
        messages.send(player, "unclaim_success", String.valueOf(chunk.getX()), String.valueOf(chunk.getZ()));
        return true;
    }
}
