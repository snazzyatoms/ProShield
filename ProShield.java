package com.snazzyatoms.proshield;

import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;

    @Override
    public void onEnable() {
        instance = this;
        plotManager = new PlotManager();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlotProtectionListener(plotManager), this);

        // Register commands
        getCommand("proshield").setExecutor(new ProShieldCommand(plotManager));

        getLogger().info("ProShield enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield disabled.");
    }

    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }
}
