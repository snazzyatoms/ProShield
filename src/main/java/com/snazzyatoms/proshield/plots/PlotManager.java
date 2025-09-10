package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all claims (plots) on the server.
 * - Handles storage, lookup, and persistence
 * - Provides helper methods for listeners and commands
 * - Syncs per-claim settings with config.yml defaults
 */
public class PlotManager {

    private final ProShield plugin;

    /** Active claims indexed by chunk key. */
    private final Map<String, Plot> claims = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    /* ---------------------------------------------------------
     * Claim helpers
     * --------------------------------------------------------- */

    public Plot getClaim(Location loc) {
        if (loc == null) return null;
        return getClaim(loc.getChunk());
    }

    public Plot getClaim(Chunk chunk) {
        if (chunk == null) return null;
        String key = key(chunk);
        return claims.get(key);
    }

    public boolean isClaimed(Location loc) {
        return getClaim(loc) != null;
    }

    public boolean hasAnyClaim(UUID uuid) {
        if (uuid == null) return false;
        for (Plot plot : claims.values()) {
            if (plot.isOwner(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOwner(UUID uuid, Location loc) {
        Plot plot = getClaim(loc);
        return plot != null && plot.isOwner(uuid);
    }

    public Plot createClaim(UUID owner, Location loc) {
        if (loc == null || owner == null) return null;
        Chunk chunk = loc.getChunk();
        String key = key(chunk);
        if (claims.containsKey(key)) return null;

        Plot plot = new Plot(owner, chunk);
        claims.put(key, plot);
        saveToConfig(plot);
        return plot;
    }

    public void unclaim(Location loc) {
        Plot plot = getClaim(loc);
        if (plot != null) {
            claims.remove(key(plot.getChunk()));
            removeFromConfig(plot);
        }
    }

    public void transferOwnership(Plot plot, String newOwnerName) {
        if (plot == null || newOwnerName == null) return;
        UUID newOwner = Bukkit.getOfflinePlayer(newOwnerName).getUniqueId();
        plot.setOwner(newOwner);
        saveToConfig(plot);
    }

    public Collection<Plot> getAllClaims() {
        return Collections.unmodifiableCollection(claims.values());
    }

    /* ---------------------------------------------------------
     * Persistence
     * --------------------------------------------------------- */

    public void reloadFromConfig() {
        claims.clear();
        loadFromConfig();
    }

    private void loadFromConfig() {
        FileConfiguration config = plugin.getConfig();
        if (!config.isConfigurationSection("claims")) return;

        for (String key : config.getConfigurationSection("claims").getKeys(false)) {
            String base = "claims." + key;

            UUID owner = UUID.fromString(config.getString(base + ".owner"));
            int x = config.getInt(base + ".x");
            int z = config.getInt(base + ".z");
            String world = config.getString(base + ".world");

            PlotSettings settings = new PlotSettings();
            settings.setPvpEnabled(config.getBoolean(base + ".settings.pvp", false));
            settings.setExplosionsAllowed(config.getBoolean(base + ".settings.explosions", false));
            settings.setFireAllowed(config.getBoolean(base + ".settings.fire", false));
            settings.setBucketsAllowed(config.getBoolean(base + ".settings.buckets", false));
            settings.setKeepItemsEnabled(config.getBoolean(base + ".settings.keepItems", false));
            settings.setEntityGriefingAllowed(config.getBoolean(base + ".settings.entityGriefing", false));
            settings.setInteractionsAllowed(config.getBoolean(base + ".settings.interactions", true));
            settings.setRedstoneAllowed(config.getBoolean(base + ".settings.redstone", true));
            settings.setContainerAccessAllowed(config.getBoolean(base + ".settings.containerAccess", true));
            settings.setItemFramesAllowed(config.getBoolean(base + ".settings.itemFrames", true));
            settings.setVehiclesAllowed(config.getBoolean(base + ".settings.vehicles", true));

            Plot plot = new Plot(owner, Bukkit.getWorld(world).getChunkAt(x, z));
            plot.setSettings(settings);
            claims.put(key, plot);
        }
    }

    private void saveToConfig(Plot plot) {
        FileConfiguration config = plugin.getConfig();
        String base = "claims." + key(plot.getChunk());

        config.set(base + ".owner", plot.getOwner().toString());
        config.set(base + ".x", plot.getChunk().getX());
        config.set(base + ".z", plot.getChunk().getZ());
        config.set(base + ".world", plot.getChunk().getWorld().getName());

        PlotSettings s = plot.getSettings();
        config.set(base + ".settings.pvp", s.isPvpEnabled());
        config.set(base + ".settings.explosions", s.isExplosionsAllowed());
        config.set(base + ".settings.fire", s.isFireAllowed());
        config.set(base + ".settings.buckets", s.isBucketsAllowed());
        config.set(base + ".settings.keepItems", s.isKeepItemsEnabled());
        config.set(base + ".settings.entityGriefing", s.isEntityGriefingAllowed());
        config.set(base + ".settings.interactions", s.isInteractionsAllowed());
        config.set(base + ".settings.redstone", s.isRedstoneAllowed());
        config.set(base + ".settings.containerAccess", s.isContainerAccessAllowed());
        config.set(base + ".settings.itemFrames", s.isItemFramesAllowed());
        config.set(base + ".settings.vehicles", s.isVehiclesAllowed());

        plugin.saveConfig();
    }

    private void removeFromConfig(Plot plot) {
        FileConfiguration config = plugin.getConfig();
        config.set("claims." + key(plot.getChunk()), null);
        plugin.saveConfig();
    }

    /* ---------------------------------------------------------
     * Internal helpers
     * --------------------------------------------------------- */

    private String key(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }
}
