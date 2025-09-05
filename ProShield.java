package com.proshield;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.proshield.commands.ProShieldCommand;
import com.proshield.managers.PlotManager;
import com.proshield.managers.EconomyManager;
import com.proshield.managers.BackupManager;
import com.proshield.managers.DiscordManager;
import com.proshield.managers.GUIManager;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private PlotManager plotManager;
    private EconomyManager economyManager;
    private BackupManager backupManager;
    private DiscordManager discordManager;
    private GUIManager guiManager;

    @Override
public void onEnable() {
    instance = this;

    saveDefaultConfig(); // generate config.yml if missing

    // Initialize managers
    plotManager = new PlotManager(this);
    economyManager = new EconomyManager(this);
    backupManager = new BackupManager(this);
    discordManager = new DiscordManager(this);
    guiManager = new GUIManager(this);

    // Register commands
    getCommand("proshield").setExecutor(new ProShieldCommand(this));

    // Register events/listeners
    getServer().getPluginManager().registerEvents(new com.proshield.listeners.PlayerGUIListener(this), this);
    getServer().getPluginManager().registerEvents(new com.proshield.listeners.PlotProtectionListener(this), this);

    // Log startup
    Bukkit.getLogger().info("[ProShield] Plugin enabled successfully!");
}


    @Override
    public void onDisable() {
        if (backupManager != null) backupManager.shutdown();
        if (discordManager != null) discordManager.shutdown();

        Bukkit.getLogger().info("[ProShield] Plugin disabled.");
    }

    // --- Getters ---
    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public BackupManager getBackupManager() {
        return backupManager;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
