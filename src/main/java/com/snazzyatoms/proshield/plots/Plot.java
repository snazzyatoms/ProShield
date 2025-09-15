package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * Represents a claimed chunk in ProShield
 * with owner, flags, and trusted players.
 */
public class Plot {

    private final UUID id;
    private UUID ownerId;
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    // Claim data
    private final Map<String, Boolean> flags = new HashMap<>();
    private final Map<String, String> trusted = new HashMap<>();

    public Plot(UUID id, UUID ownerId, String worldName, int chunkX, int chunkZ) {
        this.id = id;
        this.ownerId = ownerId;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    // --- Identity ---
    public UUID getId() { return id; }
    public UUID getOwner() { return ownerId; }
    public void setOwner(UUID ownerId) { this.ownerId = ownerId; }
    public String getWorldName() { return worldName; }

    // --- Chunk coords ---
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    // Aliases used in ClaimPreview
    public int getX() { return chunkX; }
    public int getZ() { return chunkZ; }

    // --- Owner name ---
    public String getOwnerName() {
        if (ownerId == null) return "Unknown";
        OfflinePlayer op = Bukkit.getOfflinePlayer(ownerId);
        return op != null && op.getName() != null ? op.getName() : ownerId.toString();
    }

    // --- Trusted players ---
    public Map<String, String> getTrusted() {
        return Collections.unmodifiableMap(trusted);
    }
    public boolean isTrusted(UUID uuid) {
        if (uuid == null) return false;
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        if (op == null) return false;
        return trusted.containsKey(op.getName());
    }
    public void addTrusted(String name, String role) {
        trusted.put(name, role);
    }
    public void removeTrusted(String name) {
        trusted.remove(name);
    }

    // --- Flags ---
    public Map<String, Boolean> getFlags() {
        return flags;
    }
    public boolean getFlag(String key, boolean def) {
        return flags.getOrDefault(key, def);
    }
    public void setFlag(String key, boolean value) {
        flags.put(key, value);
    }

    // --- Checks ---
    public boolean isOwner(UUID uuid) {
        return uuid != null && uuid.equals(ownerId);
    }
}
