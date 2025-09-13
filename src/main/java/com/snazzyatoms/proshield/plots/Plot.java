package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;

/**
 * Represents a claimed plot (chunk-based).
 */
public class Plot {
    private final UUID id;
    private UUID owner;
    private final Set<UUID> trusted = new HashSet<>();
    private final Map<String, Boolean> flags = new HashMap<>();

    // ✅ New fields for chunk location
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    public Plot(UUID id, UUID owner, String worldName, int chunkX, int chunkZ) {
        this.id = id;
        this.owner = owner;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    // --- Identity ---
    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public boolean isOwner(UUID uuid) {
        return owner != null && owner.equals(uuid);
    }

    // --- Trusted ---
    public void addTrusted(UUID uuid) { trusted.add(uuid); }
    public void removeTrusted(UUID uuid) { trusted.remove(uuid); }
    public boolean isTrusted(UUID uuid) { return trusted.contains(uuid); }
    public Set<UUID> getTrusted() { return Collections.unmodifiableSet(trusted); }

    // --- Flags ---
    public void setFlag(String key, boolean value) { flags.put(key.toLowerCase(Locale.ROOT), value); }
    public boolean getFlag(String key, boolean def) { return flags.getOrDefault(key.toLowerCase(Locale.ROOT), def); }
    public Map<String, Boolean> getFlags() { return flags; }

    // --- Location ---
    public String getWorldName() { return worldName; }
    public World getWorld() { return Bukkit.getWorld(worldName); }
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
}
