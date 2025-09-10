// ===========================================
// MobBorderRepelListener.java
// ===========================================
package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class MobBorderRepelListener implements Listener {
    private final PlotManager plots;

    public MobBorderRepelListener(PlotManager plots) {
        this.plots = plots;
        startRepelTask();
    }

    private void startRepelTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (LivingEntity mob : Bukkit.getWorlds().stream().flatMap(w -> w.getLivingEntities().stream()).toList()) {
                    if (plots.isInsideClaim(mob.getLocation()) && !plots.isAllowedMob(mob)) {
                        mob.remove(); // despawn mobs inside claims
                    } else {
                        Location border = plots.getNearbyClaimBorder(mob.getLocation());
                        if (border != null) {
                            mob.setVelocity(mob.getLocation().toVector().subtract(border.toVector()).normalize().multiply(1.5));
                        }
                    }
                }
            }
        }.runTaskTimer(plots.getPlugin(), 20, 20);
    }
}
