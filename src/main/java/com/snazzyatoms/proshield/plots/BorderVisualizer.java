// src/main/java/com/snazzyatoms/proshield/plots/BorderVisualizer.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * BorderVisualizer
 *
 * ✅ Fixed:
 * - Replaced Particle.REDSTONE (deprecated/removed) with DustOptions.
 * - Preserves prior logic: draws red particle borders around claims.
 */
public class BorderVisualizer {

    private final Set<Player> active = new HashSet<>();

    public void toggle(Player player, Plot plot) {
        if (active.contains(player)) {
            active.remove(player);
            player.sendMessage("§cBorder preview disabled.");
        } else {
            active.add(player);
            player.sendMessage("§aBorder preview enabled.");
            showBorder(player, plot);
        }
    }

    private void showBorder(Player player, Plot plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        if (world == null) return;

        int bx = plot.getX() << 4;
        int bz = plot.getZ() << 4;

        // Use red dust particle instead of removed REDSTONE constant
        Particle.DustOptions dust = new Particle.DustOptions(org.bukkit.Color.RED, 1.5F);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!active.contains(player) || !player.isOnline()) {
                    cancel();
                    return;
                }

                // Spawn border particles
                for (int i = 0; i < 16; i++) {
                    Location l1 = new Location(world, bx + i, player.getLocation().getY(), bz);
                    Location l2 = new Location(world, bx + i, player.getLocation().getY(), bz + 16);
                    Location l3 = new Location(world, bx, player.getLocation().getY(), bz + i);
                    Location l4 = new Location(world, bx + 16, player.getLocation().getY(), bz + i);

                    player.spawnParticle(Particle.REDSTONE, l1.add(new Vector(0.5, 0, 0.5)), 1, dust);
                    player.spawnParticle(Particle.REDSTONE, l2.add(new Vector(0.5, 0, 0.5)), 1, dust);
                    player.spawnParticle(Particle.REDSTONE, l3.add(new Vector(0.5, 0, 0.5)), 1, dust);
                    player.spawnParticle(Particle.REDSTONE, l4.add(new Vector(0.5, 0, 0.5)), 1, dust);
                }

                ticks++;
                if (ticks > 100) { // ~5 seconds
                    cancel();
                    active.remove(player);
                    player.sendMessage("§cBorder preview ended.");
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("ProShield"), 0L, 10L);
    }
}
