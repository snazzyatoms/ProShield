// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import java.util.*;

public class Plot {
    private final UUID id;
    private UUID owner;

    // ✅ Added for ClaimPreview & utilities
    private final String worldName;
    private final int x; // chunk X
    private final int z; // chunk Z

    private final Set<UUID> trusted = new HashSet<>();
    private final Map<String, Boolean> flags = new HashMap<>();

    public Plot(UUID id, UUID owner, String worldName, int x, int z) {
        this.id = id;
        this.owner = owner;
        this.worldName = worldName;
        this.x = x;
        this.z = z;
    }

    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public boolean isOwner(UUID uuid) {
        return owner != null && owner.equals(uuid);
    }

    public String getWorldName() { return worldName; }
    public int getX() { return x; }
    public int getZ() { return z; }

    public void addTrusted(UUID uuid) { trusted.add(uuid); }
    public void removeTrusted(UUID uuid) { trusted.remove(uuid); }
    public boolean isTrusted(UUID uuid) { return trusted.contains(uuid); }
    public Set<UUID> getTrusted() { return Collections.unmodifiableSet(trusted); }

    public void setFlag(String key, boolean value) { flags.put(key.toLowerCase(Locale.ROOT), value); }
    public boolean getFlag(String key, boolean def) { return flags.getOrDefault(key.toLowerCase(Locale.ROOT), def); }
    public Map<String, Boolean> getFlags() { return flags; }
}
