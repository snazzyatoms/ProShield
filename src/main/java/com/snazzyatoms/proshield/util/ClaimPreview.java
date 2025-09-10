package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Lightweight chunk border preview using particles.
 * Removes dependency on ProShield.getInstance() – we acquire the plugin via Bukkit.
 * Preserves previous concept; call ClaimPreview.show(player, player.getLocation()).
 */
public final class ClaimPreview {

    private ClaimPreview() {}

    private static ProShield plugin() {
        Plugin p = Bukkit.getPluginManager().getPlugin("ProShield");
        return (ProShield) p;
    }

    public static void show(Player player, Location center) {
        if (player == null || center == null) return;
        ProShield plugin = plugin();
        if (plugin == null) return;

        World world = center.getWorld();
        if (world == null) return;

        Chunk chunk = center.getChunk();
        int minX = chunk.getX() << 4;
        int minZ = chunk.getZ() << 4;
        int y = Math.max(64, center.getBlockY());

        // draw a simple square with particles
        for (int x = minX; x < minX + 16; x+=1) {
            spawnParticle(world, x + 0.5, y, minZ + 0.5, player);
            spawnParticle(world, x + 0.5, y, minZ + 15.5, player);
        }
        for (int z = minZ; z < minZ + 16; z+=1) {
            spawnParticle(world, minX + 0.5, y, z + 0.5, player);
            spawnParticle(world, minX + 15.5, y, z + 0.5, player);
        }
    }

    private static void spawnParticle(World world, double x, double y, double z, Player player) {
        world.spawnParticle(Particle.VILLAGER_HAPPY, x, y, z, 1, 0, 0, 0, 0.0);
    }
}
