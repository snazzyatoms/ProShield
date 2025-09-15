package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Plot (claim) stored by chunk coordinates and world name.
 * Stores owner, trusted roles, and per-plot flags.
 */
public class Plot {

    private final UUID id;
    private final String world;
    private final int x; // chunk X
    private final int z; // chunk Z

    private UUID owner;

    // Trusted players: UUID -> role key (matches config roles.available.*)
    private final Map<UUID, String> trusted = new HashMap<>();

    // Per-plot flags (overrides). If absent, fall back to config defaults.
    private final Map<String, Boolean> flags = new HashMap<>();

    public Plot(UUID id, String world, int x, int z, UUID owner) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.z = z;
        this.owner = owner;
    }

    public static Plot of(Chunk chunk, UUID owner) {
        return new Plot(UUID.randomUUID(), chunk.getWorld().getName(), chunk.getX(), chunk.getZ(), owner);
    }

    public UUID getId() { return id; }
    public String getWorld() { return world; }
    public int getX() { return x; }
    public int getZ() { return z; }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    // ---------- Trusted / Roles ----------
    public Map<UUID, String> getTrusted() { return Collections.unmodifiableMap(trusted); }

    public boolean isTrusted(UUID uuid) {
        if (uuid == null) return false;
        if (uuid.equals(owner)) return true;
        return trusted.containsKey(uuid);
    }

    public String getRole(UUID uuid) {
        if (uuid == null) return null;
        if (uuid.equals(owner)) return "owner";
        return trusted.get(uuid);
    }

    public void setRole(UUID uuid, String roleKey) {
        if (uuid == null || roleKey == null) return;
        trusted.put(uuid, roleKey.toLowerCase(Locale.ROOT));
    }

    public void untrust(UUID uuid) {
        if (uuid == null) return;
        trusted.remove(uuid);
    }

    public boolean hasPermission(UUID uuid, String permissionKey, FileConfiguration cfg) {
        if (uuid == null) return false;
        if (uuid.equals(owner)) return true; // owner = full access
        String role = trusted.get(uuid);
        if (role == null) return false;
        ConfigurationSection perm = cfg.getConfigurationSection("roles.available." + role + ".permissions");
        if (perm == null) return false;
        return perm.getBoolean(permissionKey, false);
    }

    // ---------- Flags ----------
    /** Get effective flag value: per-plot override or default from config flags.available */
    public boolean getFlag(String key, FileConfiguration cfg) {
        if (flags.containsKey(key)) return flags.get(key);
        return cfg.getBoolean("flags.available." + key + ".default", false);
    }

    public void setFlag(String key, boolean value) {
        flags.put(key, value);
    }

    public Map<String, Boolean> getRawFlags() {
        return Collections.unmodifiableMap(flags);
    }

    // ---------- Match helper ----------
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
                '}';
    }
}
