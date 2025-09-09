// path: src/main/java/com/snazzyatoms/proshield/plots/Claim.java
package com.snazzyatoms.proshield.plots;

import java.util.*;

public class Claim {
    private UUID owner;
    private final String world;
    private final int chunkX;
    private final int chunkZ;
    private final long createdAt;

    // Optional: per-player role storage (stringy for now; manager resolves enum)
    private final Map<UUID, String> roles = new HashMap<>();
    // Original trusted set maintained for backward compatibility
    private final Set<UUID> trusted = new HashSet<>();

    public Claim(UUID owner, String world, int chunkX, int chunkZ, long createdAt) {
        this.owner = owner;
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.createdAt = createdAt;
    }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID newOwner) { this.owner = newOwner; }

    public String getWorld() { return world; }
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public long getCreatedAt() { return createdAt; }

    public Set<UUID> getTrusted() { return trusted; }
    public Map<UUID, String> getRoles() { return roles; }

    /** Config storage key: world:chunkX:chunkZ */
    public String key() { return world + ":" + chunkX + ":" + chunkZ; }
}
