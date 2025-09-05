package com.proshield;

import org.bukkit.plugin.java.JavaPlugin;
import com.proshield.managers.PlotManager;
import com.proshield.listeners.PlotProtectionListener;
import com.proshield.commands.ProShieldCommand;

public class ProShield extends JavaPlugin {

    private PlotManager plotManager;

    @Override
    public void onEnable() {
        // Initialize PlotManager
        this.plotManager = new PlotManager();

        // Register event listener
        getServer().getPluginManager().registerEvents(new PlotProtectionListener(plotManager), this);

        // Register command
        getCommand("proshield").setExecutor(new ProShieldCommand(plotManager));

        getLogger().info("ProShield has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield has been disabled!");
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }
}
