package com.snazzyatoms.proshield.plots;

import java.util.*;

/**
 * Represents a single claimed plot (one chunk).
 * Preserves owner, trusted players, and plot settings.
 * Extended with safe helpers and convenience methods.
 */
public class Plot {

    private final UUID owner;
    private final Set<UUID> trusted;
    private final PlotSettings settings;

    public Plot(UUID owner) {
        this.owner = owner;
        this.trusted = new HashSet<>();
        this.settings = new PlotSettings(); // always initialize defaults
    }

    /* ---------------------------------------------------------
     * Core Ownership
     * --------------------------------------------------------- */

    public UUID getOwner() {
        return owner;
    }

    public boolean isOwner(UUID playerId) {
        return playerId != null && owner.equals(playerId);
    }

    /* ---------------------------------------------------------
     * Trusted Players
     * --------------------------------------------------------- */

    public Set<UUID> getTrusted() {
        return Collections.unmodifiableSet(trusted);
    }

    public void addTrusted(UUID playerId) {
        if (playerId != null) {
            trusted.add(playerId);
        }
    }

    public void removeTrusted(UUID playerId) {
        if (playerId != null) {
            trusted.remove(playerId);
        }
    }

    public boolean isTrusted(UUID playerId) {
        return playerId != null && (owner.equals(playerId) || trusted.contains(playerId));
    }

    /* ---------------------------------------------------------
     * Plot Settings (flags)
     * --------------------------------------------------------- */

    public PlotSettings getSettings() {
        return settings;
    }

    /* ---------------------------------------------------------
     * Display & Debug Helpers
     * --------------------------------------------------------- */

    /**
     * Returns a safe display name for the claim.
     * Used in messages to avoid "getName() missing" errors.
     */
    public String getDisplayNameSafe() {
        return owner != null ? owner.toString().substring(0, 8) : "Unnamed Claim";
    }

    @Override
    public String toString() {
        return "Plot{" +
                "owner=" + owner +
                ", trusted=" + trusted +
                ", settings=" + settings +
                '}';
    }
}
