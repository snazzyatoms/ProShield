package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.cache.GUICache;
import com.snazzyatoms.proshield.commands.ProShieldCommand;
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
        reloadConfig();

        // Initialize managers
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.guiCache = new GUICache();
        this.guiManager = new GUIManager(this, guiCache);

        // Register commands
        ProShieldCommand commandExecutor = new ProShieldCommand(this, plotManager, guiManager);
        PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            cmd.setExecutor(commandExecutor);
            cmd.setTabCompleter(commandExecutor);
        }

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, plotManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new SpawnClaimGuardListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MobBorderRepelListener(this), this);

        getLogger().info("✅ ProShield v1.2.5 enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("⛔ ProShield disabled.");
    }

    // ==========================
    // RELOAD SUPPORT
    // ==========================
    public void reloadAllConfigs() {
        reloadConfig();
        roleManager.reloadFromConfig();
        guiCache.clearGUIs();
        guiCache.clearItems();
        getLogger().info("♻️ ProShield configuration & cache reloaded.");
    }

    // ==========================
    // GETTERS
    // ==========================
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
