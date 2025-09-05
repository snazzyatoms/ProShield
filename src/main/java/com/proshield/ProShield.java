package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.listeners.AdminJoinListener;
import com.snazzyatoms.proshield.listeners.GUIListener;
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

        // ✅ Ensure plugin folder & config exists
        setupFiles();

        // ✅ Initialize managers
        this.plotManager = new PlotManager(this);
        this.guiManager = new GUIManager(this, plotManager);

        // ✅ Register command
        getCommand("proshield").setExecutor(new ProShieldCommand(this, guiManager));

        // ✅ Register listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new AdminJoinListener(), this);

        getLogger().info("✅ ProShield enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("⛔ ProShield disabled.");
    }

    /**
     * Makes sure the ProShield plugin folder & config.yml exist.
     */
    private void setupFiles() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig(); // Copies config.yml from resources
            getLogger().info("📂 Created default config.yml in ProShield folder.");
        }
    }

    // 🔑 Getters
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
