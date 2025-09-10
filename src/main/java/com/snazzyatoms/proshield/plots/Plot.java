package com.snazzyatoms.proshield.plots;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a claimed chunk.
 * Stores ownership, trusted players, and per-claim settings.
 */
public class Plot {

    private final UUID owner;
    private final String key;
    private final Map<UUID, String> trusted;
    private PlotSettings settings;

    public Plot(UUID owner, String key) {
        this.owner = owner;
        this.key = key;
        this.trusted = new HashMap<>();
        this.settings = new PlotSettings(); // defaults applied
    }

    // === Core Info ===
    public UUID getOwner() {
        return owner;
    }

    public String getKey() {
        return key;
    }

    // === Trusted Players ===
    public Map<UUID, String> getTrusted() {
        return trusted;
    }

    public void addTrusted(UUID player, String role) {
        trusted.put(player, role);
    }

    public void removeTrusted(UUID player) {
        trusted.remove(player);
    }

    public boolean isTrusted(UUID player) {
        return trusted.containsKey(player);
    }

    public String getRole(UUID player) {
        return trusted.getOrDefault(player, "Visitor");
    }

    // === Settings ===
    public PlotSettings getSettings() {
        return settings;
    }

    public void setSettings(PlotSettings settings) {
        this.settings = settings;
    }

    // === Convenience Checks for Flags ===
    public boolean isPvpEnabled() {
        return settings.isPvpEnabled();
    }

    public boolean isExplosionsEnabled() {
        return settings.isExplosionsEnabled();
    }

    public boolean isFireEnabled() {
        return settings.isFireEnabled();
    }

    public boolean isMobGriefEnabled() {
        return settings.isMobGriefEnabled();
    }

    public boolean isKeepItemsEnabled() {
        return settings.isKeepItemsEnabled();
    }

    public boolean isRedstoneEnabled() {
        return settings.isRedstoneEnabled();
    }

    public boolean isContainerAccessEnabled() {
        return settings.isContainerAccessEnabled();
    }

    public boolean isAnimalInteractEnabled() {
        return settings.isAnimalInteractEnabled();
    }
}
