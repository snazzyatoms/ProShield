package com.snazzyatoms.proshield.plots;

import java.util.*;

/**
 * Represents a land claim (plot) in ProShield.
 */
public class Plot {

    private final UUID id;
    private final UUID owner;
    private final String world;
    private final int x;
    private final int z;
    private int radius;

    // Trusted players and their roles
    private final Map<UUID, String> trusted = new HashMap<>();

    // Per-claim flags (string key → boolean/string value)
    private final Map<String, String> flags = new HashMap<>();

    public Plot(UUID owner, String world, int x, int z, UUID id, int radius) {
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.z = z;
        this.id = id;
        this.radius = radius;
    }

    /* ======================
     * Core getters
     * ====================== */
    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    /* ======================
     * Trusted players
     * ====================== */
    public Map<UUID, String> getTrusted() {
        return trusted;
    }

    public void trust(UUID uuid, String role) {
        trusted.put(uuid, role);
    }

    public void untrust(UUID uuid) {
        trusted.remove(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return trusted.containsKey(uuid);
    }

    /* ======================
     * Flags
     * ====================== */
    public Map<String, String> getFlags() {
        return flags;
    }

    public void setFlag(String key, boolean value) {
        flags.put(key, String.valueOf(value));
    }

    public void setFlag(String key, String value) {
        flags.put(key, value);
    }

    public boolean getFlagAsBool(String key, boolean def) {
        String val = flags.get(key);
        if (val == null) return def;
        return Boolean.parseBoolean(val);
    }

    public String getFlagAsString(String key, String def) {
        return flags.getOrDefault(key, def);
    }
}
