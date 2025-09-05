package com.snazzyatoms.proshield.managers;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlotManager {

    private final ProShield plugin;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Simple placeholder logic: Always allow ops, deny everyone else.
     * Later this can be expanded into real plot ownership logic.
     */
    public boolean canBuild(Player player, Location location) {
        return player.isOp();
    }
}
