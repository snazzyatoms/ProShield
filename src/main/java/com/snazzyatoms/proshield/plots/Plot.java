package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;

import java.util.*;

/**
 * Represents a claimed plot of land in ProShield.
 */
public class Plot {

    private final UUID id;
    private final UUID ownerId;
    private final Set<UUID> trusted;
    private final Map<String, Boolean> flags;

    public Plot(UUID id, UUID ownerId) {
        this.id = id;
        this.ownerId = ownerId;
        this.trusted = new HashSet<>();
        this.flags = new HashMap<>();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    /** NEW: Returns the owner’s name (for messages and GUIs). */
    public String getOwnerName() {
        return Bukkit.getOfflinePlayer(ownerId).getName();
    }

    public boolean isOwner(UUID playerId) {
        return ownerId.equals(playerId);
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
}
