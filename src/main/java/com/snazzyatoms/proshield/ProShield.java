// src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.*;
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
        roleManager = new ClaimRoleManager(plotManager);

        // GUI
        guiCache = new GUICache();
        guiManager = new GUIManager(this, guiCache, roleManager);

        // Compass
        compassManager = new CompassManager(this, guiManager);

        // Commands
        registerCommands();

        // Listeners
        registerListeners();

        // Tasks
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
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(
                new ProShieldCommand(this, plotManager, guiManager, compassManager)
            );
        }

        if (getCommand("claim") != null) {
            getCommand("claim").setExecutor(new ClaimCommand(this, plotManager));
        }

        if (getCommand("unclaim") != null) {
            getCommand("unclaim").setExecutor(new UnclaimCommand(this, plotManager, roleManager));
        }

        if (getCommand("trust") != null) {
            getCommand("trust").setExecutor(new TrustCommand(this, plotManager, roleManager));
        }

        if (getCommand("untrust") != null) {
            getCommand("untrust").setExecutor(new UntrustCommand(this, plotManager, roleManager));
        }

        if (getCommand("transfer") != null) {
            getCommand("transfer").setExecutor(new TransferCommand(this, plotManager, roleManager));
        }

        if (getCommand("roles") != null) {
            getCommand("roles").setExecutor(new RolesCommand(this, plotManager, roleManager, guiManager));
        }

        if (getCommand("flags") != null) {
            getCommand("flags").setExecutor(new FlagsCommand(this, guiManager));
        }
    }

    private void registerListeners() {
        // GUI system
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);

        // Unified plot listener
        Bukkit.getPluginManager().registerEvents(new PlotListener(this, plotManager, roleManager, messages), this);
    }

    private void scheduleTasks() {
        try {
            new EntityMobRepelTask(this, plotManager).runTaskTimer(this, 20L, 20L * 5);
        } catch (Throwable ignored) {}

        try {
            new EntityBorderRepelTask(this, plotManager).runTaskTimer(this, 20L, 20L * 3);
        } catch (Throwable ignored) {}
    }

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
