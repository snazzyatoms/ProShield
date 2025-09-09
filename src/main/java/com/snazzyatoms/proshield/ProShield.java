package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield INSTANCE;

    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;

    private boolean debug;

    public static ProShield getInstance() {
        return INSTANCE;
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

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        getLogger().info("[Debug] set to " + debug);
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Config bootstrap
        saveDefaultConfig();
        this.debug = getConfig().getBoolean("proshield.debug", false);

        // Core managers
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.plotManager.setRoleManager(roleManager);

        // GUI
        this.guiManager = new GUIManager(this, plotManager);

        // Listeners (register everything here)
        Bukkit.getPluginManager().registerEvents(new GUIListener(plotManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, plotManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageProtectionListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new SpawnClaimGuardListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new MobBorderRepelListener(plotManager), this);

        // Command + tab
        ProShieldCommand cmd = new ProShieldCommand(this, plotManager, guiManager);
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(cmd);
            // Avoid pattern matching issues on older compilers: cast explicitly
            getCommand("proshield").setTabCompleter((TabCompleter) cmd);
        }

        // Any scheduled tasks that need initial boot can be started here
        // (e.g., MobBorderRepelListener schedules its own repeating task in its ctor if enabled)
        getLogger().info("ProShield " + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        // Persist claims/config if needed
        if (plotManager != null) {
            plotManager.saveAll();
        }
        getLogger().info("ProShield disabled.");
    }

    /** Unified reload entry used by admin GUI and /proshield reload. */
    public void onConfigReload() {
        reloadConfig();
        this.debug = getConfig().getBoolean("proshield.debug", false);

        // Let managers refresh their caches/views of config
        if (plotManager != null) plotManager.reloadFromConfig();
        if (roleManager != null) roleManager.reloadFromConfig();
        if (guiManager != null) guiManager.onConfigReload();

        // If any listeners maintain scheduled tasks that depend on config values,
        // they should subscribe to ServerLoadEvent or expose a static refresh() call.
        Bukkit.getPluginManager().callEvent(new ProShieldReloadEvent(this));

        getLogger().info("ProShield config reloaded.");
    }

    /** Back-compat shim for older call sites that invoked this name. */
    public void reloadAllConfigs() {
        onConfigReload();
    }
}
