// path: src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.KeepDropsListener;
import com.snazzyatoms.proshield.plots.PlayerJoinListener;
import com.snazzyatoms.proshield.plots.PlotManager;
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

    // listeners we keep around for reloads
    private BlockProtectionListener protectionListener;
    private PvpProtectionListener pvpListener;
    private GUIListener guiListener;
    private KeepDropsListener keepDropsListener;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }

    @Override
    public void onEnable() {
        instance = this;

        // Create config & ensure required sections + perform tiny migrations
        saveDefaultConfig();
        migrateConfig();

        // Managers
        plotManager = new PlotManager(this);
        guiManager  = new GUIManager(this, plotManager); // ✅ pass plugin + plotManager

        // Listeners
        protectionListener = new BlockProtectionListener(plotManager);
        pvpListener        = new PvpProtectionListener(plotManager);
        guiListener        = new GUIListener(plotManager, guiManager);
        keepDropsListener  = new KeepDropsListener(plotManager);

        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(keepDropsListener, this);

        // Command
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

        // Recipe
        registerCompassRecipe();

        // Expiry maintenance
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield 1.2.2 enabled. Claims loaded: " + plotManager.getClaimCount());
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
        if (protectionListener != null) protectionListener.reloadProtectionConfig();
        if (pvpListener != null)         pvpListener.reloadPvpFlag();
        if (plotManager != null)         plotManager.reloadFromConfig();
        if (guiManager != null)          guiManager.reloadSlotsFromConfig();
        if (keepDropsListener != null)   keepDropsListener.reloadConfigWindow();
    }

    private void migrateConfig() {
        // Ensure claims section
        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
        }
        // Ensure keep-items structure
        String base = "claims.keep-items";
        if (!getConfig().isSet(base + ".enabled")) {
            getConfig().set(base + ".enabled", false);
        }
        if (!getConfig().isSet(base + ".retention-seconds")) {
            getConfig().set(base + ".retention-seconds", 600); // default 10min, allowed 300..900
        }
        // Ensure GUI slot keys
        if (!getConfig().isConfigurationSection("gui.slots")) {
            getConfig().set("gui.slots.claim", 11);
            getConfig().set("gui.slots.info", 13);
            getConfig().set("gui.slots.unclaim", 15);
            getConfig().set("gui.slots.help", 31);
            getConfig().set("gui.slots.admin", 33);
        }
        // Tag config version
        getConfig().set("config-version", "1.2.2");
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
