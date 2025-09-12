package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Bukkit;

import java.util.*;

/**
 * Represents a claimed plot of land in ProShield.
 */
public class Plot {

    private final UUID id; // Unique identifier for this plot
    private final Chunk chunk;
    private UUID owner;

    // Players with roles inside this claim
    private final Map<UUID, ClaimRole> roles = new HashMap<>();

    private final PlotSettings settings;

    public Plot(Chunk chunk, UUID owner) {
        this.id = UUID.randomUUID();
        this.chunk = chunk;
        this.owner = owner;
        this.settings = new PlotSettings();
    }

    // --- Core Identifiers ---

    public UUID getId() {
        return id;
    }

    public Chunk getChunk() {
        return chunk;
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

    // --- Display ---

    public String getName() {
        return owner != null ? owner.toString() : "Unowned";
    }

    public String getDisplayNameSafe() {
        if (owner == null) return "Unowned";
        String name = Bukkit.getOfflinePlayer(owner).getName();
        return (name != null) ? name : owner.toString();
    }

    public String getWorldName() {
        return chunk.getWorld().getName();
    }

    public int getX() {
        return chunk.getX();
    }

    public int getZ() {
        return chunk.getZ();
    }

    // --- Settings ---

    public PlotSettings getSettings() {
        return settings;
    }

    // --- Roles ---

    public ClaimRole getRole(UUID playerId) {
        if (isOwner(playerId)) {
            return ClaimRole.OWNER;
        }
        return roles.getOrDefault(playerId, ClaimRole.NONE);
    }

    public void setRole(UUID playerId, ClaimRole role) {
        if (role == ClaimRole.NONE) {
            roles.remove(playerId);
        } else {
            roles.put(playerId, role);
        }
    }

    public boolean containsKey(UUID playerId) {
        return roles.containsKey(playerId);
    }

    public void put(UUID playerId, ClaimRole role) {
        roles.put(playerId, role);
    }

    public Map<UUID, ClaimRole> getRoles() {
        return roles;
    }

    public List<String> getTrustedNames() {
        List<String> names = new ArrayList<>();
        for (UUID id : roles.keySet()) {
            String name = Bukkit.getOfflinePlayer(id).getName();
            names.add((name != null) ? name : id.toString());
        }
        return names;
    }
}
