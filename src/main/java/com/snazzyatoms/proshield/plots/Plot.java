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
 * Preserves all prior logic:
 * - Owner UUID
 * - Trusted players with ClaimRole
 * - Settings object
 * - Display name safe helper
 * - Added created timestamp
 * - Added serialize/deserialize for persistence
 * - Added isEmpty() and getNearestBorder(Location) for listeners
 */
public class Plot {

    private final String worldName;
    private final int x;
    private final int z;

    private UUID owner;
    private final Map<UUID, ClaimRole> trusted = new HashMap<>();
    private final PlotSettings settings;

    private String name;
    private long created; // timestamp of creation

    public Plot(Chunk chunk, UUID owner) {
        this.worldName = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.owner = owner;
        this.settings = new PlotSettings();
        this.name = "Claim@" + x + "," + z;
        this.created = System.currentTimeMillis();
    }

    public String getWorldName() {
        return worldName;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID newOwner) {
        this.owner = newOwner;
    }

    public Map<UUID, ClaimRole> getTrusted() {
        return trusted;
    }

    public PlotSettings getSettings() {
        return settings;
    }

    public String getName() {
        return name != null ? name : "Claim@" + x + "," + z;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayNameSafe() {
        return (name != null && !name.isEmpty()) ? name : "Claim@" + x + "," + z;
    }

    public boolean isOwner(UUID playerId) {
        return owner != null && owner.equals(playerId);
    }

    public void addTrusted(UUID playerId, ClaimRole role) {
        if (playerId != null && role != null) {
            trusted.put(playerId, role);
        }
    }

    public void removeTrusted(UUID playerId) {
        if (playerId != null) {
            trusted.remove(playerId);
        }
    }

    public long getCreated() {
        return created;
    }

    /* -------------------------------------------------------
     * Helpers for listeners
     * ------------------------------------------------------- */

    /**
     * Whether this claim is effectively "empty".
     * Used by listeners that expect Optional-like behavior.
     */
    public boolean isEmpty() {
        return owner == null && trusted.isEmpty();
    }

    /**
     * Stub for border distance checks (used in MobBorderRepelListener).
     * Returns the chunk edge location closest to the given point.
     */
    public Location getNearestBorder(Location loc) {
        if (loc == null || !loc.getWorld().getName().equalsIgnoreCase(worldName)) {
            return null;
        }
        World w = getWorld();
        if (w == null) return null;

        int blockX = loc.getBlockX();
        int blockZ = loc.getBlockZ();

        int minX = x << 4;
        int minZ = z << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        // clamp to nearest edge
        int nearestX = Math.min(Math.max(blockX, minX), maxX);
        int nearestZ = Math.min(Math.max(blockZ, minZ), maxZ);

        return new Location(w, nearestX, loc.getBlockY(), nearestZ);
    }

    /* -------------------------------------------------------
     * Serialization
     * ------------------------------------------------------- */
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("owner", owner != null ? owner.toString() : null);
        map.put("trusted", serializeTrusted());
        map.put("settings", settings.serialize());
        map.put("name", name);
        map.put("created", created);
        return map;
    }

    private Map<String, String> serializeTrusted() {
        Map<String, String> out = new HashMap<>();
        for (Map.Entry<UUID, ClaimRole> e : trusted.entrySet()) {
            out.put(e.getKey().toString(), e.getValue().name());
        }
        return out;
    }

    public static Plot deserialize(ConfigurationSection sec) {
        if (sec == null) return null;

        String[] coords = sec.getName().split(",");
        if (coords.length != 2) return null;

        String world = sec.getParent().getName();
        int x = Integer.parseInt(coords[0]);
        int z = Integer.parseInt(coords[1]);

        UUID owner = null;
        if (sec.isString("owner")) {
            try {
                owner = UUID.fromString(sec.getString("owner"));
            } catch (IllegalArgumentException ignored) {}
        }

        Plot plot = new Plot(Bukkit.getWorld(world).getChunkAt(x, z), owner);

        plot.name = sec.getString("name", "Claim@" + x + "," + z);
        plot.created = sec.getLong("created", System.currentTimeMillis());

        // trusted
        ConfigurationSection trustedSec = sec.getConfigurationSection("trusted");
        if (trustedSec != null) {
            for (String key : trustedSec.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    ClaimRole role = ClaimRole.fromString(trustedSec.getString(key));
                    plot.trusted.put(id, role);
                } catch (Exception ignored) {}
            }
        }

        // settings
        if (sec.isConfigurationSection("settings")) {
            plot.settings.deserialize(sec.getConfigurationSection("settings"));
        }

        return plot;
    }
}
