// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Plot
 * ----
 * Represents a claimed chunk/area.
 * Supports trusted players, flags, and expansion radius.
 */
public class Plot {
    private final UUID id;
    private UUID owner; // ⚡ must not be final if we allow transfer
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    private final Map<String, Boolean> flags = new HashMap<>();
    private final Set<UUID> trusted = new HashSet<>();

    // Expansion radius (default = 0 means just the chunk itself)
    private int extraRadius = 0;

    // Existing constructor
    public Plot(UUID owner, String worldName, int chunkX, int chunkZ) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    // ✅ New convenience constructor for Bukkit Chunk
    public Plot(UUID owner, Chunk chunk) {
        this(owner, chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    // --- Core info ---
    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID newOwner) {
        this.owner = newOwner;
    }

    public boolean isOwner(UUID playerId) {
        return owner != null && owner.equals(playerId);
    }

    // --- Trusted Players ---
    public boolean isTrusted(UUID playerId) {
        return isOwner(playerId) || trusted.contains(playerId);
    }

    public void addTrusted(UUID playerId) {
        trusted.add(playerId);
    }

    public void removeTrusted(UUID playerId) {
        trusted.remove(playerId);
    }

    public Set<UUID> getTrusted() {
        return trusted;
    }

    // --- Location info ---
    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return chunkX;
    }

    public int getZ() {
        return chunkZ;
    }

    // --- Flags ---
    public boolean getFlag(String flag, boolean def) {
        return flags.getOrDefault(flag, def);
    }

    public void setFlag(String flag, boolean value) {
        flags.put(flag, value);
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    // --- Expansion ---
    public int getExtraRadius() {
        return extraRadius;
    }

    public void expand(int extra) {
        if (extra > 0) {
            this.extraRadius += extra;
        }
    }

    // --- Utility ---
    @Override
    public String toString() {
        return "Plot{" +
                "owner=" + owner +
                ", world='" + worldName + '\'' +
                ", chunk=(" + chunkX + "," + chunkZ + ")" +
                ", extraRadius=" + extraRadius +
                ", flags=" + flags +
                ", trusted=" + trusted +
                '}';
    }
}
