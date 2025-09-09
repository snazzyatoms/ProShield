// path: src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlayerJoinListener;
import com.snazzyatoms.proshield.plots.PvpProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public final class ProShield extends JavaPlugin {

    /* ==== Singleton & managers ==== */
    private static ProShield instance;
    private PlotManager plotManager;
    private GUIManager guiManager;

    /* ==== Listeners we keep references to for reload ==== */
    private BlockProtectionListener protectionListener;
    private PvpProtectionListener pvpListener;
    private GUIListener guiListener;

    /* ==== Runtime flags ==== */
    private volatile boolean debugEnabled = false;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }

    public boolean isDebug() { return debugEnabled; }
    public void setDebug(boolean v) {
        debugEnabled = v;
        getLogger().info("[Debug] setDebug=" + v);
    }

    @Override
    public void onEnable() {
        instance = this;

        // Ensure baseline config exists and stamp version
        saveDefaultConfig();
        ensureBaseSections();
        ensureConfigVersion("1.2.4", /*saveNow*/ true);

        // Managers
        plotManager = new PlotManager(this);
        guiManager  = new GUIManager(this, plotManager);

        // Listeners (pass plotManager, not 'this')
        protectionListener = new BlockProtectionListener(plotManager);
        pvpListener        = new PvpProtectionListener(plotManager);
        guiListener        = new GUIListener(plotManager, guiManager);

        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Commands
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));
        } else {
            getLogger().warning("Command 'proshield' not found in plugin.yml!");
        }

        // Custom recipe (admin compass)
        registerCompassRecipe();

        // Expiry maintenance
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield " + getDescription().getVersion() +
                " enabled. Claims loaded: " + plotManager.getClaimCount());
    }

    @Override
    public void onDisable() {
        try {
            if (plotManager != null) plotManager.saveAll();
            saveConfig();
        } catch (Exception ex) {
            getLogger().warning("Error during shutdown save: " + ex.getMessage());
        }
        getLogger().info("ProShield disabled.");
    }

    /** Called by /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        ensureBaseSections();
        ensureConfigVersion("1.2.4", /*saveNow*/ false);

        if (protectionListener != null) protectionListener.reloadProtectionConfig();
        if (pvpListener != null)         pvpListener.reloadPvpFlag();
        if (plotManager != null)         plotManager.reloadFromConfig();

        // Persist version stamp after reload tweaks
        saveConfigSafely();
        getLogger().info("Configs reloaded.");
    }

    /* ================= Helpers ================= */

    private void ensureBaseSections() {
        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
        }

        // Add tiny migration for keep-items block (safely no-op if present)
        if (!getConfig().isConfigurationSection("claims.settings")) {
            getConfig().createSection("claims.settings");
        }
        String keepPath = "claims.settings.keep-items";
        if (!getConfig().isSet(keepPath)) {
            // default OFF for server-owner control
            getConfig().set(keepPath + ".enabled", false);
            getConfig().set(keepPath + ".seconds", 900); // capped default within 300–900
        }
    }

    /** Stamp plugin-version in config so server owners see what's running on disk */
    private void ensureConfigVersion(String version, boolean saveNow) {
        getConfig().set("plugin-version", version);
        if (saveNow) saveConfigSafely();
    }

    private void saveConfigSafely() {
        try {
            saveConfig();
        } catch (Exception e) {
            getLogger().warning("Failed to save config: " + e.getMessage());
        }
    }

    private void registerCompassRecipe() {
        ItemStack compass = GUIManager.createAdminCompass();
        NamespacedKey key = new NamespacedKey(this, "proshield_admin_compass");
        ShapedRecipe recipe = new ShapedRecipe(key, compass);
        recipe.shape("IRI", "RCR", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.COMPASS);
        // Avoid duplicates on reload
        Bukkit.removeRecipe(key);
        Bukkit.addRecipe(recipe);
    }

    private void maybeRunExpiryNow() {
        if (getConfig().getBoolean("expiry.enabled", false)) {
            int days = getConfig().getInt("expiry.days", 30);
            // commit=true does actual cleanup (not a dry-run)
            int removed = plotManager.cleanupExpiredClaims(days, true);
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
            int removed = plotManager.cleanupExpiredClaims(days, true);
            if (removed > 0) {
                getLogger().info("Daily expiry: removed " + removed + " expired claim(s).");
                plotManager.saveAll();
            }
        }, oneDayTicks, oneDayTicks);
    }
}
