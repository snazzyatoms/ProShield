package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;

/**
 * Represents a claimed plot of land.
 * Each plot is identified by its chunk and has an owner, trusted players with roles,
 * and customizable flags.
 */
public class Plot {

    private final UUID id;                   // Unique plot ID
    private final UUID owner;                // Owner UUID
    private final Chunk chunk;               // The claimed chunk
    private final Map<UUID, ClaimRole> trusted; // Trusted players + their role
    private final Map<String, Boolean> flags;   // Claim flags (e.g., explosions, fire)

    public Plot(Chunk chunk, UUID owner) {
        this.id = UUID.randomUUID();
        this.chunk = chunk;
        this.owner = owner;
        this.trusted = new HashMap<>();
        this.flags = new HashMap<>();
    }

    // Alternate constructor for loading from storage
    public Plot(UUID id, UUID owner, Chunk chunk, Map<UUID, ClaimRole> trusted, Map<String, Boolean> flags) {
        this.id = id;
        this.owner = owner;
        this.chunk = chunk;
        this.trusted = trusted != null ? new HashMap<>(trusted) : new HashMap<>();
        this.flags = flags != null ? new HashMap<>(flags) : new HashMap<>();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public Chunk getChunk() {
        return chunk;
    }

    public String getOwnerNameSafe() {
        return (owner != null) ? owner.toString() : "Unknown";
    }

    public Map<UUID, ClaimRole> getTrusted() {
        return trusted;
    }

    public void trust(UUID uuid, ClaimRole role) {
        trusted.put(uuid, role);
    }

    public void untrust(UUID uuid) {
        trusted.remove(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return trusted.containsKey(uuid);
    }

    public ClaimRole getRole(UUID uuid) {
        return trusted.getOrDefault(uuid, ClaimRole.VISITOR);
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    public boolean hasFlag(String flag) {
        return flags.getOrDefault(flag, false);
    }

    public void setFlag(String flag, boolean enabled) {
        flags.put(flag, enabled);
    }

    public String getDisplayNameSafe() {
        return "Plot@" + chunk.getX() + "," + chunk.getZ();
    }

    @Override
    public String toString() {
        return "Plot{" +
                "id=" + id +
                ", owner=" + owner +
                ", chunk=" + chunk +
                ", trusted=" + trusted +
                ", flags=" + flags +
                '}';
    }
}
