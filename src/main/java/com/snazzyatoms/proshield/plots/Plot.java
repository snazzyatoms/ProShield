package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.Location;

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
    private final Map<UUID, String> trusted = new HashMap<>();              // UUID -> role name
    private final Map<String, Boolean> flags = new HashMap<>();             // flag key -> state

    public Plot(UUID id, String world, int x, int z, UUID owner) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.z = z;
        this.owner = owner;

        // Safe defaults (you can tune in config/GUI):
        // Keep protected by default (no grief, no fire, no explosions, no mob damage in claims)
        flags.put("block-break", false);
        flags.put("block-place", false);
        flags.put("container-access", true);
        flags.put("explosions", false);
        flags.put("fire-burn", false);
        flags.put("fire-spread", false);
        flags.put("ignite-flint", false);
        flags.put("ignite-lava", false);
        flags.put("ignite-lightning", false);
        flags.put("mob-spawn", false);      // mob spawning inside claims disabled
        flags.put("mob-damage", false);     // mobs cannot damage players/entities in claims
        flags.put("pvp", true);             // example toggle (left true by default)
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

    public Map<UUID, String> getTrusted() { return Collections.unmodifiableMap(trusted); }
    public Map<String, Boolean> getFlags() { return Collections.unmodifiableMap(flags); }

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

    /** Get a flag with default fallback if not set */
    public boolean getFlag(String key, boolean def) {
        return flags.getOrDefault(key, def);
    }

    public void setFlag(String key, boolean value) {
        flags.put(key, value);
    }

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
