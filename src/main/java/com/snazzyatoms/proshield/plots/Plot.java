// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;

import org.bukkit.Chunk;

import java.time.Instant;
import java.util.*;

/**
 * Plot
 * Represents a single claimed chunk.
 * Stores owner, trusted players, roles, flags, and metadata.
 */
public class Plot {

    private final UUID id;
    private final Chunk chunk;
    private final UUID owner;

    // Trusted players and roles
    private final Map<UUID, ClaimRole> trusted = new HashMap<>();

    // Claim display name
    private String displayName;

    // Flags (pvp, explosions, fire, etc.)
    private final Map<String, Boolean> flags = new HashMap<>();

    // Metadata
    private Instant lastActive;

    public Plot(Chunk chunk, UUID owner) {
        this.id = UUID.randomUUID();
        this.chunk = chunk;
        this.owner = owner;
        this.displayName = "Claim-" + id.toString().substring(0, 8);
        this.lastActive = Instant.now();
    }

    /* ----------------------------
     * Getters / Setters
     * ---------------------------- */
    public UUID getId() { return id; }

    public Chunk getChunk() { return chunk; }

    public UUID getOwner() { return owner; }

    public boolean isOwner(UUID uuid) { return owner.equals(uuid); }

    public String getDisplayNameSafe() {
        return (displayName != null && !displayName.isEmpty()) ? displayName : "Unnamed Claim";
    }

    public void setDisplayName(String name) {
        this.displayName = (name != null && !name.isEmpty()) ? name : "Unnamed Claim";
    }

    public Instant getLastActive() { return lastActive; }

    public void updateActivity() { this.lastActive = Instant.now(); }

    /* ----------------------------
     * Trusted Players
     * ---------------------------- */
    public ClaimRole getRole(UUID uuid) {
        return trusted.getOrDefault(uuid, ClaimRole.VISITOR);
    }

    public void setRole(UUID uuid, ClaimRole role) {
        trusted.put(uuid, role);
    }

    public void removeRole(UUID uuid) {
        trusted.remove(uuid);
    }

    public Set<String> getTrustedNames() {
        // Names can be resolved asynchronously if needed
        Set<String> names = new HashSet<>();
        for (UUID uuid : trusted.keySet()) {
            names.add(uuid.toString()); // TODO: hook into Bukkit.getOfflinePlayer
        }
        return names;
    }

    /* ----------------------------
     * Flags
     * ---------------------------- */
    public void setFlag(String flag, boolean value) {
        flags.put(flag.toLowerCase(), value);
    }

    public boolean getFlag(String flag) {
        return flags.getOrDefault(flag.toLowerCase(), false);
    }

    public Map<String, Boolean> getFlags() {
        return Collections.unmodifiableMap(flags);
    }
}
