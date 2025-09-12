// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Chunk;

import java.util.*;

/**
 * Plot
 * - Represents a single claimed chunk.
 * - Stores owner, trusted players, roles, and metadata.
 * - Now supports lastActive timestamp for expiry/purge.
 */
public class Plot {

    private final UUID id;
    private final Chunk chunk;
    private final UUID owner;

    // Trusted players & roles
    private final Map<UUID, ClaimRole> trusted = new HashMap<>();

    // Metadata
    private String customName;
    private long lastActive;

    public Plot(Chunk chunk, UUID owner) {
        this.id = UUID.randomUUID();
        this.chunk = chunk;
        this.owner = owner;
        this.customName = "Claim-" + id.toString().substring(0, 6);
        this.lastActive = System.currentTimeMillis(); // mark as active on creation
    }

    /* ======================================================
     * GETTERS
     * ====================================================== */

    public UUID getId() {
        return id;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String name) {
        this.customName = name;
        updateActivity();
    }

    public String getDisplayNameSafe() {
        return (customName != null && !customName.isEmpty()) ? customName : "Claim";
    }

    /* ======================================================
     * TRUSTED PLAYERS & ROLES
     * ====================================================== */

    public void trust(UUID playerId, ClaimRole role) {
        trusted.put(playerId, role);
        updateActivity();
    }

    public void untrust(UUID playerId) {
        trusted.remove(playerId);
        updateActivity();
    }

    public ClaimRole getRole(UUID playerId) {
        return trusted.getOrDefault(playerId, ClaimRole.NONE);
    }

    public Set<UUID> getTrusted() {
        return Collections.unmodifiableSet(trusted.keySet());
    }

    public List<String> getTrustedNames() {
        List<String> names = new ArrayList<>();
        for (UUID id : trusted.keySet()) {
            names.add(id.toString()); // TODO: resolve to OfflinePlayer name if needed
        }
        return names;
    }

    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
    }

    /* ======================================================
     * ACTIVITY TRACKING (for expiry)
     * ====================================================== */

    /** Update last activity timestamp (call whenever plot changes). */
    public void updateActivity() {
        this.lastActive = System.currentTimeMillis();
    }

    /** Get the last active timestamp. */
    public long getLastActive() {
        return lastActive;
    }
}
