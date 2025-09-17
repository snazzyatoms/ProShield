package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * ClaimPreview
 * - Shows border particles around a claim temporarily
 * - Fixed: inner class now uses final locals
 */
public class ClaimPreview {

    private final ProShield plugin;
    private final UUID playerId;
    private final World world;
    private final int x;
    private final int z;
    private final int radius;

    private BukkitTask task;

    public ClaimPreview(ProShield plugin, UUID playerId, World world, int x, int z, int radius) {
        this.plugin = plugin;
        this.playerId = playerId;
        this.world = world;
        this.x = x;
        this.z = z;
        this.radius = radius;
    }

    public void start() {
        final UUID uuid = playerId;
        final World w = world;
        final int cx = x;
        final int cz = z;
        final int r = radius;

        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Location center = new Location(w, cx << 4, w.getHighestBlockYAt(cx << 4, cz << 4), cz << 4);

            Set<Location> border = new HashSet<>();
            for (int dx = -r; dx <= r; dx++) {
                border.add(center.clone().add(dx, 0, -r));
                border.add(center.clone().add(dx, 0, r));
            }
            for (int dz = -r; dz <= r; dz++) {
                border.add(center.clone().add(-r, 0, dz));
                border.add(center.clone().add(r, 0, dz));
            }

            for (Location loc : border) {
                w.spawnParticle(Particle.VILLAGER_HAPPY, loc.add(0.5, 1, 0.5), 1);
            }

        }, 0L, 20L); // run every second
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}
