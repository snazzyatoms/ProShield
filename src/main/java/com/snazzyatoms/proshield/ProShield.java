package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.ItemProtectionListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlayerJoinListener;
import com.snazzyatoms.proshield.plots.PvpProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;
    private GUIManager guiManager;

    private BlockProtectionListener protectionListener;
    private PvpProtectionListener pvpListener;
    private GUIListener guiListener;
    private ItemProtectionListener itemListener;

    private boolean debug;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean on) { this.debug = on; }

    @Override
    public void onEnable() {
        instance = this;

        // Ensure config exists
        saveDefaultConfig();
        guardedConfigLoad();      // safer YAML load
        migrateIfNeeded();        // ensure config.config.version is 1.2.3

        // Managers
        plotManager = new PlotManager(this);
        guiManager  = new GUIManager(this, plotManager);

        // Listeners
        protectionListener = new BlockProtectionListener(plotManager);
        pvpListener        = new PvpProtectionListener(plotManager);
        guiListener        = new GUIListener(plotManager, guiManager);
        itemListener       = new ItemProtectionListener(plotManager);

        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(itemListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Commands
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

        // Custom recipe
        registerCompassRecipe();

        // Expiry tasks
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        debug = getConfig().getBoolean("debug.enabled", false);

        getLogger().info("ProShield 1.2.3 enabled. Claims loaded: " + plotManager.getClaimCount());
    }

    @Override
    public void onDisable() {
        plotManager.saveAll();
        saveConfig();
        getLogger().info("ProShield disabled.");
    }

    public void reloadAllConfigs() {
        if (!guardedConfigLoad()) return;
        if (protectionListener != null) protectionListener.reloadProtectionConfig();
        if (pvpListener != null)         pvpListener.reloadPvpFlag();
        if (plotManager != null)         plotManager.reloadFromConfig();
        if (guiManager != null)          guiManager.reloadIconsFromConfig();
        if (itemListener != null)        itemListener.reloadFromConfig();
        debug = getConfig().getBoolean("debug.enabled", false);
    }

    private void registerCompassRecipe() {
        ItemStack compass = GUIManager.createAdminCompass();
        NamespacedKey key = new NamespacedKey(this, "proshield_admin_compass");
        ShapedRecipe recipe = new ShapedRecipe(key, compass);
        recipe.shape("IRI", "RCR", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.COMPASS);
        Bukkit.removeRecipe(key);
        Bukkit.addRecipe(recipe);
    }

    private void maybeRunExpiryNow() {
        if (getConfig().getBoolean("expiry.enabled", false)) {
            int days = getConfig().getInt("expiry.days", 30);
            int removed = plotManager.cleanupExpiredClaims(days, true);
            if (removed > 0) {
                getLogger().info("Expiry (startup): removed " + removed + " expired claim(s).");
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

    private boolean guardedConfigLoad() {
        try {
            // reloadConfig() will read from disk; ensure file exists
            File f = new File(getDataFolder(), "config.yml");
            if (!f.exists()) saveDefaultConfig();
            reloadConfig();
            return true;
        } catch (Exception ex) {
            getLogger().severe("Failed to reload config.yml: " + ex.getMessage());
            if (ex instanceof InvalidConfigurationException || ex instanceof IOException) {
                getLogger().severe("Please validate YAML syntax.");
            }
            return false;
        }
    }

    /** Ensure config.config.version is set to 1.2.3, seed new nodes if missing. */
    private void migrateIfNeeded() {
        String ver = getConfig().getString("config.version", "0");
        if (!"1.2.3".equals(ver)) {
            getConfig().set("config.version", "1.2.3");
            // seed new nodes if not present
            if (!getConfig().isConfigurationSection("item-keep")) {
                getConfig().set("item-keep.enabled", false);
                getConfig().set("item-keep.despawn-seconds", 600);
                getConfig().set("item-keep.anti-theft", true);
            }
            if (!getConfig().isConfigurationSection("debug")) {
                getConfig().set("debug.enabled", false);
            }
            saveConfig();
        }
    }
}
