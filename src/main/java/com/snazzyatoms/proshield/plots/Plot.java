package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
    private int radius; // protection radius in blocks (per-plot)

    // Keep mutable so persistence + GUI can modify them
    private final Map<UUID, String> trusted = new HashMap<>();
    private final Map<String, Boolean> flags = new HashMap<>();

    public Plot(UUID id, String world, int x, int z, UUID owner, int radius) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.z = z;
        this.owner = owner;
        this.radius = radius;

        setDefaultFlags();
    }

    public static Plot of(Chunk chunk, UUID owner, int radius) {
        return new Plot(UUID.randomUUID(),
                chunk.getWorld().getName(),
                chunk.getX(),
                chunk.getZ(),
                owner,
                radius);
    }

    private void setDefaultFlags() {
        // Core build/place
        flags.putIfAbsent("block-break", false);
        flags.putIfAbsent("block-place", false);
        flags.putIfAbsent("bucket-use", false);
        flags.putIfAbsent("bucket-lava", false);
        flags.putIfAbsent("bucket-water", false);

        // Explosions
        flags.putIfAbsent("explosions", false);
        flags.putIfAbsent("explosions-creeper", false);
        flags.putIfAbsent("explosions-tnt", false);
        flags.putIfAbsent("explosions-ghast", false);
        flags.putIfAbsent("explosions-other", false);

        // Fire
        flags.putIfAbsent("fire-burn", false);
        flags.putIfAbsent("fire-spread", false);
        flags.putIfAbsent("ignite-flint", false);
        flags.putIfAbsent("ignite-lava", false);
        flags.putIfAbsent("ignite-lightning", false);

        // Mobs
        flags.putIfAbsent("mob-spawn", false);
        flags.putIfAbsent("mob-damage", false);
        flags.putIfAbsent("safezone", true);
        flags.putIfAbsent("mob-repel", true);
        flags.putIfAbsent("mob-despawn", true);
        flags.putIfAbsent("protect-pets", true);
        flags.putIfAbsent("protect-passive", true);

        // PvP
        flags.putIfAbsent("pvp", false);

        // Optional admin-claim
        flags.putIfAbsent("admin-claim", false);
    }

    public UUID getId() { return id; }
    public String getWorld() { return world; }
    public int getX() { return x; }
    public int getZ() { return z; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }

    public Map<UUID, String> getTrusted() { return trusted; }
    public Map<String, Boolean> getFlags() { return flags; }

    public boolean isTrusted(UUID uuid) {
        if (uuid == null) return false;
        if (uuid.equals(owner)) return true; // owner always trusted
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

    /** Get a flag with config default fallback */
    public boolean getFlag(String key, FileConfiguration cfg) {
        if (flags.containsKey(key)) return flags.get(key);

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

    /** Treat admin claims as a flag or ownerless plots. */
    public boolean isAdminClaim() {
        Boolean flag = flags.get("admin-claim");
        return (flag != null && flag) || owner == null;
    }

    public boolean matches(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equalsIgnoreCase(world)) return false;
        return loc.getChunk().getX() == x && loc.getChunk().getZ() == z;
    }

    /** Utility: display trusted players as names instead of UUIDs */
    public List<String> getTrustedNames() {
        List<String> list = new ArrayList<>();
        for (UUID uuid : trusted.keySet()) {
            OfflinePlayer off = org.bukkit.Bukkit.getOfflinePlayer(uuid);
            if (off != null && off.getName() != null) {
                list.add(off.getName() + " (" + trusted.get(uuid) + ")");
            }
        }
        return list;
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
