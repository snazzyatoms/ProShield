// path: src/main/java/com/snazzyatoms/proshield/plots/Claim.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;

import java.util.UUID;

/**
 * A simple "chunk claim" model: each claim locks 1 chunk to its owner.
 */
public class Claim {
    private final UUID owner;
    private final String world;
    private final int chunkX;
    private final int chunkZ;

    public Claim(UUID owner, String world, int chunkX, int chunkZ) {
        this.owner = owner;
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public UUID getOwner() { return owner; }
    public String getWorld() { return world; }
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }

    public static String key(String world, int chunkX, int chunkZ) {
        return world + ":" + chunkX + "_" + chunkZ;
    }

    public static String key(Chunk chunk) {
        return key(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    @Override
    public String toString() {
        return "Claim{" + "owner=" + owner + ", world='" + world + '\'' +
                ", chunkX=" + chunkX + ", chunkZ=" + chunkZ + '}';
    }
}
