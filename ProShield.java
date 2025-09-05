package com.snazzyatoms.proshield;

import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Initialize plot manager
        plotManager = new PlotManager(this);

        // Register events
        getServer().getPluginManager().registerEvents(new PlotProtectionListener(this), this);

        // Register commands
        getCommand("proshield").setExecutor(new ProShieldCommand(this));

        getLogger().info("ProShield enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield disabled!");
    }

    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }
}
