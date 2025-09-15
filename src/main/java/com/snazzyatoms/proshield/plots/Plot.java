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
    private boolean adminClaim; // ✅ Safe zone / admin area flag

    private final Map<UUID, String> trusted = new HashMap<>();  // UUID -> role name
    private final Map<String, Boolean> flags = new HashMap<>(); // flag key -> state

    public Plot(UUID id, String world, int x, int z, UUID owner) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.z = z;
        this.owner = owner;
        this.adminClaim = false;

        // ===== Safe defaults (can be changed in GUI) =====
        // Block/Container
        flags.put("block-break", false);
        flags.put("block-place", false);
        flags.put("container-access", true);

        // Explosions & fire
        flags.put("explosions", false);
        flags.put("fire-burn", false);
        flags.put("fire-spread", false);
        flags.put("ignite-flint", false);
        flags.put("ignite-lava", false);
        flags.put("ignite-lightning", false);

        // Liquids / grief
        flags.put("lava-flow", false);
        flags.put("water-flow", false);
        flags.put("bucket-empty", false);

        // Mobs
        flags.put("mob-spawn", false);       // hostile spawn blocked
        flags.put("mob-damage", false);      // mobs cannot damage players in claim
        flags.put("hostile-aggro", false);   // hostiles cannot target players in claim
        flags.put("mob-repel", true);        // push hostiles away from players in claim
        flags.put("mob-despawn", true);      // despawn hostiles that slip into a claim

        // Pets & animals
        flags.put("pet-protect", true);      // tamed pets protected from other players
        flags.put("animal-protect", true);   // farm/passive animals protected from other players

        // PvP
        flags.put("pvp", true);
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

    public boolean isAdminClaim() { return adminClaim; }
    public void setAdminClaim(boolean adminClaim) { this.adminClaim = adminClaim; }

    public Map<UUID, String> getTrusted() { return Collections.unmodifiableMap(trusted); }
    public Map<String, Boolean> getFlags() { return Collections.unmodifiableMap(flags); }

    public boolean isTrusted(UUID uuid) {
        if (uuid == null) return false;
        if (adminClaim) return false; // admin/safe zones: only perms via admin GUI
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
                ", adminClaim=" + adminClaim +
                '}';
    }
}
