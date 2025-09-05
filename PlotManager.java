package com.snazzyatoms.proshield.managers;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Location> plots = new HashMap<>();

    private final int defaultRadius;
    private final int maxRadius;
    private final int minGap;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
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
        plots.put(player.getUniqueId(), center);

        player.sendMessage("§aPlot claimed with radius " + radius + " at your current location!");
        return true;
    }

    public boolean hasPlot(Player player) {
        return plots.containsKey(player.getUniqueId());
    }

    public Location getPlot(Player player) {
        return plots.get(player.getUniqueId());
    }

    public void unclaimPlot(Player player) {
        plots.remove(player.getUniqueId());
        player.sendMessage("§eYour plot has been unclaimed.");
    }
}
