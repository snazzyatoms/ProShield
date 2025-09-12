// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Chunk;

import java.util.*;

/**
 * Represents a single land claim ("plot").
 * Stores owner, trusted players, roles, and metadata.
 * Unified from earlier versions (1.2.0 → 1.2.5).
 */
public class Plot {

    private final UUID id;
    private final Chunk chunk;
    private UUID owner;

    // playerId → role
    private final Map<UUID, ClaimRole> trusted = new HashMap<>();

    public Plot(Chunk chunk, UUID owner) {
        this.id = UUID.randomUUID();
        this.chunk = chunk;
        this.owner = owner;
    }

    public UUID getId() {
        return id;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID newOwner) {
        this.owner = newOwner;
    }

    /** Display-friendly claim name (defaults to owner UUID if unknown). */
    public String getDisplayNameSafe() {
        return "Claim-" + id.toString().substring(0, 8);
    }

    /* ------------------------------------------------------
     * Trust & Roles
     * ------------------------------------------------------ */
    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
    }

    public void trust(UUID playerId, ClaimRole role) {
        trusted.put(playerId, role);
    }

    public void untrust(UUID playerId) {
        trusted.remove(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return trusted.containsKey(playerId);
    }

    public ClaimRole getRole(UUID playerId) {
        return trusted.getOrDefault(playerId, ClaimRole.NONE);
    }

    public Set<UUID> getTrustedPlayers() {
        return Collections.unmodifiableSet(trusted.keySet());
    }
}
