// path: src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a claimed plot (chunk).
 * - Preserves all existing functionality
 * - Extended with new PlotSettings flags (keepItems, redstone, etc.)
 */
public class Plot implements Serializable {

    private final UUID owner;
    private final String worldName;
    private final int x;
    private final int z;
    private final PlotSettings settings;

    private final Set<UUID> trustedPlayers;

    public Plot(UUID owner, Chunk chunk) {
        this.owner = owner;
        this.worldName = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.settings = new PlotSettings();
        this.trustedPlayers = new HashSet<>();
    }

    // === Getters ===
    public UUID getOwner() {
        return owner;
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

    public PlotSettings getSettings() {
        return settings;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    // === Helpers ===
    public boolean isTrusted(UUID player) {
        return trustedPlayers.contains(player);
    }

    public void trust(UUID player) {
        trustedPlayers.add(player);
    }

    public void untrust(UUID player) {
        trustedPlayers.remove(player);
    }

    public boolean isChunk(Chunk chunk) {
        return worldName.equals(chunk.getWorld().getName()) &&
               x == chunk.getX() &&
               z == chunk.getZ();
    }

    // === Forward Settings for convenience ===
    public boolean isPvpEnabled() { return settings.isPvpEnabled(); }
    public boolean isKeepItemsEnabled() { return settings.isKeepItemsEnabled(); }
    public boolean isRedstoneEnabled() { return settings.isRedstoneEnabled(); }
    public boolean isContainerAccessEnabled() { return settings.isContainerAccessEnabled(); }
    public boolean isAnimalAccessEnabled() { return settings.isAnimalAccessEnabled(); }
}
