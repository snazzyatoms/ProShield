package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.PlayerDamageProtectionListener;
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

    // keep references for reload
    private BlockProtectionListener protectionListener;
    private PvpProtectionListener pvpListener;
    private PlayerDamageProtectionListener damageListener;
    private GUIListener guiListener;

    private boolean debug = false;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }

    // Debug helpers (referenced by GUI/commands)
    public boolean isDebug() { return debug; }
    public void setDebug(boolean v) { this.debug = v; }

    @Override
    public void onEnable() {
        instance = this;

        // Ensure config exists & stamp version
        saveDefaultConfig();
        getConfig().set("version", "1.2.4");
        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
        }

        // Tiny migration: ensure protection.damage block exists with safe defaults
        if (!getConfig().isConfigurationSection("protection.damage")) {
            getLogger().info("Adding missing protection.damage defaults...");
            getConfig().set("protection.damage.enabled", true);
            getConfig().set("protection.damage.protect-owner-and-trusted", true);
            getConfig().set("protection.damage.cancel-all", true);
            // selective toggles (used when cancel-all=false)
            getConfig().set("protection.damage.pve", true);
            getConfig().set("protection.damage.projectiles", true);
            getConfig().set("protection.damage.environment", true);
            getConfig().set("protection.damage.fire-lava", true);
            getConfig().set("protection.damage.fall", true);
            getConfig().set("protection.damage.explosions", true);
            getConfig().set("protection.damage.drown-void-suffocate", true);
            getConfig().set("protection.damage.poison-wither", true);
        }
        saveConfig();

        // Managers
        plotManager = new PlotManager(this);
        guiManager  = new GUIManager(this, plotManager);

        // Listeners — pass plotManager
        protectionListener = new BlockProtectionListener(plotManager);
        pvpListener        = new PvpProtectionListener(plotManager);
        damageListener     = new PlayerDamageProtectionListener(plotManager, this);
        guiListener        = new GUIListener(plotManager, guiManager);

        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(damageListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Commands
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

        // Custom recipe
        registerCompassRecipe();

        // Expiry maintenance
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield 1.2.4 enabled. Claims loaded: " + plotManager.getClaimCount());
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
        getConfig().set("version", "1.2.4");
        if (protectionListener != null) protectionListener.reloadProtectionConfig();
        if (pvpListener != null)         pvpListener.reloadPvpFlag();
        if (plotManager != null)         plotManager.reloadFromConfig();
    }

    private void registerCompassRecipe() {
        ItemStack compass = GUIManager.createAdminCompass();
        NamespacedKey key = new NamespacedKey(this, "proshield_admin_compass");
        ShapedRecipe recipe = new ShapedRecipe(key, compass);
        recipe.shape("IRI", "RCR", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.COMPASS);
        Bukkit.removeRecipe(key); // avoid duplicates on reload
        Bukkit.addRecipe(recipe);
    }

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
}
