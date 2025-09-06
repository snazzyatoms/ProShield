package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.GUI.GUIListener;
import com.snazzyatoms.proshield.GUI.GUIManager;
import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.listeners.AdminJoinListener;
import com.snazzyatoms.proshield.listeners.PlotProtectionListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private PlotManager plotManager;
    private GUIManager guiManager;

    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        // ensure data folder + config
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        // managers
        plotManager = new PlotManager(this);
        guiManager = new GUIManager(this, plotManager);

        // listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new PlotProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new AdminJoinListener(this), this);

        // command
        getCommand("proshield").setExecutor(new ProShieldCommand(this));

        getLogger().info("ProShield enabled.");
    }

    @Override
    public void onDisable() {
        // persist any in-memory updates to config
        FileConfiguration cfg = getConfig();
        plotManager.flushToConfig(cfg);
        saveConfig();
        getLogger().info("ProShield disabled.");
    }
}
