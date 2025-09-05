package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.listeners.AdminJoinListener;
import com.snazzyatoms.proshield.listeners.PlotProtectionListener;
import com.snazzyatoms.proshield.managers.PlotManager;
import com.snazzyatoms.proshield.commands.ProShieldCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;

    @Override
    public void onEnable() {
        instance = this;
        this.plotManager = new PlotManager();

        // Register commands
        getCommand("proshield").setExecutor(new ProShieldCommand(this));

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlotProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new AdminJoinListener(), this);

        getLogger().info("ProShield v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield has been disabled.");
    }

    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }
}
