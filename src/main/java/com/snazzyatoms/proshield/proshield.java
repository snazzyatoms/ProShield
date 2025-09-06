package com.snazzyatoms.proshield;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.listeners.GUIListener;
import com.snazzyatoms.proshield.GUI.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;

public class ProShield extends JavaPlugin {
    private static ProShield instance;
    private PlotManager plotManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        // Managers
        this.plotManager = new PlotManager(this);
        this.guiManager = new GUIManager(this, plotManager);

        // Commands
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager, guiManager));

        // Listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);

        // Config setup
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Cleanup if needed
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

