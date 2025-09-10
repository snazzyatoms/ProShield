package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import java.util.UUID;

/**
 * Represents a single claimed plot of land.
 * Holds ownership, settings, and trusted players/roles.
 */
public class Plot {

    private final Chunk chunk;
    private final UUID owner;

    private final PlotSettings settings;

    public Plot(Chunk chunk, UUID owner) {
        this.chunk = chunk;
        this.owner = owner;
        this.settings = new PlotSettings();
    }

    public Chunk getChunk() {
        return chunk;
    }

    public UUID getOwner() {
        return owner;
    }

    public PlotSettings getSettings() {
        return settings;
    }

    // === Shortcut helpers for flags ===

    public boolean isPvpEnabled() {
        return settings.isPvpEnabled();
    }

    public void setPvpEnabled(boolean enabled) {
        settings.setPvpEnabled(enabled);
    }

    public boolean isExplosionsEnabled() {
        return settings.isExplosionsEnabled();
    }

    public void setExplosionsEnabled(boolean enabled) {
        settings.setExplosionsEnabled(enabled);
    }

    public boolean isFireEnabled() {
        return settings.isFireEnabled();
    }

    public void setFireEnabled(boolean enabled) {
        settings.setFireEnabled(enabled);
    }

    // === Keep Items (NEW) ===

    /**
     * @return Boolean flag if explicitly set, otherwise null (use global fallback).
     */
    public Boolean getKeepItemsEnabled() {
        return settings.getKeepItemsEnabled();
    }

    /**
     * @param enabled true = force keep items in this claim,
     *                false = force disable,
     *                null = fallback to global config.
     */
    public void setKeepItemsEnabled(Boolean enabled) {
        settings.setKeepItemsEnabled(enabled);
    }
}
