package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnclaimCommand implements CommandExecutor {

    private final ProShield plugin;
    private final PlotManager plots;
    private final ClaimRoleManager roles;

    public UnclaimCommand(ProShield plugin, PlotManager plots, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.plots = plots;
        this.roles = roles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plots.getPlot(chunk);
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "⛔ This chunk is not claimed.");
            return true;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            if (!roles.canUnclaim(plot.getId(), player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "⛔ You do not have permission to unclaim this land.");
                return true;
            }
        }

        plots.unclaim(chunk);
        player.sendMessage(ChatColor.GREEN + "✅ Successfully unclaimed this chunk.");

        return true;
    }
}
