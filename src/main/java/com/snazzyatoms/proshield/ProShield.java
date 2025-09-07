package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
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

    // New 1.2 systems
    private ClaimRolesManager rolesManager;
    private ClaimExpansionHandler expansionHandler;

    // listeners (kept as fields for reloads)
    private BlockProtectionListener protectionListener;
    private PvpProtectionListener pvpListener;
    private GUIListener guiListener;
    private PlayerJoinListener joinListener;
    private ClaimMessageListener claimMessageListener;

    public static ProShield getInstance() { return instance; }

    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public ClaimRolesManager getRolesManager() { return rolesManager; }
    public ClaimExpansionHandler getExpansionHandler() { return expansionHandler; }

    @Override
    public void onEnable() {
        instance = this;

        // Ensure config + base sections
        saveDefaultConfig();
        if (!getConfig().isConfigurationSection("claims")) getConfig().createSection("claims");
        if (!getConfig().isConfigurationSection("roles"))  getConfig().createSection("roles");
        if (!getConfig().isConfigurationSection("homes"))  getConfig().createSection("homes");
        if (!getConfig().isConfigurationSection("messages-per-claim")) getConfig().createSection("messages-per-claim");
        saveConfig();

        // Managers
        plotManager      = new PlotManager(this);
        rolesManager     = new ClaimRolesManager(this, plotManager);
        expansionHandler = new ClaimExpansionHandler(this, plotManager);
        guiManager       = new GUIManager(this);

        // Listeners
        protectionListener   = new BlockProtectionListener(plotManager);
        pvpListener          = new PvpProtectionListener(plotManager);
        guiListener          = new GUIListener(plotManager, guiManager);
        joinListener         = new PlayerJoinListener(this);
        claimMessageListener = new ClaimMessageListener(plotManager);

        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(joinListener, this);
        Bukkit.getPluginManager().registerEvents(claimMessageListener, this);

        // Commands (your existing command class keeps working)
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

        // Custom recipe for Admin Compass
        registerCompassRecipe();

        // Expiry maintenance
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield 1.2.0 enabled. Claims loaded: " + plotManager.getClaimCount());
    }

    @Override
    public void onDisable() {
        plotManager.saveAll();
        rolesManager.saveAll();
        saveConfig();
        getLogger().info("ProShield disabled.");
    }

    /** Invoked by /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        protectionListener.reloadProtectionConfig();
        pvpListener.reloadPvpFlag();
        rolesManager.reload();
        plotManager.reloadFromConfig();
        claimMessageListener.reload();
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
            int removed = plotManager.cleanupExpiredClaims(days, getConfig().getBoolean("expiry.review-only", true));
            if (removed > 0) {
                getLogger().info("Expiry: flagged/removed " + removed + " expired claim(s).");
                plotManager.saveAll();
            }
        }
    }

    private void scheduleDailyExpiry() {
        if (!getConfig().getBoolean("expiry.enabled", false)) return;
        long oneDayTicks = TimeUnit.DAYS.toSeconds(1) * 20L;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            int days = getConfig().getInt("expiry.days", 30);
            int removed = plotManager.cleanupExpiredClaims(days, getConfig().getBoolean("expiry.review-only", true));
            if (removed > 0) {
                getLogger().info("Daily expiry: flagged/removed " + removed + " expired claim(s).");
                plotManager.saveAll();
            }
        }, oneDayTicks, oneDayTicks);
    }
}
