package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a single claimed chunk with its settings and trusted players.
 * Preserves prior fields/methods and extends with missing helpers (getX, getZ, setOwner, etc).
 */
public class Plot {

    private final String worldName;
    private final int x;
    private final int z;

    private UUID owner;
    private String name;

    private final Map<UUID, com.snazzyatoms.proshield.roles.ClaimRole> trusted = new HashMap<>();
    private final PlotSettings settings = new PlotSettings();

    public Plot(Chunk chunk, UUID owner, String name) {
        this.worldName = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.owner = owner;
        this.name = name;
    }

    /* -------------------------------------------------------
     * Core Info
     * ------------------------------------------------------- */

    public String getWorldName() {
        return worldName;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** Returns a safe display name for messages. */
    public String getDisplayNameSafe() {
        return (name != null && !name.isBlank()) ? name : (owner != null ? owner.toString() : worldName + ":" + x + "," + z);
    }

    /* -------------------------------------------------------
     * Trusted Players
     * ------------------------------------------------------- */

    public Map<UUID, com.snazzyatoms.proshield.roles.ClaimRole> getTrusted() {
        return trusted;
    }

    public boolean isOwner(UUID uuid) {
        return owner != null && owner.equals(uuid);
    }

    public void addTrusted(UUID playerId, com.snazzyatoms.proshield.roles.ClaimRole role) {
        trusted.put(playerId, role);
    }

    public void removeTrusted(UUID playerId) {
        trusted.remove(playerId);
    }

    /* -------------------------------------------------------
     * World / Chunk Access
     * ------------------------------------------------------- */

    public World getWorld() {
        return Bukkit.getServer().getWorld(worldName);
    }

    public Chunk getChunk() {
        World w = getWorld();
        return (w != null) ? w.getChunkAt(x, z) : null;
    }

    /* -------------------------------------------------------
     * Settings
     * ------------------------------------------------------- */

    public PlotSettings getSettings() {
        return settings;
    }
}
