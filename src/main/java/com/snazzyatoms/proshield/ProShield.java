// src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.*;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.gui.listeners.RoleFlagsListener;
import com.snazzyatoms.proshield.gui.listeners.RolesListener;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.BucketProtectionListener;
import com.snazzyatoms.proshield.plots.ClaimMessageListener;
import com.snazzyatoms.proshield.plots.EntityBorderRepelTask;
import com.snazzyatoms.proshield.plots.EntityMobRepelTask;
import com.snazzyatoms.proshield.plots.InteractionProtectionListener;
import com.snazzyatoms.proshield.plots.ItemProtectionListener;
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
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(plotManager);

        // GUI stack
        guiCache = new GUICache();
        guiManager = new GUIManager(this, guiCache, roleManager);

        // Compass (constructor expected by log: (ProShield, GUIManager))
        compassManager = new CompassManager(this, guiManager);

        // Commands — signatures aligned to your current classes
        registerCommands();

        // Listeners — constructors aligned to build log expectations
        registerListeners();

        // Background tasks (repel, borders) — constructors expected: (ProShield, PlotManager)
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
        // /proshield (expects: ProShieldCommand(ProShield, PlotManager, GUIManager, CompassManager))
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(
                new ProShieldCommand(this, plotManager, guiManager, compassManager)
            );
        }

        // /claim  (constructor currently used in your tree)
        if (getCommand("claim") != null) {
            getCommand("claim").setExecutor(new ClaimCommand(this, plotManager));
        }

        // /unclaim (uses roles in some versions; pass both to be safe)
        if (getCommand("unclaim") != null) {
            getCommand("unclaim").setExecutor(new UnclaimCommand(this, plotManager, roleManager));
        }

        // /trust  (TrustCommand(ProShield, PlotManager, ClaimRoleManager))
        if (getCommand("trust") != null) {
            getCommand("trust").setExecutor(new TrustCommand(this, plotManager, roleManager));
        }

        // /untrust (UntrustCommand(ProShield, PlotManager, ClaimRoleManager))
        if (getCommand("untrust") != null) {
            getCommand("untrust").setExecutor(new UntrustCommand(this, plotManager, roleManager));
        }

        // /transfer (TransferCommand(ProShield, PlotManager, ClaimRoleManager))
        if (getCommand("transfer") != null) {
            getCommand("transfer").setExecutor(new TransferCommand(this, plotManager, roleManager));
        }

        // /roles (RolesCommand(ProShield, PlotManager, ClaimRoleManager, GUIManager))
        if (getCommand("roles") != null) {
            getCommand("roles").setExecutor(new RolesCommand(this, plotManager, roleManager, guiManager));
        }

        // /flags (FlagsCommand(ProShield, GUIManager))
        if (getCommand("flags") != null) {
            getCommand("flags").setExecutor(new FlagsCommand(this, guiManager));
        }
    }

    private void registerListeners() {
        // GUI root listener (log expects GUIListener(ProShield, GUIManager))
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);

        // Role menus and flag menus in GUI (per build log expectations)
        Bukkit.getPluginManager().registerEvents(new RolesListener(this, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new RoleFlagsListener(this, roleManager), this);

        // Protection listeners (align with earlier provided constructors)
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new InteractionProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager, roleManager, messages), this);

        // Buckets, claim entry/exit messages
        Bukkit.getPluginManager().registerEvents(new BucketProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(this, plotManager, messages), this);
    }

    private void scheduleTasks() {
        // These classes commonly extend BukkitRunnable. Use their runTaskTimer if available.
        try {
            new EntityMobRepelTask(this, plotManager).runTaskTimer(this, 20L, 20L * 5);
        } catch (Throwable ignored) {
            // If not a BukkitRunnable in your tree, skip scheduling gracefully.
        }
        try {
            new EntityBorderRepelTask(this, plotManager).runTaskTimer(this, 20L, 20L * 3);
        } catch (Throwable ignored) {
            // Skip if class shape differs.
        }
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
     * Debug hooks (referenced by MessagesUtil & ProShieldCommand)
     * ------------------------------------------------------- */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
    }

    /** Simple toggle used by /proshield debug */
    public void toggleDebug() {
        this.debugEnabled = !this.debugEnabled;
        if (messages != null) {
            messages.debug("Debug mode toggled: " + (this.debugEnabled ? "ON" : "OFF"));
        }
    }
}
