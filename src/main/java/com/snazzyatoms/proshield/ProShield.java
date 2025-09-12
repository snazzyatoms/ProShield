// src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ClaimCommandHandler;
import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.ClaimMessageListener;
import com.snazzyatoms.proshield.plots.EntityBorderRepelTask;
import com.snazzyatoms.proshield.plots.EntityMobRepelTask;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private MessagesUtil messages;
    private PlotManager plotManager;
    private ClaimRoleManager roleManager;

    private GUICache guiCache;
    private GUIManager guiManager;
    private CompassManager compassManager;

    private boolean debugEnabled = false;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Managers
        messages = new MessagesUtil(this);
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(plotManager);

        guiCache = new GUICache();
        guiManager = new GUIManager(this);
        compassManager = new CompassManager(this, guiManager);

        // Register commands + listeners
        registerCommands();
        registerListeners();
        scheduleTasks();

        getLogger().info("ProShield v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (plotManager != null) {
            plotManager.saveAll();
        }
        getLogger().info("ProShield disabled.");
    }

    /* -------------------------------------------------------
     * Command Registration
     * ------------------------------------------------------- */
    private void registerCommands() {
        // Root admin/debug command
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(
                new ProShieldCommand(this, plotManager, guiManager, compassManager)
            );
        }

        // Unified handler for all claim-related commands
        ClaimCommandHandler claimHandler = new ClaimCommandHandler(this, plotManager, roleManager, guiManager, compassManager);

        String[] claimCommands = { "claim", "unclaim", "trust", "untrust", "roles", "transfer", "flags" };
        for (String cmd : claimCommands) {
            if (getCommand(cmd) != null) {
                getCommand(cmd).setExecutor(claimHandler);
            }
        }
    }

    /* -------------------------------------------------------
     * Listener Registration
     * ------------------------------------------------------- */
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(this, plotManager, messages), this);
        // Other protection listeners can be unified later as we condense
    }

    /* -------------------------------------------------------
     * Tasks
     * ------------------------------------------------------- */
    private void scheduleTasks() {
        try {
            new EntityMobRepelTask(this, plotManager).runTaskTimer(this, 20L, 20L * 5);
        } catch (Throwable ignored) {}

        try {
            new EntityBorderRepelTask(this, plotManager).runTaskTimer(this, 20L, 20L * 3);
        } catch (Throwable ignored) {}
    }

    /* -------------------------------------------------------
     * Getters
     * ------------------------------------------------------- */
    public MessagesUtil getMessagesUtil() { return messages; }
    public PlotManager getPlotManager() { return plotManager; }
    public ClaimRoleManager getRoleManager() { return roleManager; }
    public GUICache getGuiCache() { return guiCache; }
    public GUIManager getGuiManager() { return guiManager; }
    public CompassManager getCompassManager() { return compassManager; }

    /* -------------------------------------------------------
     * Debug
     * ------------------------------------------------------- */
    public boolean isDebugEnabled() { return debugEnabled; }
    public void setDebugEnabled(boolean enabled) { this.debugEnabled = enabled; }

    public void toggleDebug() {
        this.debugEnabled = !this.debugEnabled;
        if (messages != null) {
            messages.debug("Debug mode toggled: " + (this.debugEnabled ? "ON" : "OFF"));
        }
    }
}
