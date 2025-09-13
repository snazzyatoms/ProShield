// src/main/java/com/snazzyatoms/proshield/plots/PlotListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

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
    public void onMobSpawn(EntitySpawnEvent event) {
        Plot plot = plotManager.getPlot(event.getLocation());
        if (plot != null && plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            if (event.getEntity() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot != null && plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            if (event.getDamager() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot != null && plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            if (event.getEntity() instanceof Monster) {
                event.setCancelled(true);
            }
        }
    }
}
