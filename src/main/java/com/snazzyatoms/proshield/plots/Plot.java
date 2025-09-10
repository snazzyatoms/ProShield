package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a land claim (plot).
 * - Stores owner, trusted players, and role data
 * - Stores claim-specific settings (merged with global config defaults)
 */
public class Plot {

    private final UUID owner;
    private final Chunk chunk;
    private final Set<UUID> trustedPlayers;
    private final PlotSettings settings;

    public Plot(UUID owner, Chunk chunk) {
        this.owner = owner;
        this.chunk = chunk;
        this.trustedPlayers = new HashSet<>();
        this.settings = new PlotSettings(); // defaults applied
    }

    public UUID getOwner() {
        return owner;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public Location getCenter() {
        return chunk.getBlock(8, chunk.getWorld().getHighestBlockYAt(chunk.getBlock(8, 0, 8).getLocation()), 8).getLocation();
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public void addTrusted(UUID playerId) {
        trustedPlayers.add(playerId);
    }

    public void removeTrusted(UUID playerId) {
        trustedPlayers.remove(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return trustedPlayers.contains(playerId);
    }

    public PlotSettings getSettings() {
        return settings;
    }

    // === NEW EXTENSIONS ===

    /**
     * Returns whether item-keep is enabled in this claim.
     * Falls back to global config if not overridden.
     */
    public boolean isKeepItemsEnabled() {
        return settings.isKeepItemsEnabled();
    }

    /**
     * Sets the keep-items flag for this claim.
     */
    public void setKeepItemsEnabled(boolean enabled) {
        settings.setKeepItemsEnabled(enabled);
    }

    /**
     * Returns whether PvP is enabled in this claim.
     * Used by PvpProtectionListener.
     */
    public boolean isPvpEnabled() {
        return settings.isPvpEnabled();
    }

    public void setPvpEnabled(boolean enabled) {
        settings.setPvpEnabled(enabled);
    }

    /**
     * Returns whether explosions are allowed in this claim.
     */
    public boolean isExplosionsEnabled() {
        return settings.isExplosionsEnabled();
    }

    public void setExplosionsEnabled(boolean enabled) {
        settings.setExplosionsEnabled(enabled);
    }

    /**
     * Returns whether fire is allowed in this claim.
     */
    public boolean isFireEnabled() {
        return settings.isFireEnabled();
    }

    public void setFireEnabled(boolean enabled) {
        settings.setFireEnabled(enabled);
    }

    /**
     * Returns whether mob griefing is allowed in this claim.
     */
    public boolean isMobGriefEnabled() {
        return settings.isMobGriefEnabled();
    }

    public void setMobGriefEnabled(boolean enabled) {
        settings.setMobGriefEnabled(enabled);
    }

    @Override
    public String toString() {
        return "Plot{" +
                "owner=" + owner +
                ", chunk=" + chunk +
                ", trustedPlayers=" + trustedPlayers +
                ", settings=" + settings +
                '}';
    }
}
