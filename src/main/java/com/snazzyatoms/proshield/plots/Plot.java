// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Plot
 * - Represents a single claimed chunk
 * - Stores owner, trusted players, and metadata
 *
 * Fixed for v1.2.5 so all managers & tasks compile.
 */
public class Plot {

    private final UUID id;
    private final UUID ownerId;
    private final String worldName;
    private final int x;
    private final int z;

    private String ownerName; // cache of owner's last known name
    private final Set<UUID> trusted = new HashSet<>();

    public Plot(Chunk chunk, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.worldName = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    // -------------------------------
    // Basic Getters
    // -------------------------------
    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    // -------------------------------
    // Owner Name (cached)
    // -------------------------------
    public String getOwnerName() {
        return ownerName != null ? ownerName : ownerId.toString();
    }

    public void setOwnerName(String name) {
        this.ownerName = name;
    }

    // -------------------------------
    // Trusted Players
    // -------------------------------
    public Set<UUID> getTrusted() {
        return trusted;
    }

    public boolean isOwner(UUID playerId) {
        return ownerId.equals(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return trusted.contains(playerId);
    }

    // -------------------------------
    // Utility
    // -------------------------------
    public String getDisplayNameSafe() {
        return getOwnerName() + "'s Claim";
    }
}
