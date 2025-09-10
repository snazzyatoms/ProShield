package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUICache;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;
    private GUICache guiCache;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // === Initialize systems ===
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.guiCache = new GUICache();
        this.guiManager = new GUIManager(this, plotManager, guiCache);

        // === Register listeners ===
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(guiManager, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new SpawnGuardListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new MobBorderRepelListener(this, plotManager), this);

        // === Register command ===
        ProShieldCommand cmd = new ProShieldCommand(this, plotManager, guiManager);
        getCommand("proshield").setExecutor(cmd);
        getCommand("proshield").setTabCompleter(cmd);

        getLogger().info("ProShield v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (plotManager != null) {
            plotManager.saveClaims();
        }
        getLogger().info("ProShield disabled.");
    }

    /**
     * Reload all configurations & caches safely.
     */
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        plotManager.reloadFromConfig();
        roleManager.reloadFromConfig();
        guiCache.clearAll();
        getLogger().info("ProShield configuration fully reloaded.");
    }

    // === Getters ===
    public PlotManager getPlotManager() {
        return plotManager;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public GUICache getGuiCache() {
        return guiCache;
    }
}
