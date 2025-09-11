package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a land claim ("plot").
 *
 * ✅ Preserves original ownership + trust logic
 * ✅ Uses Bukkit's Chunk instead of DummyChunk
 * ✅ Fixed @Override errors (removed invalid annotations)
 * ✅ Added getNearestBorder(Location) used by MobBorderRepelListener
 */
public class Plot {

    private final UUID owner;
    private final Set<UUID> trusted;
    private final String worldName;
    private final int x;
    private final int z;

    public Plot(UUID owner, Chunk chunk) {
        this.owner = owner;
        this.worldName = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.trusted = new HashSet<>();
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isOwner(UUID id) {
        return owner != null && owner.equals(id);
    }

    public void trust(UUID id) {
        trusted.add(id);
    }

    public void untrust(UUID id) {
        trusted.remove(id);
    }

    public boolean isTrusted(UUID id) {
        return owner != null && owner.equals(id) || trusted.contains(id);
    }

    public Set<UUID> getTrusted() {
        return Collections.unmodifiableSet(trusted);
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    /**
     * Get the Bukkit Chunk for this plot.
     */
    public Chunk getChunk() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return world.getChunkAt(x, z);
    }

    /**
     * Get the center location of this plot.
     */
    public Location getCenter() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        double cx = (x << 4) + 8;
        double cz = (z << 4) + 8;
        return new Location(world, cx, world.getHighestBlockYAt((int) cx, (int) cz), cz);
    }

    /**
     * Serialize to config.
     */
    public void save(ConfigurationSection sec) {
        sec.set("owner", owner.toString());
        sec.set("world", worldName);
        sec.set("x", x);
        sec.set("z", z);

        List<String> ids = new ArrayList<>();
        for (UUID t : trusted) ids.add(t.toString());
        sec.set("trusted", ids);
    }

    /**
     * Deserialize from config.
     */
    public static Plot load(ConfigurationSection sec) {
        UUID owner = UUID.fromString(sec.getString("owner"));
        String world = sec.getString("world");
        int x = sec.getInt("x");
        int z = sec.getInt("z");

        World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) return null;
        Chunk chunk = bukkitWorld.getChunkAt(x, z);

        Plot plot = new Plot(owner, chunk);

        List<String> ids = sec.getStringList("trusted");
        for (String s : ids) {
            try {
                plot.trust(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }

        return plot;
    }

    /**
     * Utility for MobBorderRepelListener.
     * Returns the nearest border corner to a location.
     */
    public Location getNearestBorder(Location loc) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        int minX = x << 4;
        int minZ = z << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        double dx = (loc.getX() < (minX + maxX) / 2.0) ? minX : maxX;
        double dz = (loc.getZ() < (minZ + maxZ) / 2.0) ? minZ : maxZ;

        return new Location(world, dx, loc.getY(), dz);
    }
}
