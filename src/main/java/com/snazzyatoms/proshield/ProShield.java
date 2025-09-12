// src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

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

    /* -------------------------------------------------------
     * Singletons / Managers
     * ------------------------------------------------------- */
    private static ProShield instance;

    private MessagesUtil messages;
    private PlotManager plotManager;
    private ClaimRoleManager roleManager;

    private GUICache guiCache;
    private GUIManager guiManager;
    private CompassManager compassManager;

    /* Debug toggle used by MessagesUtil.debug(...) */
    private boolean debugEnabled = false;

    public static ProShield getInstance() {
        return instance;
    }

    /* -------------------------------------------------------
     * Lifecycle
     * ------------------------------------------------------- */
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Utilities
        messages = new MessagesUtil(this);

        // Core managers
        roleManager = new ClaimRoleManager(null); // temp null until after plotManager init
        plotManager = new PlotManager(this, roleManager);
        roleManager.setPlotManager(plotManager); // circular link

        // GUI stack
        guiCache = new GUICache();
        guiManager = new GUIManager(this);
        compassManager = new CompassManager(this, guiManager);

        // Commands
        registerCommands();

        // Listeners
        registerListeners();

        // Background tasks
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
     * Wiring
     * ------------------------------------------------------- */
    private void registerCommands() {
        ProShieldCommand cmd = new ProShieldCommand(this, plotManager, roleManager, guiManager, compassManager, messages);
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(cmd);
            getCommand("proshield").setTabCompleter(cmd);
        }
    }

    private void registerListeners() {
        // GUI root listener (config-driven)
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);

        // Claim enter/exit messages
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(this, plotManager, messages), this);

        // TODO: Condense other protection listeners into a single ProtectionListener later
    }

    private void scheduleTasks() {
        try {
            new EntityMobRepelTask(this, plotManager).runTaskTimer(this, 20L, 20L * 5);
        } catch (Throwable ignored) {}
        try {
            new EntityBorderRepelTask(this, plotManager).runTaskTimer(this, 20L, 20L * 3);
        } catch (Throwable ignored) {}
    }

    /* -------------------------------------------------------
     * Getters used across the codebase
     * ------------------------------------------------------- */
    public MessagesUtil getMessagesUtil() {
        return messages;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
    }

    public GUICache getGuiCache() {
        return guiCache;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public CompassManager getCompassManager() {
        return compassManager;
    }

    /* -------------------------------------------------------
     * Debug hooks
     * ------------------------------------------------------- */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
    }

    public void toggleDebug() {
        this.debugEnabled = !this.debugEnabled;
        if (messages != null) {
            messages.debug("Debug mode toggled: " + (this.debugEnabled ? "ON" : "OFF"));
        }
    }
}
