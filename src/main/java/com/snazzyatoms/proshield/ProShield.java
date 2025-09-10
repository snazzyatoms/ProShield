package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUICache;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;
    private GUICache guiCache;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // Core managers
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.guiCache = new GUICache();
        this.guiManager = new GUIManager(this, guiCache);

        // Register command
        ProShieldCommand commandExecutor = new ProShieldCommand(this, plotManager, guiManager);
        PluginCommand command = getCommand("proshield");
        if (command != null) {
            command.setExecutor(commandExecutor);
            command.setTabCompleter(commandExecutor);
        }

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, plotManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new SpawnGuardListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new MobBorderRepelListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);

        getLogger().info("✅ ProShield 1.2.5 enabled!");
    }

    @Override
    public void onDisable() {
        guiCache.clearAll();
        getLogger().info("⛔ ProShield disabled.");
    }

    /**
     * Reload plugin configuration and refresh caches.
     */
    public void reloadAllConfigs() {
        reloadConfig();

        // Re-init managers with new config
        this.plotManager.reloadFromConfig();
        this.roleManager.reloadFromConfig();

        // Clear GUI caches
        this.guiCache.clearAll();

        getLogger().info("🔄 ProShield configuration reloaded.");
    }

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
