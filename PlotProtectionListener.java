package com.proshield.listeners;

import com.proshield.ProShield;
import com.proshield.managers.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlotProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final boolean protectOutsidePlots;

    public PlotProtectionListener(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();

        // Load option from config.yml
        this.protectOutsidePlots = plugin.getConfig().getBoolean("protection.protect-outside-plots", false);

        Bukkit.getLogger().info("[ProShield] Plot protection mode: "
                + (protectOutsidePlots ? "Locked to plots only" : "Free build outside plots"));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        boolean insidePlot = plotManager.isInsideClaim(player.getLocation(), player);

        if (protectOutsidePlots && !insidePlot) {
            // Restrict building outside plots
            event.setCancelled(true);
            player.sendMessage("§cYou can only build inside your claimed plot!");
        }
        // If not protecting outside, always allow building outside plots.
        // Inside plots are still protected against non-owners (PlotManager should handle that).
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        boolean insidePlot = plotManager.isInsideClaim(player.getLocation(), player);

        if (protectOutsidePlots && !insidePlot) {
            // Restrict breaking outside plots
            event.setCancelled(true);
            player.sendMessage("§cYou can only break blocks inside your claimed plot!");
        }
        // If not protecting outside, allow breaking outside plots.
    }
}
