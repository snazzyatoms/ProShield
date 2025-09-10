package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;

import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * Represents a claimed plot (one chunk).
 * - Stores ownership and trusted players
 * - Delegates protection flags to PlotSettings
 * - Exposes helpers for commands/listeners
 */
public class Plot {

    private UUID owner;
    private final Chunk chunk;
    private final Map<UUID, ClaimRole> trusted = new HashMap<>();
    private PlotSettings settings;

    public Plot(UUID owner, Chunk chunk) {
        this.owner = owner;
        this.chunk = chunk;
        this.settings = new PlotSettings(); // defaults synced with config
    }

    /* ---------------------------------------------------------
     * Core accessors
     * --------------------------------------------------------- */

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public PlotSettings getSettings() {
        return settings;
    }

    public void setSettings(PlotSettings settings) {
        this.settings = settings;
    }

    /**
     * Human-readable name for messages.
     */
    public String getName() {
        OfflinePlayer player = chunk.getWorld().getServer().getOfflinePlayer(owner);
        return player != null && player.getName() != null ? player.getName() + "'s Claim" : "Unowned Claim";
    }

    /* ---------------------------------------------------------
     * Trusted roles
     * --------------------------------------------------------- */

    public Map<UUID, ClaimRole> getTrusted() {
        return Collections.unmodifiableMap(trusted);
    }

    public void trustPlayer(UUID uuid, ClaimRole role) {
        if (uuid == null || role == null) return;
        trusted.put(uuid, role);
    }

    public void removeTrusted(UUID uuid) {
        trusted.remove(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return trusted.containsKey(uuid);
    }

    public ClaimRole getRole(UUID uuid) {
        return trusted.getOrDefault(uuid, ClaimRole.VISITOR);
    }

    /* ---------------------------------------------------------
     * Ownership helpers
     * --------------------------------------------------------- */

    public boolean isOwner(UUID uuid) {
        return owner != null && owner.equals(uuid);
    }

    public boolean isTrustedOrOwner(UUID uuid) {
        return isOwner(uuid) || isTrusted(uuid);
    }

    /* ---------------------------------------------------------
     * Convenience for persistence
     * --------------------------------------------------------- */

    public String serializeKey() {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }
}
