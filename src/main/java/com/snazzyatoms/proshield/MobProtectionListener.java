// src/main/java/com/snazzyatoms/proshield/listeners/MobProtectionListener.java
package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class MobProtectionListener implements Listener {

    private final ProShield plugin;

    public MobProtectionListener(ProShield plugin) {
        this.plugin = plugin;
    }

    private boolean debug() {
        FileConfiguration cfg = plugin.getConfig();
        return cfg.getBoolean("messages.admin-flag-chat", true); // reuse toggle
    }

    @EventHandler
    public void onMobTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;

        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) return;

        if (plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            event.setCancelled(true);
            if (debug()) {
                plugin.getLogger().info("[Debug] " + event.getEntityType() + " prevented from targeting " 
                        + player.getName() + " in safe claim " + plot.getId());
            }
        }
    }

    @EventHandler
    public void onMobDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        if (plot == null) return;

        if (plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
            if (event.getDamager() instanceof Monster) {
                event.setCancelled(true);
                if (debug()) {
                    plugin.getLogger().info("[Debug] Prevented mob attack on " + player.getName() + " inside safe claim.");
                }
            }
        }
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;

        Plot plot = plugin.getPlotManager().getPlot(event.getLocation());
        if (plot == null) return;

        if (plot.getFlag("mob-despawn", plugin.getConfig().getBoolean("claims.default-flags.mob-despawn", true))) {
            event.setCancelled(true);
            if (debug()) {
                plugin.getLogger().info("[Debug] Prevented " + mob.getType() + " from spawning inside safe claim.");
            }
        }
    }
}
