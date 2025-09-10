package com.snazzyatoms.proshield.plots;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a single claimed chunk.
 * Each plot stores its owner, trusted players, roles, and per-claim settings.
 */
public class Plot {

    private final UUID owner;
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    private final Set<UUID> trustedPlayers = new HashSet<>();

    // Per-claim settings (merged from PlotSettings)
    private final PlotSettings settings;

    // === NEW: Per-claim item keep & item protection toggles ===
    private boolean keepItemsEnabled;
    private boolean itemProtectionEnabled;

    public Plot(UUID owner, String worldName, int chunkX, int chunkZ) {
        this.owner = owner;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.settings = new PlotSettings();
        this.keepItemsEnabled = false;         // default -> inherit global config
        this.itemProtectionEnabled = true;     // default -> inherit global config
    }

    public UUID getOwner() {
        return owner;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public PlotSettings getSettings() {
        return settings;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public void addTrustedPlayer(UUID uuid) {
        trustedPlayers.add(uuid);
    }

    public void removeTrustedPlayer(UUID uuid) {
        trustedPlayers.remove(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return trustedPlayers.contains(uuid);
    }

    // === NEW GETTERS/SETTERS ===

    public boolean isKeepItemsEnabled() {
        return keepItemsEnabled;
    }

    public void setKeepItemsEnabled(boolean keepItemsEnabled) {
        this.keepItemsEnabled = keepItemsEnabled;
    }

    public boolean isItemProtectionEnabled() {
        return itemProtectionEnabled;
    }

    public void setItemProtectionEnabled(boolean itemProtectionEnabled) {
        this.itemProtectionEnabled = itemProtectionEnabled;
    }
}
