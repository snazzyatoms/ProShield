// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Plot {
    private final UUID id;
    private UUID owner; // ⚡ must not be final if we allow transfer
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    private final Map<String, Boolean> flags = new HashMap<>();
    private final Set<UUID> trusted = new HashSet<>();

    public Plot(UUID id, UUID owner, String worldName, int chunkX, int chunkZ) {
        this.id = id;
        this.owner = owner;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID newOwner) {
        this.owner = newOwner;
    }

    public boolean isOwner(UUID playerId) {
        return owner != null && owner.equals(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return isOwner(playerId) || trusted.contains(playerId);
    }

    public void addTrusted(UUID playerId) {
        trusted.add(playerId);
    }

    public void removeTrusted(UUID playerId) {
        trusted.remove(playerId);
    }

    // ✅ Added so ClaimPreview compiles
    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return chunkX;
    }

    public int getZ() {
        return chunkZ;
    }

    // Flag management
    public boolean getFlag(String flag, boolean def) {
        return flags.getOrDefault(flag, def);
    }

    public void setFlag(String flag, boolean value) {
        flags.put(flag, value);
    }
}
