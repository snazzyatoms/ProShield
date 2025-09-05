package com.snazzyatoms.proshield;

import org.bukkit.plugin.java.JavaPlugin;

import com.snazzyatoms.proshield.listeners.PlotProtectionListener;
import com.snazzyatoms.proshield.managers.PlotManager;
import com.snazzyatoms.proshield.commands.ProShieldCommand;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;

    @Override
    public void onEnable() {
        instance = this;

        // Load config.yml
        saveDefaultConfig();

        // Initialize Plot Manager
        plotManager = new PlotManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlotProtectionListener(plotManager), this);

        // Register commands
        getCommand("proshield").setExecutor(new ProShieldCommand(plotManager));

        getLogger().info("ProShield has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield has been disabled!");
    }

    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }
}
