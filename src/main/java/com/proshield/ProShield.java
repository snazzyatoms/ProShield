package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.listeners.GUIListener;
import com.snazzyatoms.proshield.listeners.PlotProtectionListener;
import com.snazzyatoms.proshield.managers.GUIManager;
import com.snazzyatoms.proshield.managers.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        // ✅ Ensure plugin folder exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // ✅ Save default config.yml if missing
        saveDefaultConfig();

        // ✅ Initialize managers
        plotManager = new PlotManager();
        guiManager = new GUIManager(plotManager);

        // ✅ Register listeners
        Bukkit.getPluginManager().registerEvents(new PlotProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);

        // ✅ Register commands
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(new ProShieldCommand(guiManager));
        }

        getLogger().info("✅ ProShield v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ ProShield has been disabled!");
    }

    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
