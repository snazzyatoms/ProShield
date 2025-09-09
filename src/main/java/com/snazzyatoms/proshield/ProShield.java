package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlayerJoinListener;
import com.snazzyatoms.proshield.plots.PvpProtectionListener;
import com.snazzyatoms.proshield.plots.PlayerDamageProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public final class ProShield extends JavaPlugin {

    private static ProShield instance;

    private PlotManager plotManager;
    private GUIManager guiManager;

    // listeners
    private BlockProtectionListener protectionListener;
    private PvpProtectionListener pvpListener;
    private GUIListener guiListener;
    private PlayerDamageProtectionListener damageListener;

    public static ProShield getInstance() { return instance; }

    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        migrateConfig_ensureVersionAndSpawn();
        saveConfig();

        // managers
        plotManager = new PlotManager(this);
        guiManager  = new GUIManager(this, plotManager);

        // listeners
        protectionListener = new BlockProtectionListener(plotManager);
        pvpListener        = new PvpProtectionListener(plotManager);
        guiListener        = new GUIListener(plotManager, guiManager);
        damageListener     = new PlayerDamageProtectionListener(plotManager);

        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(damageListener, this);

        // command
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

        // recipe
        guiManager.registerCompassRecipe();

        // expiry maintenance
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield 1.2.4 enabled. Claims loaded: " + plotManager.getClaimCount());
    }

    @Override
    public void onDisable() {
        if (plotManager != null) plotManager.saveAll();
        saveConfig();
        getLogger().info("ProShield disabled.");
    }

    /** /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        migrateConfig_ensureVersionAndSpawn();
        if (protectionListener != null) protectionListener.reloadProtectionConfig();
        if (pvpListener != null)         pvpListener.reloadPvpFlag();
        if (plotManager != null)         plotManager.reloadFromConfig();
        if (guiManager != null)          guiManager.onConfigReload();
        saveConfig();
    }

    /* =========================
       Small helpers
       ========================= */

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
     * Ensure config contains required bits for 1.2.4 and correct version string.
     */
    private void migrateConfig_ensureVersionAndSpawn() {
        // Version string
        getConfig().set("version", "1.2.4");

        // Ensure spawn no-claim section exists
        if (!getConfig().isConfigurationSection("no-claim.spawn")) {
            getConfig().set("no-claim.spawn.enabled", true);
            getConfig().set("no-claim.spawn.radius-blocks", 96);
        }

        // Ensure GUI slots exist (sane defaults if absent)
        if (!getConfig().isConfigurationSection("gui.slots.main")) {
            getConfig().set("gui.slots.main.create", 11);
            getConfig().set("gui.slots.main.info", 13);
            getConfig().set("gui.slots.main.remove", 15);
            getConfig().set("gui.slots.main.admin", 33);
            getConfig().set("gui.slots.main.help", 49);
            getConfig().set("gui.slots.main.back", 48);
        }
        if (!getConfig().isConfigurationSection("gui.slots.admin")) {
            getConfig().set("gui.slots.admin.toggle-drop-if-full", 20);
            getConfig().set("gui.slots.admin.help", 22);
            getConfig().set("gui.slots.admin.back", 31);
        }

        // Ensure protection.damage defaults (protect owners/trusted in claims)
        if (!getConfig().isConfigurationSection("protection.damage")) {
            getConfig().set("protection.damage.enabled", true);
            getConfig().set("protection.damage.protect-owner-and-trusted", true);
            getConfig().set("protection.damage.cancel-all", true);
        }
    }
}
