package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Plot - single-claim data holder.
 * Preserves prior fields and extends with small helpers used by listeners/commands.
 */
public class Plot {

    private final UUID worldId;
    private final int x; // chunk X
    private final int z; // chunk Z

    private String name;             // optional display name
    private UUID owner;              // owner UUID
    private final Map<UUID, ClaimRole> trusted; // trusted players & roles

    private PlotSettings settings;

    // dirty flag for persistence batching
    private boolean dirty;

    public Plot(UUID worldId, int x, int z, UUID owner) {
        this.worldId = worldId;
        this.x = x;
        this.z = z;
        this.owner = owner;
        this.trusted = new HashMap<>();
        this.settings = new PlotSettings(); // defaults populated from config by PlotManager on first load
        this.dirty = true;
    }

    /* -------------------------
     * Basic identity
     * ------------------------- */
    public UUID getWorldId() { return worldId; }
    public int getX() { return x; }
    public int getZ() { return z; }

    /* -------------------------
     * Naming
     * ------------------------- */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; setDirty(true); }

    /** Safe name for messages (fallback to coords). */
    public String getDisplayNameSafe() {
        if (name != null && !name.isBlank()) return name;
        return "Claim@" + x + "," + z;
    }

    /* -------------------------
     * Ownership / trust
     * ------------------------- */
    public UUID getOwner() { return owner; }
    public void setOwner(UUID newOwner) { this.owner = newOwner; setDirty(true); }

    public boolean isOwner(UUID playerId) {
        return playerId != null && owner != null && owner.equals(playerId);
    }

    public Map<UUID, ClaimRole> getTrusted() {
        return Collections.unmodifiableMap(trusted);
    }

    /** Internal mutator (used by ClaimRoleManager and deserialization). */
    public void putTrusted(UUID uuid, ClaimRole role) {
        if (uuid == null || role == null) return;
        trusted.put(uuid, role);
        setDirty(true);
    }

    public void removeTrusted(UUID uuid) {
        if (uuid == null) return;
        trusted.remove(uuid);
        setDirty(true);
    }

    public boolean hasTrusted(UUID uuid) {
        return uuid != null && trusted.containsKey(uuid);
    }

    /* -------------------------
     * Settings
     * ------------------------- */
    public PlotSettings getSettings() { return settings; }
    public void setSettings(PlotSettings settings) {
        if (settings != null) this.settings = settings;
        setDirty(true);
    }

    /* -------------------------
     * Flags convenience for GUI (back-compat)
     * ------------------------- */
    public boolean isFlagEnabled(String key) {
        // Kept for back-compat: map known keys to settings
        return switch (key.toLowerCase()) {
            case "pvp" -> settings.isPvpEnabled();
            case "explosions" -> settings.isExplosionsAllowed();
            case "fire" -> settings.isFireAllowed();
            case "entity-grief" -> settings.isEntityGriefingAllowed();
            case "interactions", "redstone" -> settings.isInteractionsAllowed();
            case "containers" -> settings.isContainersAllowed();
            case "animals" -> settings.isAnimalInteractAllowed();
            case "vehicles" -> settings.isVehiclesAllowed();
            default -> false;
        };
    }

    public void setFlag(String key, boolean value) {
        switch (key.toLowerCase()) {
            case "pvp" -> settings.setPvpEnabled(value);
            case "explosions" -> settings.setExplosionsAllowed(value);
            case "fire" -> settings.setFireAllowed(value);
            case "entity-grief" -> settings.setEntityGriefingAllowed(value);
            case "interactions", "redstone" -> settings.setInteractionsAllowed(value);
            case "containers" -> settings.setContainersAllowed(value);
            case "animals" -> settings.setAnimalInteractAllowed(value);
            case "vehicles" -> settings.setVehiclesAllowed(value);
            default -> { /* ignore unknown */ }
        }
        setDirty(true);
    }

    /* -------------------------
     * Persistence
     * ------------------------- */
    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }
}
