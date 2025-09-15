package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;

import java.util.*;

/**
 * Represents a claimed chunk in ProShield.
 */
public class Plot {

    private final UUID id;
    private UUID owner;
    private final String worldName;
    private final int x;
    private final int z;

    private final Set<UUID> trusted;
    private final Map<String, Boolean> flags;

    public Plot(UUID owner, String worldName, int x, int z) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.worldName = worldName;
        this.x = x;
        this.z = z;
        this.trusted = new HashSet<>();
        this.flags = new HashMap<>();
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

    public String getOwnerName() {
        return Bukkit.getOfflinePlayer(owner).getName();
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

    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
    }

    public Set<UUID> getTrusted() {
        return trusted;
    }

    public void addTrusted(UUID uuid) {
        trusted.add(uuid);
    }

    public void removeTrusted(UUID uuid) {
        trusted.remove(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return trusted.contains(uuid);
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    public boolean getFlag(String key, boolean def) {
        return flags.getOrDefault(key, def);
    }

    public void setFlag(String key, boolean value) {
        flags.put(key, value);
    }

    /** Optional: simple expansion (dummy stub for now). */
    public void expand(int extraRadius) {
        // Implementation could claim adjacent chunks later.
        // Stubbed to preserve API compatibility.
    }
}
