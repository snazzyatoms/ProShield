package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;

    private MobBorderRepelListener mobRepelTask;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        reloadConfig();

        // Initialize core managers
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.guiManager = new GUIManager(this);

        // Register commands
        ProShieldCommand cmd = new ProShieldCommand(this, plotManager, guiManager);
        getCommand("proshield").setExecutor(cmd);
        getCommand("proshield").setTabCompleter(cmd);

        // Register event listeners
        registerListeners();

        getLogger().info("✅ ProShield v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (mobRepelTask != null) {
            mobRepelTask.stopTask();
        }
        getLogger().info("⛔ ProShield disabled.");
    }

    private void registerListeners() {
        // Core protections
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(plotManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);

        // GUI
        Bukkit.getPluginManager().registerEvents(new com.snazzyatoms.proshield.gui.GUIListener(guiManager), this);

        // Spawn guard protection
        Bukkit.getPluginManager().registerEvents(new SpawnGuardListener(plotManager), this);

        // Conditional mob repel system
        if (getConfig().getBoolean("protection.mobs.border-repel.enabled", true)) {
            mobRepelTask = new MobBorderRepelListener(plotManager);
            mobRepelTask.startTask();
            getLogger().info("🟢 Mob repel system enabled.");
        } else {
            getLogger().info("⚪ Mob repel system disabled in config.");
        }
    }

    public void reloadAllConfigs() {
        reloadConfig();
        roleManager.reloadFromConfig();

        // Restart repel task if needed
        if (mobRepelTask != null) {
            mobRepelTask.stopTask();
            mobRepelTask = null;
        }
        if (getConfig().getBoolean("protection.mobs.border-repel.enabled", true)) {
            mobRepelTask = new MobBorderRepelListener(plotManager);
            mobRepelTask.startTask();
        }

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
}
