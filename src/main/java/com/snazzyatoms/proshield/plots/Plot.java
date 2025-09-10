package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;

import java.util.*;

/**
 * Represents a single claimed plot (chunk).
 * Stores ownership, trusted players, roles, and per-claim settings.
 */
public class Plot {

    private final UUID owner;
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    private final Map<UUID, ClaimRole> trustedPlayers;
    private final PlotSettings settings;

    public Plot(UUID owner, String worldName, int chunkX, int chunkZ) {
        this.owner = owner;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.trustedPlayers = new HashMap<>();
        this.settings = new PlotSettings(); // default flags
    }

    // =====================
    // ✅ Ownership
    // =====================

    public UUID getOwner() {
        return owner;
    }

    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
    }

    // =====================
    // ✅ Chunk Info
    // =====================

    public String getWorldName() {
        return worldName;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    // =====================
    // ✅ Trusted Players & Roles
    // =====================

    public void trustPlayer(UUID playerId, ClaimRole role) {
        trustedPlayers.put(playerId, role);
    }

    public void removeTrusted(UUID playerId) {
        trustedPlayers.remove(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return trustedPlayers.containsKey(playerId);
    }

    public ClaimRole getRole(UUID playerId) {
        return trustedPlayers.getOrDefault(playerId, ClaimRole.VISITOR);
    }

    public Map<UUID, ClaimRole> getTrustedPlayers() {
        return Collections.unmodifiableMap(trustedPlayers);
    }

    // =====================
    // ✅ Per-Claim Settings
    // =====================

    public PlotSettings getSettings() {
        return settings;
    }

    // Convenience shortcuts for listeners:

    public boolean isPvpEnabled() {
        return settings.isPvpEnabled();
    }

    public boolean isKeepItemsEnabled() {
        return settings.isKeepItemsEnabled();
    }

    public boolean isDamageEnabled() {
        return settings.isDamageEnabled();
    }

    public boolean isPveEnabled() {
        return settings.isPveEnabled();
    }

    public boolean isEntityGriefingAllowed() {
        return settings.isEntityGriefingAllowed();
    }

    public boolean isItemFramesAllowed() {
        return settings.isItemFramesAllowed();
    }

    public boolean isVehiclesAllowed() {
        return settings.isVehiclesAllowed();
    }

    public boolean isBucketsAllowed() {
        return settings.isBucketsAllowed();
    }

    public boolean isRedstoneAllowed() {
        return settings.isRedstoneAllowed();
    }

    public boolean isContainersAllowed() {
        return settings.isContainersAllowed();
    }

    public boolean isAnimalAccessAllowed() {
        return settings.isAnimalAccessAllowed();
    }

    public boolean hasFlag(String flag) {
        return settings.hasFlag(flag);
    }

    // =====================
    // ✅ Utility
    // =====================

    public String getId() {
        return worldName + ":" + chunkX + "," + chunkZ;
    }

    @Override
    public String toString() {
        return "Plot{" +
                "owner=" + owner +
                ", world='" + worldName + '\'' +
                ", chunkX=" + chunkX +
                ", chunkZ=" + chunkZ +
                ", trusted=" + trustedPlayers.size() +
                ", settings=" + settings.getFlags() +
                '}';
    }
}
