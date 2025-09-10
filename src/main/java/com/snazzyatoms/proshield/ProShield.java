package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import com.snazzyatoms.proshield.tasks.MobBorderRepelListener;
import com.snazzyatoms.proshield.tasks.ClaimExpiryTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;

    // Listeners & tasks
    private MobBorderRepelListener mobBorderRepelListener;
    private ClaimExpiryTask claimExpiryTask;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        reloadConfig();

        // Core managers
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.guiManager = new GUIManager(this);

        // Register command
        ProShieldCommand command = new ProShieldCommand(this, plotManager, guiManager);
        getCommand("proshield").setExecutor(command);
        getCommand("proshield").setTabCompleter(command);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(plotManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new SpawnGuardListener(plotManager), this);

        // Start tasks
        startClaimExpiryTask();
        startMobBorderRepelTask();

        getLogger().info("✅ ProShield enabled!");
    }

    @Override
    public void onDisable() {
        stopMobBorderRepelTask();
        stopClaimExpiryTask();
        getLogger().info("🛑 ProShield disabled!");
    }

    // === Task Management ===

    private void startClaimExpiryTask() {
        this.claimExpiryTask = new ClaimExpiryTask(plotManager);
        this.claimExpiryTask.runTaskTimer(this, 20L * 60L, 20L * 60L * 60L); // hourly
    }

    private void stopClaimExpiryTask() {
        if (this.claimExpiryTask != null) {
            this.claimExpiryTask.cancel();
            this.claimExpiryTask = null;
        }
    }

    private void startMobBorderRepelTask() {
        this.mobBorderRepelListener = new MobBorderRepelListener(this, plotManager);
        this.mobBorderRepelListener.start();
    }

    private void stopMobBorderRepelTask() {
        if (this.mobBorderRepelListener != null) {
            this.mobBorderRepelListener.stop();
            this.mobBorderRepelListener = null;
        }
    }

    // === Reload ===

    public void reloadAllConfigs() {
        reloadConfig();
        roleManager.reloadFromConfigSafe();
        guiManager.onConfigReload();

        // Restart tasks so config changes apply immediately
        stopClaimExpiryTask();
        startClaimExpiryTask();

        stopMobBorderRepelTask();
        startMobBorderRepelTask();

        getLogger().info("🔄 ProShield configs reloaded successfully!");
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
}
