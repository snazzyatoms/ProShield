// src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.PlayerCommandDispatcher;
import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.compass.CompassListener;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.EntityBorderRepelTask;
import com.snazzyatoms.proshield.plots.EntityMobRepelTask;
import com.snazzyatoms.proshield.plots.PlotListener;
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

        // Utilities
        messages = new MessagesUtil(this);

        // Core managers
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this, plotManager);

        // GUI + Compass
        guiCache = new GUICache();
        guiManager = new GUIManager(this);
        compassManager = new CompassManager(this, guiManager);

        // Commands
        registerCommands();

        // Listeners
        registerListeners();

        // Background tasks (repel)
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

    private void registerCommands() {
        // Root /proshield command
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(
                new ProShieldCommand(this, plotManager, guiManager, compassManager)
            );
        }

        // Player dispatcher for claim-related commands
        PlayerCommandDispatcher playerCommandDispatcher =
            new PlayerCommandDispatcher(this, plotManager, roleManager, messages);

        if (getCommand("claim") != null) getCommand("claim").setExecutor(playerCommandDispatcher);
        if (getCommand("unclaim") != null) getCommand("unclaim").setExecutor(playerCommandDispatcher);
        if (getCommand("trust") != null) getCommand("trust").setExecutor(playerCommandDispatcher);
        if (getCommand("untrust") != null) getCommand("untrust").setExecutor(playerCommandDispatcher);
        if (getCommand("roles") != null) getCommand("roles").setExecutor(playerCommandDispatcher);
        if (getCommand("transfer") != null) getCommand("transfer").setExecutor(playerCommandDispatcher);
    }

    private void registerListeners() {
        // GUI
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);

        // Claims / protections
        Bukkit.getPluginManager().registerEvents(new PlotListener(this, plotManager, roleManager, messages), this);

        // Compass
        Bukkit.getPluginManager().registerEvents(new CompassListener(this, compassManager), this);
    }

    private void scheduleTasks() {
        // Configurable task intervals
        int mobRepelInterval = getConfig().getInt("protection.mobs.mob-repel-task-interval", 100);
        int borderRepelInterval = getConfig().getInt("protection.mobs.border-repel.interval-ticks", 60);

        // Mob repel (despawn + reset target)
        if (getConfig().getBoolean("protection.mobs.despawn-inside", true)) {
            new EntityMobRepelTask(this, plotManager).runTaskTimer(this, 20L, mobRepelInterval);
            if (isDebugEnabled()) {
                getLogger().info("[Debug] Mob repel task scheduled every " + mobRepelInterval + " ticks.");
            }
        }

        // Border repel
        if (getConfig().getBoolean("protection.mobs.border-repel.enabled", true)) {
            new EntityBorderRepelTask(this, plotManager).runTaskTimer(this, 20L, borderRepelInterval);
            if (isDebugEnabled()) {
                getLogger().info("[Debug] Border repel task scheduled every " + borderRepelInterval + " ticks.");
            }
        }
    }

    // Getters
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

    // Debug support
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
