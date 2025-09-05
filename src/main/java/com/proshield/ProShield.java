package com.snazzyatoms.proshield;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.listeners.AdminJoinListener;
import com.snazzyatoms.proshield.listeners.PlotProtectionListener;
import com.snazzyatoms.proshield.managers.PlotManager;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize PlotManager
        plotManager = new PlotManager(this);

        // Register commands
        getCommand("proshield").setExecutor(new ProShieldCommand(this));

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new AdminJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlotProtectionListener(this), this);

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
