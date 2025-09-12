// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Chunk;

import java.time.Instant;
import java.util.*;

/**
 * Plot
 * - Represents a single claimed chunk.
 * - Stores owner, trusted players with roles, and metadata.
 * - Now tracks lastActive timestamp for expiry system.
 */
public class Plot {

    private final UUID id;
    private final Chunk chunk;
    private final UUID owner;

    // Trusted player UUID → Role
    private final Map<UUID, ClaimRole> trusted = new HashMap<>();

    // Display name (customizable later)
    private String displayName;

    // Last active timestamp (used for expiry)
    private Instant lastActive;

    public Plot(Chunk chunk, UUID owner) {
        this.id = UUID.randomUUID();
        this.chunk = chunk;
        this.owner = owner;
        this.displayName = "Claim-" + id.toString().substring(0, 8);
        this.lastActive = Instant.now();
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

    public String getDisplayNameSafe() {
        return (displayName != null && !displayName.isEmpty()) ? displayName : "Unnamed Claim";
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<UUID, ClaimRole> getTrusted() {
        return Collections.unmodifiableMap(trusted);
    }

    public Set<String> getTrustedNames() {
        Set<String> names = new HashSet<>();
        for (UUID uuid : trusted.keySet()) {
            names.add(uuid.toString()); // TODO: resolve to player names
        }
        return names;
    }

    public void trust(UUID playerId, ClaimRole role) {
        trusted.put(playerId, role);
        markActive();
    }

    public void untrust(UUID playerId) {
        trusted.remove(playerId);
        markActive();
    }

    public ClaimRole getRole(UUID playerId) {
        return trusted.getOrDefault(playerId, ClaimRole.NONE);
    }

    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
    }

    /* =====================================================
     * Expiry system
     * ===================================================== */
    public Instant getLastActive() {
        return lastActive;
    }

    public void setLastActive(Instant lastActive) {
        this.lastActive = lastActive;
    }

    public void markActive() {
        this.lastActive = Instant.now();
    }
}
