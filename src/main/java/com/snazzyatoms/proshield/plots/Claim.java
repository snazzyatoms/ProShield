package com.snazzyatoms.proshield.plots;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Claim {
    private final UUID owner;
    private final String world;
    private final int chunkX;
    private final int chunkZ;
    private final long createdAt;
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

    public String key() {
        return world + ":" + chunkX + ":" + chunkZ;
    }
}
