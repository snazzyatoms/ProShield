package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.GUICache;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;
    private GUICache guiCache;

    private static ProShield instance;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.guiCache = new GUICache();
        this.guiManager = new GUIManager(this, plotManager, roleManager, guiCache);

        getServer().getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        getServer().getPluginManager().registerEvents(new ClaimMessageListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new EntityDamageProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(guiManager), this);
        getServer().getPluginManager().registerEvents(new SpawnGuardListener(this, plotManager), this);
        getServer().getPluginManager().registerEvents(new MobBorderRepelListener(this, plotManager), this);

        ProShieldCommand command = new ProShieldCommand(this, plotManager, guiManager);
        getCommand("proshield").setExecutor(command);
        getCommand("proshield").setTabCompleter(command);

        getLogger().info("✅ ProShield v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("⛔ ProShield disabled.");
    }

    /** Reload configuration and reset caches */
    public void reloadAllConfigs() {
        reloadConfig();
        roleManager.reloadFromConfig();
        guiCache.clear();
        getLogger().info("🔄 ProShield config reloaded.");
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public GUICache getGuiCache() {
        return guiCache;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
    }
}
