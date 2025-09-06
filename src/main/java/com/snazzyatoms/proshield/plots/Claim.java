package com.snazzyatoms.proshield.plots;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Chunk-based claim descriptor.
 * Note: PlotManager currently stores claims in config as world:chunkX:chunkZ -> owner UUID
 * and does not use this class directly. This class is provided to keep the model consistent
 * and ready for future use if you want to pass Claim objects around.
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

    public UUID getOwner() {
        return owner;
    }

    public String getWorld() {
        return world;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    /** True if the given location is in the same chunk as this claim. */
    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equals(world)) return false;
        return loc.getChunk().getX() == chunkX && loc.getChunk().getZ() == chunkZ;
    }

    @Override
    public String toString() {
        return "Claim{" +
                "owner=" + owner +
                ", world='" + world + '\'' +
                ", chunkX=" + chunkX +
                ", chunkZ=" + chunkZ +
                '}';
    }
}
