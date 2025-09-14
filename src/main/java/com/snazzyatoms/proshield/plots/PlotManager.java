package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>(); // key = world:x:z

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    private String key(String world, int x, int z) {
        return world + ":" + x + ":" + z;
    }

    private String key(Chunk chunk) {
        return key(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    // --- Core API ---
    public Plot createPlot(Player player, Location loc) {
        Chunk chunk = loc.getChunk();
        Plot plot = new Plot(player.getUniqueId(), chunk);
        plots.put(key(chunk), plot);
        return plot;
    }

    public void removePlot(Location loc) {
        plots.remove(key(loc.getWorld().getName(), loc.getChunk().getX(), loc.getChunk().getZ()));
    }

    public Plot getPlot(Location loc) {
        return plots.get(key(loc.getWorld().getName(), loc.getChunk().getX(), loc.getChunk().getZ()));
    }

    public void expandClaim(UUID playerId, int extraRadius) {
        for (Plot plot : plots.values()) {
            if (plot.getOwner().equals(playerId)) {
                plot.expand(extraRadius);
            }
        }
    }

    // --- Utility ---
    public String getPlayerName(UUID uuid) {
        return Optional.ofNullable(Bukkit.getOfflinePlayer(uuid).getName()).orElse(uuid.toString());
    }

    public Collection<Plot> getAllPlots() {
        return plots.values();
    }
}
