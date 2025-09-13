// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlotListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public PlotListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        this.messages = messages;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Plot fromPlot = plotManager.getPlot(event.getFrom());
        Plot toPlot = plotManager.getPlot(event.getTo());

        if (fromPlot == toPlot) return; // same claim or wilderness

        if (toPlot != null) {
            if (toPlot.isOwner(player.getUniqueId())) {
                messages.send(player, plugin.getConfig().getString("messages.enter-own"));
            } else {
                String ownerName = plotManager.getPlayerName(toPlot.getOwner());
                messages.send(player,
                        plugin.getConfig().getString("messages.enter-other")
                                .replace("{owner}", ChatColor.YELLOW + ownerName + ChatColor.RESET));
            }
        } else {
            if (plugin.getConfig().getBoolean("messages.show-wilderness", true)) {
                messages.send(player, plugin.getConfig().getString("messages.wilderness"));
            }
        }
    }
}
