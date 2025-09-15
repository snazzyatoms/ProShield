package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;

/**
 * Immutable chunk-anchored Claim ("Plot").
 * Stores owner, trusted players, and per-claim flags.
 *
 * Key design points:
 * - A plot is bound to a single chunk (world + chunkX + chunkZ).
 * - ID is stable and derived from world:chunkX:chunkZ so it’s consistent across restarts.
 * - Flags are simple boolean toggles (e.g., "explosions", "mob-spawning", "fire-spread").
 */
public class Plot {

    // Identity
    private final UUID id;          // stable, name-based from world:chunkX:chunkZ
    private final String worldName; // chunk world name
    private final int chunkX;       // chunk coordinates
    private final int chunkZ;

    // Ownership & trust
    private UUID owner;
    private final Set<UUID> trusted = new HashSet<>();

    // Feature flags
    private final Map<String, Boolean> flags = new HashMap<>();

    // ---------------------------
    // Constructors
    // ---------------------------
    public Plot(UUID owner, Chunk chunk) {
        this(owner, chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public Plot(UUID owner, String worldName, int chunkX, int chunkZ) {
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.owner = owner;
        this.id = nameBasedId(worldName, chunkX, chunkZ);

        // sensible defaults (can be overridden)
        flags.put("explosions", false);
        flags.put("fire-spread", false);
        flags.put("mob-spawning", true);
    }

    private static UUID nameBasedId(String world, int cx, int cz) {
        String key = world + ":" + cx + ":" + cz;
        return UUID.nameUUIDFromBytes(key.getBytes());
    }

    // ---------------------------
    // Identity & Location helpers
    // ---------------------------
    public UUID getId() { return id; }
    public String getWorldName() { return worldName; }
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }

    /**
     * Block-space coordinates (block position of the chunk’s min corner).
     * Useful for visualizers like ClaimPreview that need an (x,z).
     */
    public int getX() { return chunkX << 4; } // chunkX * 16
    public int getZ() { return chunkZ << 4; } // chunkZ * 16

    /**
     * Returns true if the provided Bukkit Location belongs to this plot's chunk.
     */
    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        return loc.getWorld().getName().equals(worldName)
                && loc.getChunk().getX() == chunkX
                && loc.getChunk().getZ() == chunkZ;
    }

    // ---------------------------
    // Ownership
    // ---------------------------
    public UUID getOwner() { return owner; }
    public void setOwner(UUID newOwner) {
        if (newOwner != null) this.owner = newOwner;
    }

    // ---------------------------
    // Trust management
    // ---------------------------
    public boolean isTrusted(UUID uuid) {
        if (uuid == null) return false;
        if (uuid.equals(owner)) return true; // owner always trusted
        return trusted.contains(uuid);
    }

    public boolean addTrusted(UUID uuid) {
        if (uuid == null || uuid.equals(owner)) return false;
        return trusted.add(uuid);
    }

    public boolean removeTrusted(UUID uuid) {
        if (uuid == null || uuid.equals(owner)) return false;
        return trusted.remove(uuid);
    }

    public Set<UUID> getTrusted() {
        return Collections.unmodifiableSet(trusted);
    }

    // ---------------------------
    // Flags
    // ---------------------------
    public boolean getFlag(String key, boolean def) {
        Boolean v = flags.get(key);
        return v != null ? v : def;
    }

    public void setFlag(String key, boolean value) {
        if (key == null || key.isEmpty()) return;
        flags.put(key, value);
    }

    public Map<String, Boolean> getFlags() {
        return Collections.unmodifiableMap(flags);
    }

    // ---------------------------
    // Serialization helpers (optional for persistence)
    // ---------------------------
    public Map<String, Object> serialize() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id.toString());
        m.put("world", worldName);
        m.put("chunkX", chunkX);
        m.put("chunkZ", chunkZ);
        m.put("owner", owner.toString());

        List<String> trustedList = new ArrayList<>(trusted.size());
        for (UUID u : trusted) trustedList.add(u.toString());
        m.put("trusted", trustedList);

        m.put("flags", new LinkedHashMap<>(flags));
        return m;
    }

    @SuppressWarnings("unchecked")
    public static Plot deserialize(Map<String, Object> map) {
        String world = (String) map.get("world");
        int cx = (int) map.get("chunkX");
        int cz = (int) map.get("chunkZ");
        UUID owner = UUID.fromString((String) map.get("owner"));

        Plot p = new Plot(owner, world, cx, cz);

        // restore trusted
        Object t = map.get("trusted");
        if (t instanceof Collection<?> list) {
            for (Object s : list) {
                try { p.trusted.add(UUID.fromString(String.valueOf(s))); }
                catch (IllegalArgumentException ignored) {}
            }
        }

        // restore flags
        Object f = map.get("flags");
        if (f instanceof Map<?, ?> fm) {
            for (Map.Entry<?, ?> e : fm.entrySet()) {
                String k = String.valueOf(e.getKey());
                boolean v = Boolean.parseBoolean(String.valueOf(e.getValue()));
                p.flags.put(k, v);
            }
        }
        return p;
        }

    // ---------------------------
    // Misc
    // ---------------------------
    @Override
    public String toString() {
        return "Plot{" + worldName + "@" + chunkX + "," + chunkZ + " owner=" + owner + "}";
    }
}
