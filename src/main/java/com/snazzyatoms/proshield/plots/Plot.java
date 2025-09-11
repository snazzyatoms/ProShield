package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a claimed chunk of land.
 *
 * ✅ Uses external PlotSettings class instead of duplicate inner class.
 * ✅ Fixed serialization to save/load full settings map.
 * ✅ Added getSettings() to expose the standalone PlotSettings object.
 */
public class Plot {

    private final String worldName;
    private final int x;
    private final int z;

    private UUID owner;
    private final Map<UUID, ClaimRole> trusted = new HashMap<>();
    private String displayName;

    // Per-plot settings (standalone class)
    private final PlotSettings settings = new PlotSettings();

    public Plot(Chunk chunk, UUID owner) {
        this.worldName = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.owner = owner;
    }

    /* -------------------------------------------------------
     * Getters
     * ------------------------------------------------------- */
    public String getWorldName() { return worldName; }
    public int getX() { return x; }
    public int getZ() { return z; }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public Map<UUID, ClaimRole> getTrusted() { return trusted; }

    public String getDisplayNameSafe() {
        return (displayName != null) ? displayName : "Claim (" + x + "," + z + ")";
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** Returns a user-facing name for debug/logging. */
    public String getName() {
        return getDisplayNameSafe();
    }

    /** Returns the per-plot settings object. */
    public PlotSettings getSettings() {
        return settings;
    }

    /* -------------------------------------------------------
     * Ownership / Trust
     * ------------------------------------------------------- */
    public boolean isOwner(UUID playerId) {
        return owner != null && owner.equals(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return trusted.containsKey(playerId);
    }

    /* -------------------------------------------------------
     * Serialization
     * ------------------------------------------------------- */
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("world", worldName);
        data.put("x", x);
        data.put("z", z);
        if (owner != null) data.put("owner", owner.toString());
        if (displayName != null) data.put("display", displayName);

        // Save trusted players with role names
        List<String> trustedList = new ArrayList<>();
        for (UUID id : trusted.keySet()) {
            trustedList.add(id.toString() + ":" + trusted.get(id).name());
        }
        data.put("trusted", trustedList);

        // Save full settings
        data.put("settings", settings.serialize());

        return data;
    }

    public static Plot deserialize(ConfigurationSection section) {
        if (section == null) return null;
        String world = section.getString("world");
        int x = section.getInt("x");
        int z = section.getInt("z");
        String ownerStr = section.getString("owner");

        World w = Bukkit.getWorld(world);
        if (w == null) return null;

        Chunk chunk = w.getChunkAt(x, z);
        UUID owner = (ownerStr != null) ? UUID.fromString(ownerStr) : null;
        Plot plot = new Plot(chunk, owner);

        plot.displayName = section.getString("display");

        // Load trusted players with roles
        List<String> trustedList = section.getStringList("trusted");
        for (String entry : trustedList) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    plot.trusted.put(UUID.fromString(parts[0]), ClaimRole.fromString(parts[1]));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // Load settings
        ConfigurationSection settingsSec = section.getConfigurationSection("settings");
        if (settingsSec != null) {
            plot.getSettings().deserialize(settingsSec);
        }

        return plot;
    }

    /* -------------------------------------------------------
     * Helpers
     * ------------------------------------------------------- */
    public Location getCenter() {
        World w = Bukkit.getWorld(worldName);
        if (w == null) return null;
        return new Location(w, (x << 4) + 8, w.getHighestBlockYAt(x << 4, z << 4), (z << 4) + 8);
    }

    /** Returns the nearest border block location (simple placeholder). */
    public Location getNearestBorder(Location loc) {
        World w = Bukkit.getWorld(worldName);
        if (w == null) return loc;

        int bx = (x << 4);
        int bz = (z << 4);

        // Clamp to nearest edge of this chunk
        int nx = Math.max(bx, Math.min(bx + 15, loc.getBlockX()));
        int nz = Math.max(bz, Math.min(bz + 15, loc.getBlockZ()));

        return new Location(w, nx, loc.getY(), nz);
    }

    /* -------------------------------------------------------
     * Dummy Chunk wrapper (fallback if needed)
     * ------------------------------------------------------- */
    public static class DummyChunk {
        private final String world;
        private final int x, z;

        public DummyChunk(String world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        public String getWorld() { return world; }
        public int getX() { return x; }
        public int getZ() { return z; }
    }
}
