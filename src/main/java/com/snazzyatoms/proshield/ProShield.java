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

    private static ProShield instance;

    // Managers
    private PlotManager plotManager;
    private GUIManager guiManager;

    // Listeners kept as fields so we can reload their cached config safely
    private BlockProtectionListener protectionListener;
    private PvpProtectionListener pvpListener;
    private GUIListener guiListener;

    // Optional debug toggle (some commands may read this)
    private boolean debug = false;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    @Override
    public void onEnable() {
        instance = this;

        // Ensure base config exists
        saveDefaultConfig();

        // Minimum structure: ensure claims section present
        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
        }

        // --- Tiny migration bits (safe no-ops if already present) ---
        // Add keep-items section if missing (defaults OFF)
        if (!getConfig().isConfigurationSection("claims.keep-items")) {
            getConfig().set("claims.keep-items.enabled", false);
            getConfig().set("claims.keep-items.despawn-seconds", 900); // 300–900 allowed
        }
        // Make sure protection defaults exist (conservative safe defaults)
        if (!getConfig().isSet("protection.pvp-in-claims")) {
            getConfig().set("protection.pvp-in-claims", false);
        }
        if (!getConfig().isSet("protection.pvp.allow-trusted")) {
            getConfig().set("protection.pvp.allow-trusted", false);
        }
        saveConfig();
        // ------------------------------------------------------------

        // Managers
        plotManager = new PlotManager(this);
        guiManager  = new GUIManager(this, plotManager);

        // Listeners (NOTE: pass plotManager where required)
        protectionListener = new BlockProtectionListener(plotManager);
        pvpListener        = new PvpProtectionListener(plotManager);
        guiListener        = new GUIListener(plotManager, guiManager);

        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Command
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));
        } else {
            getLogger().warning("Command 'proshield' not found in plugin.yml!");
        }

        // Custom recipe for the Admin Compass
        registerCompassRecipe();

        // Expiry maintenance
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield " + getDescription().getVersion() +
                " enabled. Claims loaded: " + plotManager.getClaimCount());
    }

    @Override
    public void onDisable() {
        // Persist claims + config
        plotManager.saveAll();
        saveConfig();
        getLogger().info("ProShield disabled.");
    }

    /** Called by /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        // Reload listeners’ cached flags
        if (protectionListener != null) protectionListener.reloadProtectionConfig();
        if (pvpListener != null)         pvpListener.reloadPvpFlag();
        // Reload claim store view from config
        if (plotManager != null)         plotManager.reloadFromConfig();
    }

    /* ================= Helpers ================= */

    private void registerCompassRecipe() {
        ItemStack compass = GUIManager.createAdminCompass();
        NamespacedKey key = new NamespacedKey(this, "proshield_admin_compass");
        ShapedRecipe recipe = new ShapedRecipe(key, compass);
        recipe.shape("IRI", "RCR", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.COMPASS);

        // Avoid duplicates across reloads
        Bukkit.removeRecipe(key);
        Bukkit.addRecipe(recipe);
    }

    private void maybeRunExpiryNow() {
        if (getConfig().getBoolean("expiry.enabled", false)) {
            int days = getConfig().getInt("expiry.days", 30);
            // Preview=false (i.e., commit removals)
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
}
