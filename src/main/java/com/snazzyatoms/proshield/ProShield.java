// path: src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.ClaimMessageListener;
import com.snazzyatoms.proshield.plots.ClaimRoleManager;
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
    private ClaimRoleManager roleManager;

    // keep references for reload
    private BlockProtectionListener protectionListener;
    private PvpProtectionListener pvpListener;
    private GUIListener guiListener;
    private ClaimMessageListener messageListener;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public ClaimRoleManager getRoleManager() { return roleManager; }

    @Override
    public void onEnable() {
        instance = this;

        // ensure config & sections
        saveDefaultConfig();
        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
            saveConfig();
        }

        // managers
        plotManager  = new PlotManager(this);
        guiManager   = new GUIManager(this);
        roleManager  = new ClaimRoleManager(this);

        // listeners — pass correct managers
        protectionListener = new BlockProtectionListener(plotManager);
        pvpListener        = new PvpProtectionListener(plotManager);
        guiListener        = new GUIListener(plotManager, guiManager);
        messageListener    = new ClaimMessageListener(plotManager, roleManager);

        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(messageListener, this);

        // command
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

        // recipe
        registerCompassRecipe();

        // expiry maintenance
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield 1.2 enabled. Claims loaded: " + plotManager.getClaimCount());
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
