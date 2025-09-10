package com.snazzyatoms.proshield.plots;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a claimed chunk (a "plot").
 * Stores owner, trusted players, roles, and settings.
 */
public class Plot {

    private final UUID owner;                // Claim owner
    private final String world;              // World name
    private final int x;                     // Chunk X
    private final int z;                     // Chunk Z

    private final Set<UUID> trusted = new HashSet<>();
    private final PlotSettings settings;     // Per-claim settings (PvP, keep-drops, etc.)

    public Plot(UUID owner, String world, int x, int z) {
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.z = z;
        this.settings = new PlotSettings(); // Initialize with defaults
    }

    // === Basic Getters ===
    public UUID getOwner() {
        return owner;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    // === Trusted Players ===
    public Set<UUID> getTrusted() {
        return trusted;
    }

    public void addTrusted(UUID player) {
        trusted.add(player);
    }

    public void removeTrusted(UUID player) {
        trusted.remove(player);
    }

    public boolean isTrusted(UUID player) {
        return trusted.contains(player);
    }

    // === Plot Settings ===
    public PlotSettings getSettings() {
        return settings;
    }

    // === Utility ===
    @Override
    public String toString() {
        return "Plot{" +
                "owner=" + owner +
                ", world='" + world + '\'' +
                ", x=" + x +
                ", z=" + z +
                ", trusted=" + trusted.size() +
                ", settings=" + settings +
                '}';
    }

    public String getKey() {
        return world + ":" + x + "," + z;
    }
}
