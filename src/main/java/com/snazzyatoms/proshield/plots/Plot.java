package com.snazzyatoms.proshield.plots;

import java.util.*;

/**
 * Represents a single claimed plot of land (one chunk).
 * Stores owner, trusted players, settings, and human-readable name.
 */
public class Plot {

    private final UUID owner;
    private final String name;
    private final Set<UUID> trusted;
    private final PlotSettings settings;

    public Plot(UUID owner, String name) {
        this.owner = owner;
        this.name = (name != null && !name.isEmpty()) ? name : "Unnamed Plot";
        this.trusted = new HashSet<>();
        this.settings = new PlotSettings();
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public PlotSettings getSettings() {
        return settings;
    }

    public Set<UUID> getTrusted() {
        return Collections.unmodifiableSet(trusted);
    }

    public void addTrusted(UUID uuid) {
        trusted.add(uuid);
    }

    public void removeTrusted(UUID uuid) {
        trusted.remove(uuid);
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return trusted.contains(uuid);
    }

    // Flag helpers (delegate to settings)
    public boolean isFlagEnabled(String flag) {
        return settings.isFlagEnabled(flag);
    }

    public void setFlag(String flag, boolean value) {
        settings.setFlag(flag, value);
    }
}
