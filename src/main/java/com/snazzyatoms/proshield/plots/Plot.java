package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Plot (claim) stored by chunk coordinates and world name.
 * Stores owner, trusted roles, per-plot flags, and radius (for expansion).
 * All protections default to safe/secure state as defined in config.yml.
 */
public class Plot {

    private final UUID id;
    private final String world;
    private final int x; // chunk X
    private final int z; // chunk Z

    private UUID owner;
    private int radius; // 🔹 protection radius in blocks (per-plot)

    private final Map<UUID, String> trusted = new HashMap<>();     // UUID -> role name
    private final Map<String, Boolean> flags = new HashMap<>();    // flag key -> state

    public Plot(UUID id, String world, int x, int z, UUID owner, int radius) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.z = z;
        this.owner = owner;
        this.radius = radius;

        // =============================
        // Default protection flags
        // =============================
        // Building
        flags.put("block-break", false);
        flags.put("block-place", false);
        flags.put("bucket-use", false);
        flags.put("bucket-lava", false);
        flags.put("bucket-water", false);

        // Explosions
        flags.put("explosions", false);
        flags.put("explosions-creeper", false);
        flags.put("explosions-tnt", false);
        flags.put("explosions-ghast", false);
        flags.put("explosions-other", false);

        // Fire
        flags.put("fire-burn", false);
        flags.put("fire-spread", false);
        flags.put("ignite-flint", false);
        flags.put("ignite-lava", false);
        flags.put("ignite-lightning", false);

        // Mobs
        flags.put("mob-spawn", false);
        flags.put("mob-damage", false);
        flags.put("safezone", true);          // claims = safezones by default
        flags.put("mob-repel", true);         // repel enabled
        flags.put("mob-despawn", true);       // despawn inside claims
        flags.put("protect-pets", true);      // protect tamed pets
        flags.put("protect-passive", true);   // protect passive mobs

        // PvP
        flags.put("pvp", false);              // PVP disabled inside claims by default
    }

    /** Factory for creating a new Plot at a chunk with radius from config. */
    public static Plot of(Chunk chunk, UUID owner, int radius) {
        return new Plot(UUID.randomUUID(),
                chunk.getWorld().getName(),
                chunk.getX(),
                chunk.getZ(),
                owner,
                radius);
    }

    /* =============================
     * Accessors
     * ============================= */
    public UUID getId() { return id; }
    public String getWorld() { return world; }
    public int getX() { return x; }
    public int getZ() { return z; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }

    public Map<UUID, String> getTrusted() { return Collections.unmodifiableMap(trusted); }
    public Map<String, Boolean> getFlags() { return Collections.unmodifiableMap(flags); }

    /* =============================
     * Trust Management
     * ============================= */
    public boolean isTrusted(UUID uuid) {
        if (uuid == null) return false;
        if (uuid.equals(owner)) return true;
        return trusted.containsKey(uuid);
    }

    public void trust(UUID uuid, String role) {
        if (uuid == null) return;
        trusted.put(uuid, role == null ? "trusted" : role);
    }

    public void untrust(UUID uuid) {
        if (uuid == null) return;
        trusted.remove(uuid);
    }

    /* =============================
     * Flag Management
     * ============================= */
    /** Get a flag with config default fallback */
    public boolean getFlag(String key, FileConfiguration cfg) {
        if (flags.containsKey(key)) return flags.get(key);

        // fallback to config defaults if defined
        if (cfg.isConfigurationSection("protection")) {
            for (String section : cfg.getConfigurationSection("protection").getKeys(true)) {
                if (section.equalsIgnoreCase(key)) {
                    return cfg.getBoolean("protection." + section);
                }
            }
        }
        return false;
    }

    public void setFlag(String key, boolean value) {
        flags.put(key, value);
    }

    /* =============================
     * Location Helpers
     * ============================= */
    public boolean matches(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equalsIgnoreCase(world)) return false;
        return loc.getChunk().getX() == x && loc.getChunk().getZ() == z;
    }

    @Override
    public String toString() {
        return "Plot{" +
                "id=" + id +
                ", world='" + world + '\'' +
                ", x=" + x +
                ", z=" + z +
                ", owner=" + owner +
                ", radius=" + radius +
                ", trusted=" + trusted.size() +
                ", flags=" + flags +
                '}';
    }
}
