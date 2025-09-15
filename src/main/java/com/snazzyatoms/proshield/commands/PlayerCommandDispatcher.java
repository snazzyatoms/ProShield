package com.snazzyatoms.proshield.commands;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

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

    // Examples for legacy calls in your code (if any)
    public void printClaimSummary(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messages.send(player, "&7No claim here.");
            return;
        }
        OfflinePlayer owner = plugin.getServer().getOfflinePlayer(plot.getOwner());
        messages.send(player, "&eOwner: &f" + (owner.getName() == null ? owner.getUniqueId() : owner.getName()));
        messages.send(player, "&eTrusted: &f" + plot.getTrusted().size());
        messages.send
