// src/main/java/com/snazzyatoms/proshield/plots/EntityMobRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityMobRepelTask extends BukkitRunnable {
    private final ProShield plugin;
    private final PlotManager plotManager;

    public EntityMobRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @Override
    public void run() {
        // Legacy placeholder: can be removed once MobProtectionListener fully replaces it
    }
}
