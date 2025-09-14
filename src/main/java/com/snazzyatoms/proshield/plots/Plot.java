// src/main/java/com/snazzyatoms/proshield/plots/Plot.java
package com.snazzyatoms.proshield.plots;

import java.util.*;

public class Plot {
    private final UUID id;
    private UUID owner;
    private final Set<UUID> trusted = new HashSet<>();
    private final Map<String, Boolean> flags = new HashMap<>();
    private final int radius; // ✅ radius per plot

    public Plot(UUID id, UUID owner, int radius) {
        this.id = id;
        this.owner = owner;
        this.radius = radius;
    }

    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public boolean isOwner(UUID uuid) {
        return owner != null && owner.equals(uuid);
    }

    public void addTrusted(UUID uuid) { trusted.add(uuid); }
    public void removeTrusted(UUID uuid) { trusted.remove(uuid); }
    public boolean isTrusted(UUID uuid) { return trusted.contains(uuid); }
    public Set<UUID> getTrusted() { return Collections.unmodifiableSet(trusted); }

    public void setFlag(String key, boolean value) { flags.put(key.toLowerCase(Locale.ROOT), value); }
    public boolean getFlag(String key, boolean def) { return flags.getOrDefault(key.toLowerCase(Locale.ROOT), def); }
    public Map<String, Boolean> getFlags() { return flags; }

    public int getRadius() { return radius; } // ✅ used by Claim Info GUI
}
