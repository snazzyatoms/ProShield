// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Chunk;

import java.time.Instant;
import java.util.*;

public class Plot {

    private final UUID id;
    private final UUID owner;
    private final Chunk chunk;

    private final Map<UUID, ClaimRole> trusted = new HashMap<>();

    // --- Expiry Tracking ---
    private Instant lastActive; // last time claim was interacted with

    public Plot(Chunk chunk, UUID owner) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.chunk = chunk;
        this.lastActive = Instant.now(); // start as active
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public String getDisplayNameSafe() {
        return "Claim@" + chunk.getX() + "," + chunk.getZ();
    }

    public void trust(UUID player, ClaimRole role) {
        trusted.put(player, role);
        touch();
    }

    public void untrust(UUID player) {
        trusted.remove(player);
        touch();
    }

    public ClaimRole getRole(UUID player) {
        return trusted.getOrDefault(player, ClaimRole.VISITOR);
    }

    public Set<String> getTrustedNames() {
        Set<String> names = new HashSet<>();
        for (UUID uuid : trusted.keySet()) {
            names.add(uuid.toString()); // replace with Bukkit API lookup if needed
        }
        return names;
    }

    /* ======================================================
     * Expiry Logic
     * ====================================================== */

    /** Mark the plot as active now */
    public void touch() {
        lastActive = Instant.now();
    }

    /** True if claim has expired based on config value */
    public boolean isExpired() {
        long maxDays = 30; // fallback
        try {
            maxDays = com.snazzyatoms.proshield.ProShield.getInstance()
                    .getConfig().getLong("claims.expiry-days", 30);
        } catch (Exception ignored) {}

        Instant expireAt = lastActive.plusSeconds(maxDays * 86400);
        return Instant.now().isAfter(expireAt);
    }
}
