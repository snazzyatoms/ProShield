package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plot — represents a single claimed chunk.
 * Preserves existing behavior, adds helpers referenced by listeners/commands.
 */
public class Plot {

    private final UUID owner;
    private String name; // optional display name
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    // trusted players with roles
    private final Map<UUID, ClaimRole> trusted = new ConcurrentHashMap<>();

    // Per-claim flags/settings (all getters kept in PlotSettings)
    private final PlotSettings settings;

    public Plot(UUID owner, Chunk chunk) {
        this(owner, chunk.getWorld().getName(), chunk.getX(), chunk.getZ(), new PlotSettings());
    }

    public Plot(UUID owner, String worldName, int chunkX, int chunkZ, PlotSettings settings) {
        this.owner = owner;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.settings = (settings == null ? new PlotSettings() : settings);
    }

    /* -------- Core -------- */

    public UUID getOwner() {
        return owner;
    }

    public boolean isOwner(UUID uuid) {
        return uuid != null && uuid.equals(owner);
    }

    public String getWorldName() {
        return worldName;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public Optional<World> getWorld() {
        return Optional.ofNullable(Bukkit.getWorld(worldName));
    }

    public Chunk getChunk() {
        World w = Bukkit.getWorld(worldName);
        return (w == null ? null : w.getChunkAt(chunkX, chunkZ));
    }

    public Location getCenterLocation() {
        World w = Bukkit.getWorld(worldName);
        if (w == null) return null;
        // Center of chunk: (chunkX*16 + 8, some reasonable Y, chunkZ*16 + 8)
        return new Location(w, (chunkX << 4) + 8.5, w.getHighestBlockYAt((chunkX << 4) + 8, (chunkZ << 4) + 8) + 1, (chunkZ << 4) + 8.5);
    }

    /* -------- Naming / Display -------- */

    public String getName() {
        if (name != null && !name.isBlank()) return name;
        // fallback to owner uuid shorthand
        return owner.toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    /** Safe plain display name for logs/messages without color. */
    public String getDisplayNameSafe() {
        return getName();
    }

    /* -------- Trusted / Roles -------- */

    public Map<UUID, ClaimRole> getTrusted() {
        return Collections.unmodifiableMap(trusted);
    }

    public void addTrusted(UUID uuid, ClaimRole role) {
        if (uuid == null || role == null) return;
        trusted.put(uuid, role);
    }

    /** Overload for historical code that passed names. */
    public void addTrusted(String playerName, ClaimRole role) {
        if (playerName == null) return;
        UUID id = Bukkit.getOfflinePlayer(playerName).getUniqueId();
        addTrusted(id, role);
    }

    public void removeTrusted(UUID uuid) {
        if (uuid == null) return;
        trusted.remove(uuid);
    }

    /** Overload for historical code that passed names. */
    public void removeTrusted(String playerName) {
        if (playerName == null) return;
        UUID id = Bukkit.getOfflinePlayer(playerName).getUniqueId();
        removeTrusted(id);
    }

    public boolean isTrusted(UUID uuid) {
        return uuid != null && trusted.containsKey(uuid);
    }

    public Optional<ClaimRole> getRole(UUID uuid) {
        if (uuid == null) return Optional.empty();
        return Optional.ofNullable(trusted.get(uuid));
    }

    public boolean isTrustedOrOwner(UUID uuid) {
        return isOwner(uuid) || isTrusted(uuid);
    }

    public boolean isTrustedOrOwner(Player p) {
        return p != null && isTrustedOrOwner(p.getUniqueId());
    }

    /* -------- Settings / Flags -------- */

    public PlotSettings getSettings() {
        return settings;
    }

    // Generic flag access used by FlagsListener (string-based)
    public boolean isFlagEnabled(String key) {
        key = (key == null ? "" : key.toLowerCase(Locale.ROOT));
        switch (key) {
            case "pvp":               return settings.isPvpEnabled();
            case "explosions":        return settings.isExplosionsAllowed();
            case "fire":              return settings.isFireAllowed();
            case "entity-grief":      return settings.isEntityGriefingAllowed();
            case "interactions":      return settings.isInteractionsAllowed();
            case "containers":        return settings.isContainersAllowed();
            case "animal-interact":   return settings.isAnimalInteractAllowed();
            case "armor-stands":      return settings.isArmorStandsAllowed();
            case "pets":              return settings.isPetAccessAllowed();
            case "vehicles":          return settings.isVehiclesAllowed();
            case "keep-items":        return settings.isKeepItemsEnabled();
            default:                  return false;
        }
    }

    public void setFlag(String key, boolean value) {
        key = (key == null ? "" : key.toLowerCase(Locale.ROOT));
        switch (key) {
            case "pvp":               settings.setPvpEnabled(value); break;
            case "explosions":        settings.setExplosionsAllowed(value); break;
            case "fire":              settings.setFireAllowed(value); break;
            case "entity-grief":      settings.setEntityGriefingAllowed(value); break;
            case "interactions":      settings.setInteractionsAllowed(value); break;
            case "containers":        settings.setContainersAllowed(value); break;
            case "animal-interact":   settings.setAnimalInteractAllowed(value); break;
            case "armor-stands":      settings.setArmorStandsAllowed(value); break;
            case "pets":              settings.setPetAccessAllowed(value); break;
            case "vehicles":          settings.setVehiclesAllowed(value); break;
            case "keep-items":        settings.setKeepItemsEnabled(value); break;
            default: break;
        }
    }

    /**
     * Nearest point on this plot's chunk border relative to a given location.
     * Used by mob-repel logic; simple geometry to snap to nearest edge.
     */
    public Location getNearestBorder(Location from) {
        World w = Bukkit.getWorld(worldName);
        if (w == null || from == null) return null;

        int minX = chunkX << 4;
        int maxX = minX + 15;
        int minZ = chunkZ << 4;
        int maxZ = minZ + 15;

        double x = from.getX();
        double z = from.getZ();

        double distLeft   = Math.abs(x - minX);
        double distRight  = Math.abs(x - maxX);
        double distTop    = Math.abs(z - minZ);
        double distBottom = Math.abs(z - maxZ);

        double y = from.getY();

        // Choose the nearest of four edges
        if (distLeft <= distRight && distLeft <= distTop && distLeft <= distBottom) {
            return new Location(w, minX - 0.01, y, z);
        } else if (distRight <= distTop && distRight <= distBottom) {
            return new Location(w, maxX + 0.01, y, z);
        } else if (distTop <= distBottom) {
            return new Location(w, x, y, minZ - 0.01);
        } else {
            return new Location(w, x, y, maxZ + 0.01);
        }
    }
}
