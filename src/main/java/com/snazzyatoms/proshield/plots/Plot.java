// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;

import java.util.*;

/**
 * Plot
 * - Represents a single protected claim
 * - Stores owner, radius, flags, and trusted players (role-aware)
 * - Contains containment checks and flag API
 */
public class Plot {

    private final UUID id;
    private final UUID owner;
    private final String world;
    private final int x;
    private final int z;

    private int radius;

    // Claim flags (simple key → boolean)
    private final Map<String, Boolean> flags = new HashMap<>();

    // Trusted players with roles
    private final Map<UUID, String> trusted = new HashMap<>();

    public Plot(UUID owner, String world, int x, int z, UUID id, int radius) {
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.z = z;
        this.id = id;
        this.radius = Math.max(1, radius);

        // ✅ Apply defaults from config when plot is created
        ProShield plugin = ProShield.getInstance();
        if (plugin != null && plugin.getConfig().isConfigurationSection("flags.available")) {
            for (String key : plugin.getConfig().getConfigurationSection("flags.available").getKeys(false)) {
                boolean def = plugin.getConfig().getBoolean("flags.available." + key + ".default", false);
                flags.putIfAbsent(key, def);
            }
        }
    }

    /* -------------------------
     * BASIC GETTERS
     * ------------------------- */
    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public String getWorld() { return world; }
    public int getX() { return x; }
    public int getZ() { return z; }

    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = Math.max(1, radius); }

    public Map<String, Boolean> getFlags() { return flags; }
    public Map<UUID, String> getTrusted() { return trusted; }

    /* -------------------------
     * LEGACY SHIMS (to be removed in 2.0)
     * ------------------------- */
    /** @deprecated Use getTrusted() for roles */
    @Deprecated
    public Set<UUID> getTrustedPlayers() { return trusted.keySet(); }

    /** @deprecated Use isInPlot(Location) */
    @Deprecated
    public boolean contains(Location loc) { return isInPlot(loc); }

    /** @deprecated Second argument ignored; use getFlag(String) */
    @Deprecated
    public boolean getFlag(String key, Object ignoredConfig) { return getFlag(key); }

    /* -------------------------
     * FLAG API
     * ------------------------- */
    public boolean getFlag(String key) {
        // fallback to config default if not set
        if (!flags.containsKey(key)) {
            ProShield plugin = ProShield.getInstance();
            if (plugin != null) {
                return plugin.getConfig().getBoolean("flags.available." + key + ".default", false);
            }
            return false;
        }
        return flags.getOrDefault(key, false);
    }

    public void setFlag(String key, boolean value) {
        flags.put(key, value);
    }

    /* -------------------------
     * GEOMETRY
     * ------------------------- */
    public boolean isInPlot(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equalsIgnoreCase(world)) return false;

        int dx = loc.getBlockX() - x;
        int dz = loc.getBlockZ() - z;
        return (dx * dx + dz * dz) <= (radius * radius);
    }

    /* -------------------------
     * DEBUG / LOGGING
     * ------------------------- */
    @Override
    public String toString() {
        return "Plot{id=" + id +
                ", owner=" + owner +
                ", world='" + world + '\'' +
                ", center=(" + x + "," + z + ")" +
                ", radius=" + radius +
                ", trusted=" + trusted.size() +
                ", flags=" + flags + "}";
    }
}
