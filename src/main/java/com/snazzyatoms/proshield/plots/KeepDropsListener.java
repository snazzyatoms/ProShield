// path: src/main/java/com/snazzyatoms/proshield/plots/KeepDropsListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KeepDropsListener implements Listener {

    private final ProShield plugin = ProShield.getInstance();
    private final PlotManager plotManager;

    private final Map<UUID, Long> spawnTimes = new ConcurrentHashMap<>();
    private int retentionSeconds;
    private boolean featureEnabled;

    public KeepDropsListener(PlotManager plotManager) {
        this.plotManager = plotManager;
        reloadConfigWindow();
    }

    public void reloadConfigWindow() {
        this.featureEnabled   = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        int cfg = plugin.getConfig().getInt("claims.keep-items.retention-seconds", 600);
        if (cfg < 300) cfg = 300;
        if (cfg > 900) cfg = 900;
        this.retentionSeconds = cfg;
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        if (!featureEnabled) return;
        Location loc = e.getLocation();
        if (!plotManager.isClaimed(loc)) return;
        spawnTimes.put(e.getEntity().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent e) {
        if (!featureEnabled) return;
        Location loc = e.getLocation();
        if (!plotManager.isClaimed(loc)) return;

        UUID id = e.getEntity().getUniqueId();
        Long t0 = spawnTimes.get(id);
        if (t0 == null) {
            // If we didn't catch the spawn (e.g., plugin reload), treat now as start and prevent one cycle.
            spawnTimes.put(id, System.currentTimeMillis());
            e.setCancelled(true);
            return;
        }
        long livedMs = System.currentTimeMillis() - t0;
        if (livedMs < (long) retentionSeconds * 1000L) {
            // Not yet allowed to despawn → keep it
            e.setCancelled(true);
        } else {
            // Let it despawn; cleanup
            spawnTimes.remove(id);
        }
    }
}
