package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.listeners.AdminJoinListener;
import com.snazzyatoms.proshield.listeners.GUIListener;
import com.snazzyatoms.proshield.managers.PlotManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;

    @Override
    public void onEnable() {
        instance = this;

        // Create default config.yml if missing
        saveDefaultConfig();

        // Initialize managers
        plotManager = new PlotManager(this);

        // Register command executor
        getCommand("proshield").setExecutor(new ProShieldCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new AdminJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        getLogger().info("[ProShield] ProShield v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("[ProShield] ProShield has been disabled.");
    }

    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }
}
