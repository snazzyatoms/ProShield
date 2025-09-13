// src/main/java/com/snazzyatoms/proshield/plots/EntityBorderRepelTask.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityBorderRepelTask extends BukkitRunnable {
    private final ProShield plugin;
    private final PlotManager plotManager;

    public EntityBorderRepelTask(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @Override
    public void run() {
        // Legacy placeholder: can be removed once MobProtectionListener fully replaces it
    }
}
