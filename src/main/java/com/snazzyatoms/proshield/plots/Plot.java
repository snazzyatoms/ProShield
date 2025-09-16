package com.snazzyatoms.proshield.plots;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a claimed plot in ProShield.
 * Stores owner, world, coordinates, flags, trusted players, and role assignments.
 */
public class Plot {

    private final UUID id;           // Unique ID of this plot
    private final String world;      // World name
    private final int x;             // Chunk X
    private final int z;             // Chunk Z
    private final UUID owner;        // Owner UUID
    private final int radius;        // Claim radius (blocks)

    private boolean adminClaim;      // Is this claim owned/admin-managed

    // Per-claim flags (PvP, explosions, safezone, etc.)
    private final Map<String, Boolean> flags = new HashMap<>();

    // Trusted players + roles
    private final Set<UUID> trusted = new HashSet<>();
    private final Map<UUID, String> roles = new HashMap<>();

    public Plot(UUID id, String world, int x, int z, UUID owner, int radius) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.z = z;
        this.owner = owner;
        this.radius = radius;
        this.adminClaim = false;
    }

    // -------------------
    // Core Getters
    // -------------------
    public UUID getId() { return id; }
    public String getWorld() { return world; }
    public int getX() { return x; }
    public int getZ() { return z; }
    public UUID getOwner() { return owner; }
    public int getRadius() { return radius; }

    // -------------------
    // Flags
    // -------------------
    public boolean getFlag(String key) {
        return flags.getOrDefault(key, false);
    }

    public void setFlag(String key, boolean value) {
        flags.put(key, value);
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    // -------------------
    // Admin Claim
    // -------------------
    public boolean isAdminClaim() {
        return adminClaim;
    }

    public void setAdminClaim(boolean adminClaim) {
        this.adminClaim = adminClaim;
    }

    // -------------------
    // Trusted Players
    // -------------------
    public void addTrusted(UUID uuid) {
        trusted.add(uuid);
    }

    public void removeTrusted(UUID uuid) {
        trusted.remove(uuid);
        roles.remove(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return trusted.contains(uuid);
    }

    public Set<UUID> getTrusted() {
        return trusted;
    }

    // -------------------
    // Roles
    // -------------------
    public void setRole(UUID uuid, String role) {
        trusted.add(uuid);
        roles.put(uuid, role);
    }

    public String getRole(UUID uuid) {
        return roles.getOrDefault(uuid, "member");
    }

    public Map<UUID, String> getRoles() {
        return roles;
    }
}
