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
 * - Stores owner, trusted players, and roles
 * - Used by PlotManager and GUI
 *
 * Fixed for v1.2.5 to add getX(), getZ(), getWorldName(),
 * getOwnerName()/setOwnerName() so other classes compile.
 */
public class Plot {

    private final UUID id;
    private UUID owner;
    private String ownerName; // cached for GUIs/messages
    private final String worldName;
    private final int x;
    private final int z;

    // Trusted players (by UUID)
    private final Set<UUID> trusted = new HashSet<>();

    public Plot(Chunk chunk, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.owner = ownerId;
        this.worldName = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    // --- Identification ---
    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID newOwner) {
        this.owner = newOwner;
    }

    // Cached owner name (for GUIs, avoids offline lookups)
    public String getOwnerName() {
        return ownerName != null ? ownerName : owner.toString();
    }

    public void setOwnerName(String name) {
        this.ownerName = name;
    }

    // --- World / location ---
    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    // --- Trust ---
    public void addTrusted(UUID playerId) {
        trusted.add(playerId);
    }

    public void removeTrusted(UUID playerId) {
        trusted.remove(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return trusted.contains(playerId);
    }

    // --- Ownership helpers ---
    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
    }

    public String getDisplayNameSafe() {
        return getOwnerName() + "'s Claim";
    }

    // --- Chunk reference ---
    public Chunk getChunk(World world) {
        return world.getChunkAt(x, z);
    }
}
