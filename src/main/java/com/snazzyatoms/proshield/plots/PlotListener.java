package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public PlotListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Plot to = plotManager.getPlotAt(event.getTo());
        Plot from = plotManager.getPlotAt(event.getFrom());

        if (to != from) {
            if (to != null) {
                if (to.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.getMessagesUtil().get("enter-own"));
                } else {
                    player.sendMessage(plugin.getMessagesUtil().get("enter-other")
                        .replace("{owner}", to.getOwner().toString()));
                }
            } else {
                player.sendMessage(plugin.getMessagesUtil().get("wilderness"));
            }
        }
    }
}
