package com.snazzyatoms.proshield.plots;

import org.bukkit.Location;

import java.util.*;

public class Plot {

    private final UUID id;
    private final UUID owner;
    private final String world;
    private final int x;
    private final int z;

    private int radius;
    private final Map<String, Boolean> flags = new HashMap<>();

    // NEW: roles-aware trusted map
    private final Map<UUID, String> trusted = new HashMap<>();

    public Plot(UUID owner, String world, int x, int z, UUID id, int radius) {
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.z = z;
        this.id = id;
        this.radius = radius;
    }

    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public String getWorld() { return world; }
    public int getX() { return x; }
    public int getZ() { return z; }

    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = Math.max(1, radius); }

    public Map<String, Boolean> getFlags() { return flags; }

    // Roles-aware map (preferred)
    public Map<UUID, String> getTrusted() { return trusted; }

    // Legacy shim: some old code might expect a Set<UUID>
    /** @deprecated Use getTrusted() which returns Map<UUID,String> */
    @Deprecated
    public Set<UUID> getTrustedPlayers() { return trusted.keySet(); }

    // Containment check used by PlotManager
    public boolean isInPlot(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equalsIgnoreCase(world)) return false;
        int dx = loc.getBlockX() - x;
        int dz = loc.getBlockZ() - z;
        return (dx*dx + dz*dz) <= (radius * radius);
    }

    // Legacy shim: some callers used contains(Location)
    /** @deprecated Use isInPlot(Location) */
    @Deprecated
    public boolean contains(Location loc) { return isInPlot(loc); }

    // Flag API (simple booleans)
    public boolean getFlag(String key) { return flags.getOrDefault(key, false); }
    public void setFlag(String key, boolean value) { flags.put(key, value); }

    // Legacy shim to satisfy callers passing a config second arg
    /** @deprecated Second argument ignored; use getFlag(String) */
    @Deprecated
    public boolean getFlag(String key, Object ignoredConfig) { return getFlag(key); }
}
