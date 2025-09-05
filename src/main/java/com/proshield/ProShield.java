package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.listeners.CompassListener;
import com.snazzyatoms.proshield.listeners.GUIListener;
import com.snazzyatoms.proshield.managers.GUIManager;
import com.snazzyatoms.proshield.managers.PlotManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private PlotManager plotManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        // Generate /plugins/ProShield/config.yml on first run
        saveDefaultConfig();

        // Managers
        this.plotManager = new PlotManager(this);
        this.guiManager  = new GUIManager();

        // Commands
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(new com.snazzyatoms.proshield.commands.ProShieldCommand(this));
        } else {
            getLogger().severe("Command 'proshield' not found. Check plugin.yml!");
        }

        // Listeners
        getServer().getPluginManager().registerEvents(new CompassListener(guiManager), this);
        getServer().getPluginManager().registerEvents(new GUIListener(plotManager), this);

        getLogger().info("[ProShield] Enabled v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("[ProShield] Disabled.");
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
