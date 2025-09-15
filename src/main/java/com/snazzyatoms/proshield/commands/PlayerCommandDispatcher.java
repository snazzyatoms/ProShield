package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

/**
 * Optional helper used by some codepaths:
 * provides convenience wrappers around player-facing actions.
 */
public class PlayerCommandDispatcher {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public PlayerCommandDispatcher(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.messages = plugin.getMessagesUtil();
    }

    public void claim(Player player) {
        plotManager.claimPlot(player);
    }

    public void unclaim(Player player) {
        plotManager.unclaimPlot(player);
    }

    public void info(Player player) {
        plotManager.sendClaimInfo(player);
    }

    /**
     * Print a quick summary of the claim the player is standing in.
     */
    public void printClaimSummary(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&7No claim here.");
            return;
        }

        // Owner info
        OfflinePlayer owner = plugin.getServer().getOfflinePlayer(plot.getOwner());
        String ownerName = (owner.getName() != null ? owner.getName() : owner.getUniqueId().toString());

        messages.send(player, "&eOwner: &f" + ownerName);
        messages.send(player, "&eTrusted Players: &f" + plot.getTrusted().size());

        if (plot.getFlags().isEmpty()) {
            messages.send(player, "&eFlags: &7None set.");
        } else {
            // ✅ fix join on Map
            String flags = plot.getFlags().keySet().stream().collect(Collectors.joining(", "));
            messages.send(player, "&eFlags: &f" + flags);
        }
    }
}
