package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Manages all claimed plots in ProShield.
 * Preserves legacy logic while extending with helpers for 1.2.5.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* ---------------------------------------------------------
     * 🔹 Claim Management
     * --------------------------------------------------------- */
    public Plot createClaim(UUID owner, Location loc) {
        Chunk chunk = loc.getChunk();
        String key = getKey(chunk);

        if (plots.containsKey(key)) return plots.get(key);

        Plot plot = new Plot(owner, chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        plots.put(key, plot);
        return plot;
    }

    public boolean isClaimed(Location loc) {
        return plots.containsKey(getKey(loc.getChunk()));
    }

    public Plot getPlot(Chunk chunk) {
        return plots.get(getKey(chunk));
    }

    public Plot getPlot(Location loc) {
        return plots.get(getKey(loc.getChunk()));
    }

    public void unclaim(Location loc) {
        plots.remove(getKey(loc.getChunk()));
    }

    public void transferOwnership(Plot plot, UUID newOwner) {
        if (plot == null) return;
        plots.put(getKey(plot.getWorld(), plot.getX(), plot.getZ()),
                new Plot(newOwner, plot.getWorld(), plot.getX(), plot.getZ()));
    }

    /* ---------------------------------------------------------
     * 🔹 Ownership Helpers
     * --------------------------------------------------------- */
    public boolean isOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        return plot != null && plot.isOwner(playerId);
    }

    public boolean hasAnyClaim(UUID playerId) {
        for (Plot plot : plots.values()) {
            if (plot.isOwner(playerId)) return true;
        }
        return false;
    }

    /* ---------------------------------------------------------
     * 🔹 Serialization / Persistence
     * --------------------------------------------------------- */
    public void save() {
        plugin.getConfig().set("claims", null); // clear
        for (Map.Entry<String, Plot> entry : plots.entrySet()) {
            plugin.getConfig().set("claims." + entry.getKey(), entry.getValue().serialize());
        }
        plugin.saveConfig();
    }

    @SuppressWarnings("unchecked")
    public void load() {
        plots.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("claims");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            Map<String, Object> map = (Map<String, Object>) section.get(key);
            if (map == null) continue;
            Plot plot = Plot.deserialize(map);
            plots.put(key, plot);
        }
    }

    public void reloadFromConfig() {
        plugin.reloadConfig();
        load();
    }

    /* ---------------------------------------------------------
     * 🔹 Expiry Handling
     * --------------------------------------------------------- */
    public int purgeExpired(int days, boolean dryRun) {
        int removed = 0;
        long cutoff = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000);

        Iterator<Map.Entry<String, Plot>> it = plots.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Plot> entry = it.next();
            UUID owner = entry.getValue().getOwner();

            // Use Bukkit's offline player data for last played check
            long lastPlayed = Bukkit.getOfflinePlayer(owner).getLastPlayed();
            if (lastPlayed != 0 && lastPlayed < cutoff) {
                if (!dryRun) it.remove();
                removed++;
            }
        }
        return removed;
    }

    /* ---------------------------------------------------------
     * 🔹 Utility
     * --------------------------------------------------------- */
    private String getKey(Chunk chunk) {
        return getKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    private String getKey(String world, int x, int z) {
        return world + ";" + x + ";" + z;
    }

    public Map<String, Plot> getAllPlots() {
        return Collections.unmodifiableMap(plots);
    }

    public String getClaimName(Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return null;
        return plot.getOwner().toString().substring(0, 8) + "@" + plot.getX() + "," + plot.getZ();
    }
}
