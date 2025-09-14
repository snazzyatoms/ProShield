// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import java.util.*;

public class Plot {
    private final UUID id;
    private UUID owner;
    private String world;

    private int centerX;
    private int centerZ;
    private int radius;

    private final Set<UUID> trusted = new HashSet<>();
    private final Map<String, Boolean> flags = new HashMap<>();

    public Plot(UUID id, UUID owner) {
        this.id = id;
        this.owner = owner;
    }

    // --- IDs & ownership ---
    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }
    public boolean isOwner(UUID uuid) { return owner != null && owner.equals(uuid); }

    // --- Location ---
    public void setCenter(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }
    public int getCenterX() { return centerX; }
    public int getCenterZ() { return centerZ; }

    public void setRadius(int radius) { this.radius = radius; }
    public int getRadius() { return radius; }

    public void setWorld(String world) { this.world = world; }
    public String getWorld() { return world; }

    // --- Trust ---
    public void addTrusted(UUID uuid) { trusted.add(uuid); }
    public void removeTrusted(UUID uuid) { trusted.remove(uuid); }
    public boolean isTrusted(UUID uuid) { return trusted.contains(uuid); }
    public Set<UUID> getTrusted() { return Collections.unmodifiableSet(trusted); }

    // --- Flags ---
    public void setFlag(String key, boolean value) { flags.put(key.toLowerCase(Locale.ROOT), value); }
    public boolean getFlag(String key, boolean def) { return flags.getOrDefault(key.toLowerCase(Locale.ROOT), def); }
    public Map<String, Boolean> getFlags() { return flags; }
}
