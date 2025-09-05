package com.snazzyatoms.proshield;

import org.bukkit.plugin.java.JavaPlugin;
import com.snazzyatoms.proshield.managers.PlotManager;
import com.snazzyatoms.proshield.listeners.PlotProtectionListener;
import com.snazzyatoms.proshield.commands.ProShieldCommand;

public class ProShield extends JavaPlugin {

    private PlotManager plotManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plotManager = new PlotManager(this);

        // Register events
        getServer().getPluginManager().registerEvents(new PlotProtectionListener(plotManager), this);

        // Register command
        getCommand("proshield").setExecutor(new ProShieldCommand(plotManager));
    }

    @Override
    public void onDisable() {
        // Cleanup if necessary
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }
}
