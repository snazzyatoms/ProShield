package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a claimed chunk ("plot").
 *
 * Preserves all prior logic, enhanced with:
 * - getX(), getZ(), getDisplayNameSafe()
 * - setOwner(UUID)
 * - trusted map with ClaimRole
 * - created timestamp
 * - serialization/deserialization
 */
public class Plot {

    private final String world;
    private final int x;
    private final int z;

    private UUID owner;
    private final Map<UUID, ClaimRole> trusted = new HashMap<>();
    private final PlotSettings settings;

    private long created; // timestamp of claim creation

    private boolean dirty;

    public Plot(String world, int x, int z, UUID owner) {
        this.world = world;
        this.x = x;
        this.z = z;
        this.owner = owner;
        this.settings = new PlotSettings();
        this.created = System.currentTimeMillis();
    }

    /* -------------------------------------------------------
     * Accessors
     * ------------------------------------------------------- */

    public String getWorld() {
        return world;
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
        this.dirty = true;
    }

    public Map<UUID, ClaimRole> getTrusted() {
        return trusted;
    }

    public PlotSettings getSettings() {
        return settings;
    }

    public long getCreated() {
        return created;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /* -------------------------------------------------------
     * Helpers
     * ------------------------------------------------------- */

    public boolean isOwner(UUID playerId) {
        return playerId != null && playerId.equals(owner);
    }

    public boolean isTrusted(UUID playerId) {
        return trusted.containsKey(playerId);
    }

    public void addTrusted(UUID playerId, ClaimRole role) {
        if (playerId == null || role == null) return;
        if (role == ClaimRole.OWNER) return; // cannot assign owner role
        trusted.put(playerId, role);
        this.dirty = true;
    }

    public void removeTrusted(UUID playerId) {
        if (playerId == null) return;
        trusted.remove(playerId);
        this.dirty = true;
    }

    public String getDisplayNameSafe() {
        if (owner == null) {
            return "[Unowned] " + world + ":" + x + "," + z;
        }
        String name = Bukkit.getOfflinePlayer(owner).getName();
        return (name != null ? name : owner.toString()) + "'s Claim";
    }

    /* -------------------------------------------------------
     * Bukkit World/Chunk
     * ------------------------------------------------------- */

    public World getBukkitWorld() {
        return Bukkit.getWorld(world);
    }

    public Chunk getChunk() {
        World w = getBukkitWorld();
        return (w != null) ? w.getChunkAt(x, z) : null;
    }

    /* -------------------------------------------------------
     * Serialization
     * ------------------------------------------------------- */

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("owner", owner != null ? owner.toString() : null);
        data.put("x", x);
        data.put("z", z);
        data.put("world", world);
        data.put("created", created);

        // Trusted roles
        Map<String, String> trustData = new HashMap<>();
        for (Map.Entry<UUID, ClaimRole> e : trusted.entrySet()) {
            trustData.put(e.getKey().toString(), e.getValue().name());
        }
        data.put("trusted", trustData);

        // Settings
        data.put("settings", settings.serialize());

        return data;
    }

    @SuppressWarnings("unchecked")
    public static Plot deserialize(ConfigurationSection sec) {
        if (sec == null) return null;

        String world = sec.getString("world");
        int x = sec.getInt("x");
        int z = sec.getInt("z");
        String ownerStr = sec.getString("owner");
        UUID owner = ownerStr != null ? UUID.fromString(ownerStr) : null;

        Plot plot = new Plot(world, x, z, owner);
        plot.created = sec.getLong("created", System.currentTimeMillis());

        // Trusted
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

        // Settings
        ConfigurationSection settingsSec = sec.getConfigurationSection("settings");
        if (settingsSec != null) {
            plot.settings.deserialize(settingsSec);
        }

        return plot;
    }
}
