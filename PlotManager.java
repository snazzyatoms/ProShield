package com.snazzyatoms.proshield.managers;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;

    // Stores player UUID → Plot data
    private final Map<UUID, Location> plots = new HashMap<>();

    // Config values
    private final int defaultRadius;
    private final int maxRadius;
    private final int minGap;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;

        // Load settings from config.yml
        this.defaultRadius = plugin.getConfig().getInt("protection.default-radius", 10);
        this.maxRadius = plugin.getConfig().getInt("protection.max-radius", 50);
        this.minGap = plugin.getConfig().getInt("protection.min-gap", 10);
    }

    public boolean claimPlot(Player player, int radius) {
        if (radius <= 0) {
            radius = defaultRadius;
        }

        if (radius > maxRadius) {
            player.sendMessage("§cThe maximum plot radius allowed is " + maxRadius + ".");
            return false;
        }

        Location center = player.getLocation();

        // Check overlap with existing plots
        for (Location existing : plots.values()) {
            if (center.getWorld().equals(existing.getWorld()) &&
                center.distance(existing) < minGap + radius) {
                player.sendMessage("§cYou must claim at least " + minGap + " blocks away from other plots.");
                return false;
            }
        }

        plots.put(player.getUniqueId(), center);
        player.sendMessage("§aPlot claimed successfully!");
        return true;
    }

    public Location getPlot(UUID playerId) {
        return plots.get(playerId);
    }
}
