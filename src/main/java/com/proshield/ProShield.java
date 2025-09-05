package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.listeners.AdminJoinListener;
import com.snazzyatoms.proshield.listeners.CompassListener;
import com.snazzyatoms.proshield.listeners.GUIListener;
import com.snazzyatoms.proshield.managers.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config.yml if missing
        saveDefaultConfig();

        // Initialize PlotManager
        this.plotManager = new PlotManager(this);

        // Register commands
        getCommand("proshield").setExecutor(new ProShieldCommand(this));

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(new AdminJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CompassListener(), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);

        getLogger().info(ChatColor.GREEN + "[ProShield] Enabled v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "[ProShield] Disabled.");
    }

    // Access plugin instance
    public static ProShield getInstance() {
        return instance;
    }

    // Access PlotManager
    public PlotManager getPlotManager() {
        return plotManager;
    }
}
