// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Plot
 * - Represents a claimed chunk
 * - Holds owner, trusted players, settings
 * - Preserves prior logic
 */
public class Plot {

    private final String world;
    private final int x;
    private final int z;

    private UUID owner;
    private String name;

    private final Map<UUID, com.snazzyatoms.proshield.roles.ClaimRole> trusted = new HashMap<>();
    private final PlotSettings settings = new PlotSettings();

    public Plot(Chunk chunk, UUID owner) {
        this.world = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.owner = owner;
    }

    // -------------------------------------------------------
    // Core Getters
    // -------------------------------------------------------

    public String getWorldName() { return world; }
    public int getX() { return x; }
    public int getZ() { return z; }
    public UUID getOwner() { return owner; }
    public String getName() { return name; }
    public Map<UUID, com.snazzyatoms.proshield.roles.ClaimRole> getTrusted() { return trusted; }
    public PlotSettings getSettings() { return settings; }

    public void setOwner(UUID owner) { this.owner = owner; }
    public void setName(String name) { this.name = name; }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    public boolean isOwner(UUID playerId) {
        return owner != null && owner.equals(playerId);
    }

    /** ✅ Added: check if a player is trusted (any role). */
    public boolean isTrusted(UUID playerId) {
        return trusted.containsKey(playerId);
    }

    public String getDisplayNameSafe() {
        return (name != null && !name.isEmpty()) ? name : (owner != null ? owner.toString() : "Unowned");
    }

    public Location getCenter(World world) {
        int bx = x << 4;
        int bz = z << 4;
        return new Location(world, bx + 8, world.getHighestBlockYAt(bx + 8, bz + 8), bz + 8);
    }

    // -------------------------------------------------------
    // Serialization
    // -------------------------------------------------------

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("world", world);
        data.put("x", x);
        data.put("z", z);
        if (owner != null) data.put("owner", owner.toString());
        if (name != null) data.put("name", name);

        Map<String, String> trustData = new HashMap<>();
        for (Map.Entry<UUID, com.snazzyatoms.proshield.roles.ClaimRole> e : trusted.entrySet()) {
            trustData.put(e.getKey().toString(), e.getValue().name());
        }
        data.put("trusted", trustData);

        data.put("settings", settings.serialize());
        return data;
    }

    @SuppressWarnings("unchecked")
    public static Plot deserialize(ConfigurationSection section) {
        if (section == null) return null;

        String world = section.getString("world");
        int x = section.getInt("x");
        int z = section.getInt("z");
        UUID owner = section.contains("owner") ? UUID.fromString(section.getString("owner")) : null;

        Plot plot = new Plot(new DummyChunk(world, x, z), owner);
        plot.name = section.getString("name", null);

        // Trusted
        ConfigurationSection trustSec = section.getConfigurationSection("trusted");
        if (trustSec != null) {
            for (String key : trustSec.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    com.snazzyatoms.proshield.roles.ClaimRole role =
                            com.snazzyatoms.proshield.roles.ClaimRole.valueOf(trustSec.getString(key, "VISITOR"));
                    plot.trusted.put(id, role);
                } catch (Exception ignored) {}
            }
        }

        // Settings
        ConfigurationSection settingsSec = section.getConfigurationSection("settings");
        if (settingsSec != null) {
            plot.settings.deserialize(settingsSec);
        }

        return plot;
    }

    // -------------------------------------------------------
    // Dummy Chunk (used only for loading)
    // -------------------------------------------------------
    private static class DummyChunk extends Chunk {
        private final String world;
        private final int x, z;

        public DummyChunk(String world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override public World getWorld() { return null; }
        @Override public int getX() { return x; }
        @Override public int getZ() { return z; }
        @Override public boolean isLoaded() { return false; }
        @Override public boolean load(boolean generate) { return false; }
        @Override public boolean load() { return false; }
        @Override public boolean unload() { return false; }
        @Override public boolean unload(boolean save) { return false; }
        @Override public boolean equals(Object o) { return (o instanceof DummyChunk dc) && dc.x == x && dc.z == z && dc.world.equals(world); }
        @Override public int hashCode() { return Objects.hash(world, x, z); }
    }
}
