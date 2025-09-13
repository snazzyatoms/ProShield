// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.*;

public class Plot {

    private final UUID id;
    private UUID owner;
    private final String worldName;
    private final int x;
    private final int z;

    private final Map<String, Boolean> flags = new HashMap<>();
    private final Set<UUID> trusted = new HashSet<>();

    public Plot(Chunk chunk, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.owner = ownerId;
        this.worldName = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID newOwner) { this.owner = newOwner; }

    public String getWorldName() { return worldName; }
    public int getX() { return x; }
    public int getZ() { return z; }

    // Flags
    public boolean getFlag(String key, boolean def) {
        return flags.getOrDefault(key.toLowerCase(Locale.ROOT), def);
    }

    public void setFlag(String key, boolean value) {
        flags.put(key.toLowerCase(Locale.ROOT), value);
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    // Trust
    public void addTrusted(UUID playerId) { trusted.add(playerId); }
    public void removeTrusted(UUID playerId) { trusted.remove(playerId); }
    public boolean isTrusted(UUID playerId) { return trusted.contains(playerId); }
    public boolean isOwner(UUID playerId) { return owner.equals(playerId); }

    public String getDisplayNameSafe() {
        return owner.toString() + "'s Claim";
    }

    public Chunk getChunk(World world) {
        return world.getChunkAt(x, z);
    }
}
