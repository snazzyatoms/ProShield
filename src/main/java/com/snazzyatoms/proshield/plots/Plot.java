// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;

import org.bukkit.Chunk;

import java.util.*;

/**
 * Plot
 * - Represents a claimed chunk.
 * - Stores owner, trusted players (with roles), flags.
 * - Unified model for v1.2.0 → v1.2.5.
 */
public class Plot {

    private final UUID id;
    private final Chunk chunk;
    private final UUID owner;

    // trusted players → role
    private final Map<UUID, ClaimRole> trusted = new HashMap<>();

    // claim flags
    private final Map<String, Boolean> flags = new HashMap<>();

    // Optional display name (fallback = owner’s name or id)
    private String displayName;

    public Plot(Chunk chunk, UUID owner) {
        this.id = UUID.randomUUID();
        this.chunk = chunk;
        this.owner = owner;

        // default flags (can be overridden via config or GUI)
        flags.put("pvp", false);
        flags.put("explosions", false);
        flags.put("fire", false);
        flags.put("entity-grief", false);
        flags.put("redstone", true);
        flags.put("containers", false);
        flags.put("animals", false);
        flags.put("vehicles", false);
        flags.put("armor-stands", false);
        flags.put("item-frames", false);
        flags.put("buckets", false);
        flags.put("pets", false);
        flags.put("mob-repel", true);
        flags.put("mob-despawn", true);
    }

    /* -------------------------------------------------------
     * Identity
     * ------------------------------------------------------- */
    public UUID getId() {
        return id;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isOwner(UUID playerId) {
        return playerId != null && playerId.equals(owner);
    }

    /* -------------------------------------------------------
     * Trusted / Roles
     * ------------------------------------------------------- */
    public void setRole(UUID playerId, ClaimRole role) {
        if (playerId == null || role == null) return;
        trusted.put(playerId, role);
    }

    public ClaimRole getRole(UUID playerId) {
        return trusted.getOrDefault(playerId, ClaimRole.NONE);
    }

    public void clearRole(UUID playerId) {
        trusted.remove(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return trusted.containsKey(playerId);
    }

    public Set<UUID> getTrustedPlayers() {
        return Collections.unmodifiableSet(trusted.keySet());
    }

    public Map<UUID, ClaimRole> getTrustedRoles() {
        return Collections.unmodifiableMap(trusted);
    }

    /* -------------------------------------------------------
     * Flags
     * ------------------------------------------------------- */
    public boolean getFlag(String flag) {
        return flags.getOrDefault(flag.toLowerCase(), false);
    }

    public void setFlag(String flag, boolean value) {
        flags.put(flag.toLowerCase(), value);
    }

    public Map<String, Boolean> getAllFlags() {
        return Collections.unmodifiableMap(flags);
    }

    /* -------------------------------------------------------
     * Display Name
     * ------------------------------------------------------- */
    public String getDisplayNameSafe() {
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }
        return "Claim " + id.toString().substring(0, 8);
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    /* -------------------------------------------------------
     * Utility
     * ------------------------------------------------------- */
    @Override
    public String toString() {
        return "Plot{" +
                "id=" + id +
                ", owner=" + owner +
                ", chunk=" + chunk.getX() + "," + chunk.getZ() +
                ", trusted=" + trusted.size() +
                ", flags=" + flags +
                '}';
    }
}
