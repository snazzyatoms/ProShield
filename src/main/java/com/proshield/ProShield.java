package com.snazzyatoms.proshield;

import org.bukkit.plugin.java.JavaPlugin;
import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        // Ensure config exists
        saveDefaultConfig();

        // Init managers
        this.plotManager = new PlotManager(this);
        this.guiManager = new GUIManager(this);

        // Register command
        getCommand("proshield").setExecutor(new ProShieldCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this, guiManager), this);

        getLogger().info("ProShield 1.1.6 enabled successfully.");
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

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
