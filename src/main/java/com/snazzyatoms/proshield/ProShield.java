package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.PlayerDamageProtectionListener;
import com.snazzyatoms.proshield.plots.PlayerJoinListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PvpProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public final class ProShield extends JavaPlugin {

    private static ProShield instance;

    private PlotManager plotManager;
    private GUIManager guiManager;

    // listeners we want to be able to "reload"
    private BlockProtectionListener blockListener;
    private PvpProtectionListener pvpListener;
    private PlayerDamageProtectionListener damageListener;
    private GUIListener guiListener;

    // debug toggle (used by GUI/commands)
    private boolean debug = false;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }

    /** Exposed for admin toggle */
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) {
        this.debug = debug;
        getLogger().info("[Debug] set to " + debug);
    }

    @Override
    public void onEnable() {
        instance = this;

        // create/load config
        saveDefaultConfig();
        migrateConfigOnce();

        // managers
        plotManager = new PlotManager(this);
        guiManager  = new GUIManager(this, plotManager);

        // listeners
        blockListener   = new BlockProtectionListener(plotManager);
        pvpListener     = new PvpProtectionListener(plotManager);
        damageListener  = new PlayerDamageProtectionListener(this, plotManager);
        guiListener     = new GUIListener(plotManager, guiManager);

        Bukkit.getPluginManager().registerEvents(blockListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(damageListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // command
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));
        }

        // compass recipe via GUI manager (also clears dupes)
        guiManager.registerCompassRecipe();

        // expiry maintenance
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield " + getDescription().getVersion() +
                " enabled. Claims loaded: " + plotManager.getClaimCount());
    }

    @Override
    public void onDisable() {
        // persist everything
        plotManager.saveAll();
        saveConfig();
        getLogger().info("ProShield disabled.");
    }

    /** Called by /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        migrateConfigOnce();

        // listeners/managers that read config
        if (blockListener != null) blockListener.reloadProtectionConfig();
        if (pvpListener != null)   pvpListener.reloadPvpFlag();
        if (plotManager != null)   plotManager.reloadFromConfig();
        if (guiManager != null)    guiManager.onConfigReload();

        getLogger().info("ProShield configuration reloaded.");
    }

    /* ----------------------------
     * Internals
     * ---------------------------- */

    private void maybeRunExpiryNow() {
        if (getConfig().getBoolean("expiry.enabled", false)) {
            int days = getConfig().getInt("expiry.days", 30);
            int removed = plotManager.cleanupExpiredClaims(days, false);
            if (removed > 0) {
                getLogger().info("Expiry: removed " + removed + " expired claim(s).");
                plotManager.saveAll();
            }
        }
    }

    private void scheduleDailyExpiry() {
        if (!getConfig().getBoolean("expiry.enabled", false)) return;
        long oneDayTicks = TimeUnit.DAYS.toSeconds(1) * 20L;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            int days = getConfig().getInt("expiry.days", 30);
            int removed = plotManager.cleanupExpiredClaims(days, false);
            if (removed > 0) {
                getLogger().info("Daily expiry: removed " + removed + " expired claim(s).");
                plotManager.saveAll();
            }
        }, oneDayTicks, oneDayTicks);
    }

    /**
     * Tiny one-time config migration / normalization:
     * - Ensure version string matches plugin.yml version.
     * - Ensure required sections exist with safe defaults (esp. new protection.damage block).
     */
    private void migrateConfigOnce() {
        final String codeVersion = getDescription().getVersion();

        // version stamp
        String cfgVersion = getConfig().getString("version", "");
        if (!codeVersion.equals(cfgVersion)) {
            getConfig().set("version", codeVersion);
        }

        // ensure "claims" root section exists
        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
        }

        // ensure protection.damage exists (defaults: fully protect inside claims)
        String base = "protection.damage.";
        if (!getConfig().contains(base + "enabled")) {
            getConfig().set(base + "enabled", true);
        }
        if (!getConfig().contains(base + "protect-owner-and-trusted")) {
            getConfig().set(base + "protect-owner-and-trusted", true);
        }
        if (!getConfig().contains(base + "cancel-all")) {
            getConfig().set(base + "cancel-all", true);
        }

        // keep-items defaults if missing
        if (!getConfig().contains("claims.keep-items.enabled")) {
            getConfig().set("claims.keep-items.enabled", false);
        }
        if (!getConfig().contains("claims.keep-items.despawn-seconds")) {
            getConfig().set("claims.keep-items.despawn-seconds", 900);
        }

        // GUI slots sanity
        if (!getConfig().contains("gui.slots.main.help")) {
            getConfig().set("gui.slots.main.help", 49);
        }
        if (!getConfig().contains("gui.slots.main.back")) {
            getConfig().set("gui.slots.main.back", 48);
        }
        if (!getConfig().contains("gui.slots.main.admin")) {
            getConfig().set("gui.slots.main.admin", 33);
        }

        saveConfig();
    }
}
