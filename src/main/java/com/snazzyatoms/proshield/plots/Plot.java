// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Plot {
    private final UUID id;
    private final UUID owner;
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    private final Map<String, Boolean> flags = new HashMap<>();

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
