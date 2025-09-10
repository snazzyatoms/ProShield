package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager
 * Handles storage and lookups for claims.
 * - Provides helpers for ownership, trust, and flags
 * - Persists through config.yml
 */
public class PlotManager {

    private final ProShield plugin;

    /** Map chunk key -> Plot */
    private final Map<String, Plot> claims = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    /* ---------------------------------------------------------
     * Core Claim Helpers
     * --------------------------------------------------------- */

    public Plot getPlot(Chunk chunk) {
        return claims.get(getChunkKey(chunk));
    }

    public Plot getClaim(Location loc) {
        if (loc == null) return null;
        return claims.get(getChunkKey(loc.getChunk()));
    }

    public String getClaimName(Location loc) {
        Plot plot = getClaim(loc);
        return (plot != null) ? plot.getName() : null;
    }

    public boolean isClaimed(Location loc) {
        return getClaim(loc) != null;
    }

    public boolean hasAnyClaim(UUID playerId) {
        for (Plot plot : claims.values()) {
            if (plot.getOwner().equals(playerId)) return true;
        }
        return false;
    }

    public boolean isOwner(UUID playerId, Location loc) {
        Plot plot = getClaim(loc);
        return plot != null && plot.isOwner(playerId);
    }

    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getClaim(loc);
        if (plot == null) return false;
        return plot.isOwner(playerId) || plot.getTrusted().containsKey(playerId);
    }

    public Plot createClaim(UUID owner, Location loc) {
        if (isClaimed(loc)) return null;
        String key = getChunkKey(loc.getChunk());
        Plot plot = new Plot(owner, loc.getChunk());
        claims.put(key, plot);
        saveClaim(plot);
        return plot;
    }

    public void unclaim(Location loc) {
        Plot plot = getClaim(loc);
        if (plot != null) {
            claims.remove(getChunkKey(loc.getChunk()));
            removeClaimFromConfig(plot);
        }
    }

    public void transferOwnership(Plot plot, UUID newOwner) {
        if (plot == null || newOwner == null) return;
        plot.setOwner(newOwner);
        saveClaim(plot);
    }

    /* ---------------------------------------------------------
     * Persistence
     * --------------------------------------------------------- */

    public void loadFromConfig() {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.isConfigurationSection("claims")) return;

        claims.clear();

        for (String key : cfg.getConfigurationSection("claims").getKeys(false)) {
            String path = "claims." + key;
            UUID owner = UUID.fromString(cfg.getString(path + ".owner"));
            PlotSettings settings = new PlotSettings();

            settings.setPvpEnabled(cfg.getBoolean(path + ".settings.pvp", false));
            settings.setExplosionsAllowed(cfg.getBoolean(path + ".settings.explosions", false));
            settings.setFireSpreadAllowed(cfg.getBoolean(path + ".settings.fire", false));
            settings.setEntityGriefingAllowed(cfg.getBoolean(path + ".settings.entitygrief", false));
            settings.setKeepItemsEnabled(cfg.getBoolean(path + ".settings.keepitems", false));
            settings.setDamageEnabled(cfg.getBoolean(path + ".settings.damage", true));
            settings.setPveEnabled(cfg.getBoolean(path + ".settings.pve", true));
            settings.setBucketsAllowed(cfg.getBoolean(path + ".settings.buckets", false));
            settings.setItemFramesAllowed(cfg.getBoolean(path + ".settings.itemframes", false));
            settings.setVehiclesAllowed(cfg.getBoolean(path + ".settings.vehicles", false));
            settings.setRedstoneAllowed(cfg.getBoolean(path + ".settings.redstone", true));
            settings.setContainerAccessAllowed(cfg.getBoolean(path + ".settings.containers", false));
            settings.setAnimalAccessAllowed(cfg.getBoolean(path + ".settings.animals", false));

            // Rebuild plot object
            Plot plot = new Plot(owner, key, settings);

            // Load trusted players
            if (cfg.isConfigurationSection(path + ".trusted")) {
                for (String uuidStr : cfg.getConfigurationSection(path + ".trusted").getKeys(false)) {
                    UUID uuid = UUID.fromString(uuidStr);
                    String roleName = cfg.getString(path + ".trusted." + uuidStr, "MEMBER");
                    ClaimRole role = ClaimRole.fromString(roleName, ClaimRole.MEMBER);
                    plot.addTrusted(uuid, role);
                }
            }

            claims.put(key, plot);
        }
    }

    public void saveClaim(Plot plot) {
        FileConfiguration cfg = plugin.getConfig();
        String path = "claims." + getChunkKey(plot.getChunk());

        cfg.set(path + ".owner", plot.getOwner().toString());
        cfg.set(path + ".settings.pvp", plot.getSettings().isPvpEnabled());
        cfg.set(path + ".settings.explosions", plot.getSettings().isExplosionsAllowed());
        cfg.set(path + ".settings.fire", plot.getSettings().isFireSpreadAllowed());
        cfg.set(path + ".settings.entitygrief", plot.getSettings().isEntityGriefingAllowed());
        cfg.set(path + ".settings.keepitems", plot.getSettings().isKeepItemsEnabled());
        cfg.set(path + ".settings.damage", plot.getSettings().isDamageEnabled());
        cfg.set(path + ".settings.pve", plot.getSettings().isPveEnabled());
        cfg.set(path + ".settings.buckets", plot.getSettings().isBucketsAllowed());
        cfg.set(path + ".settings.itemframes", plot.getSettings().isItemFramesAllowed());
        cfg.set(path + ".settings.vehicles", plot.getSettings().isVehiclesAllowed());
        cfg.set(path + ".settings.redstone", plot.getSettings().isRedstoneAllowed());
        cfg.set(path + ".settings.containers", plot.getSettings().isContainerAccessAllowed());
        cfg.set(path + ".settings.animals", plot.getSettings().isAnimalAccessAllowed());

        // Trusted players
        for (Map.Entry<UUID, ClaimRole> entry : plot.getTrusted().entrySet()) {
            cfg.set(path + ".trusted." + entry.getKey().toString(), entry.getValue().name());
        }

        plugin.saveConfig();
    }

    public void removeClaimFromConfig(Plot plot) {
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("claims." + getChunkKey(plot.getChunk()), null);
        plugin.saveConfig();
    }

    public void reloadFromConfig() {
        loadFromConfig();
    }

    /* ---------------------------------------------------------
     * Helpers
     * --------------------------------------------------------- */

    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }
}
