package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a claimed plot of land in ProShield.
 * Holds the chunk + owner UUID, trusted players, and settings.
 */
public class Plot {

    private final Chunk chunk;
    private UUID owner;
    private final Set<UUID> trusted = new HashSet<>();
    private final PlotSettings settings;

    public Plot(Chunk chunk, UUID owner) {
        this.chunk = chunk;
        this.owner = owner;
        this.settings = new PlotSettings();
    }

    // --- Core Getters ---

    public Chunk getChunk() {
        return chunk;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID newOwner) {
        this.owner = newOwner;
    }

    public String getName() {
        return owner != null ? owner.toString() : "Unowned";
    }

    public String getWorldName() {
        return chunk.getWorld().getName();
    }

    public int getX() {
        return chunk.getX();
    }

    public int getZ() {
        return chunk.getZ();
    }

    public PlotSettings getSettings() {
        return settings;
    }

    // --- Trusted Players ---

    public void addTrusted(UUID player) {
        trusted.add(player);
    }

    public void removeTrusted(UUID player) {
        trusted.remove(player);
    }

    public boolean isTrusted(UUID player) {
        return trusted.contains(player);
    }

    public Set<UUID> getTrusted() {
        return trusted;
    }

    public Set<String> getTrustedNames() {
        Set<String> names = new HashSet<>();
        for (UUID id : trusted) {
            names.add(id.toString()); // Could hook into Bukkit.getOfflinePlayer(id).getName()
        }
        return names;
    }
}
