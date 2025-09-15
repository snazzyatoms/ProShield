package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class Plot {
    private final UUID id;
    private UUID ownerId;
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    public Plot(UUID id, UUID ownerId, String worldName, int chunkX, int chunkZ) {
        this.id = id;
        this.ownerId = ownerId;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getWorldName() { return worldName; }
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }

    public boolean isOwner(UUID uuid) {
        return uuid != null && uuid.equals(ownerId);
    }

    public String getOwnerName() {
        if (ownerId == null) return "Unknown";
        OfflinePlayer op = Bukkit.getOfflinePlayer(ownerId);
        String name = op != null ? op.getName() : null;
        return name != null ? name : ownerId.toString();
    }
}
