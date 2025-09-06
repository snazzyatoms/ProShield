package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** A single chunk-based claim */
public class Claim {
    private final UUID owner;
    private final String world;
    private final int chunkX;
    private final int chunkZ;
    private final long createdAt;            // epoch millis
    private final Set<UUID> trusted = new HashSet<>();

    public Claim(UUID owner, String world, int chunkX, int chunkZ, long createdAt) {
        this.owner = owner;
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.createdAt = createdAt;
    }

    public UUID getOwner() { return owner; }
    public String getWorld() { return world; }
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public long getCreatedAt() { return createdAt; }
    public Set<UUID> getTrusted() { return trusted; }

    public boolean contains(Location loc) {
        World w = Bukkit.getWorld(world);
        if (w == null || !w.equals(loc.getWorld())) return false;
        return loc.getChunk().getX() == chunkX && loc.getChunk().getZ() == chunkZ;
    }

    public String key() { return world + ":" + chunkX + ":" + chunkZ; }
}
