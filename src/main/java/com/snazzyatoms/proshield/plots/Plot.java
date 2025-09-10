package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a land claim (a single chunk).
 * Stores owner, trusted players, roles, and claim-specific settings.
 */
public class Plot {

    private final Chunk chunk;
    private final UUID owner;
    private final Set<UUID> trusted = new HashSet<>();
    private final PlotSettings settings = new PlotSettings();

    public Plot(Chunk chunk, UUID owner, ProShield plugin) {
        this.chunk = chunk;
        this.owner = owner;

        // Initialize per-claim settings from global config
        this.settings.initDefaults(plugin);
    }

    public Chunk getChunk() {
        return chunk;
    }

    public UUID getOwner() {
        return owner;
    }

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

    // === Claim settings ===
    public PlotSettings getSettings() {
        return settings;
    }

    // === Serialization (example methods for persistence) ===
    public String serializeChunk() {
        return chunk.getWorld().getName() + ":" + chunk.getX() + "," + chunk.getZ();
    }

    @Override
    public String toString() {
        return "Plot{" +
                "chunk=" + serializeChunk() +
                ", owner=" + owner +
                ", trusted=" + trusted +
                ", settings=" + settings +
                '}';
    }
}
