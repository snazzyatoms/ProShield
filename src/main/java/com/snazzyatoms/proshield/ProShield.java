package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.EntityDamageProtectionListener;
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
    private PlotManager plotManager;
    private GUIManager guiManager;

    // listeners we keep so we can reload their config
    private BlockProtectionListener protectionListener;
    private PvpProtectionListener pvpListener;
    private EntityDamageProtectionListener damageListener;
    private GUIListener guiListener;

    // simple cached version string for config header stamping
    private String runningVersion;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }

    @Override
    public void onEnable() {
        instance = this;
        runningVersion = getDescription().getVersion(); // "1.2.4"

        saveDefaultConfig();
        ensureBaseSections();
        ensureConfigVersionHeader();

        plotManager = new PlotManager(this);
        guiManager  = new GUIManager(this, plotManager);

        protectionListener = new BlockProtectionListener(plotManager);
        pvpListener        = new PvpProtectionListener(plotManager);
        damageListener     = new EntityDamageProtectionListener(plotManager);
        guiListener        = new GUIListener(plotManager, guiManager);

        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(damageListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

        registerCompassRecipe();
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield " + runningVersion + " enabled. Claims loaded: " + plotManager.getClaimCount());
    }

    @Override
    public void onDisable() {
        plotManager.saveAll();
        saveConfig();
        getLogger().info("ProShield disabled.");
    }

    /** /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        ensureConfigVersionHeader(); // keep the header stamped to current version
        if (protectionListener != null) protectionListener.reloadProtectionConfig();
        if (pvpListener != null)         pvpListener.reloadPvpFlag();
        if (damageListener != null)      damageListener.reloadConfig();
        if (plotManager != null)         plotManager.reloadFromConfig();
    }

    private void ensureBaseSections() {
        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
        }
        // make sure new player protection section exists with safe defaults
        if (!getConfig().isConfigurationSection("protection.player")) {
            getConfig().set("protection.player.enabled", true);
            getConfig().set("protection.player.damage.owner-immune", true);
            getConfig().set("protection.player.damage.trusted-immune", true);
            getConfig().set("protection.player.damage.mobs", true);
            getConfig().set("protection.player.damage.projectiles", true);
            getConfig().set("protection.player.damage.explosions", true);
            getConfig().set("protection.player.damage.fire-lava", true);
            getConfig().set("protection.player.damage.fall", true);
            getConfig().set("protection.player.damage.other-environment", true);
        }
        saveConfig();
    }

    /** Puts a visible header with the running plugin version into the config (non-breaking). */
    private void ensureConfigVersionHeader() {
        // Stamp a simple string; safe to overwrite or create each boot/reload
        getConfig().set("meta.config-version", runningVersion);
        saveConfig();
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
