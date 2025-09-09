// path: src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.DamageProtectionListener;
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

    // Listeners (kept as fields so we can hot-reload config)
    private BlockProtectionListener protectionListener;
    private PvpProtectionListener pvpListener;
    private DamageProtectionListener damageListener;
    private GUIListener guiListener;

    // Optional debug toggle
    private boolean debug = false;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Ensure base sections & safe defaults for brand-new installs
        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
        }
        // Keep-items defaults (off by default)
        if (!getConfig().isConfigurationSection("claims.keep-items")) {
            getConfig().set("claims.keep-items.enabled", false);
            getConfig().set("claims.keep-items.despawn-seconds", 900); // 300–900
        }

        // Damage protection defaults (ON by default)
        if (!getConfig().isConfigurationSection("protection.damage")) {
            getConfig().set("protection.damage.enabled", true);
            getConfig().set("protection.damage.protect-owner-and-trusted", true);
            getConfig().set("protection.damage.cancel-all", true);
            getConfig().set("protection.damage.pve", true);
            getConfig().set("protection.damage.projectiles", true);
            getConfig().set("protection.damage.environment", true);
            getConfig().set("protection.damage.fire-lava", true);
            getConfig().set("protection.damage.fall", true);
            getConfig().set("protection.damage.explosions", true);
            getConfig().set("protection.damage.drown-void-suffocate", true);
            getConfig().set("protection.damage.poison-wither", true);
        }

        // Existing global protection defaults (safe “fully protected” stance)
        getConfig().addDefault("protection.pvp-in-claims", false);
        getConfig().addDefault("protection.mob-grief", true);           // true = block explosions in claims
        getConfig().addDefault("protection.creeper-explosions", true);
        getConfig().addDefault("protection.tnt-explosions", true);
        getConfig().addDefault("protection.wither-explosions", true);
        getConfig().addDefault("protection.wither-skull-explosions", true);
        getConfig().addDefault("protection.ender-crystal-explosions", true);
        getConfig().addDefault("protection.ender-dragon-explosions", true);
        getConfig().addDefault("protection.fire.spread", true);
        getConfig().addDefault("protection.fire.burn", true);
        getConfig().addDefault("protection.fire.ignite.flint_and_steel", true);
        getConfig().addDefault("protection.fire.ignite.lava", true);
        getConfig().addDefault("protection.fire.ignite.lightning", true);
        getConfig().addDefault("protection.fire.ignite.explosion", true);
        getConfig().addDefault("protection.fire.ignite.spread", true);
        // Save any added defaults without overwriting user edits
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Managers
        plotManager = new PlotManager(this);
        guiManager  = new GUIManager(this, plotManager);

        // Listeners
        protectionListener = new BlockProtectionListener(plotManager);
        pvpListener        = new PvpProtectionListener(plotManager);
        damageListener     = new DamageProtectionListener(plotManager);
        guiListener        = new GUIListener(plotManager, guiManager);

        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(damageListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Command
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));
        } else {
            getLogger().warning("Command 'proshield' not found in plugin.yml!");
        }

        // Compass recipe
        registerCompassRecipe();

        // Expiry maintenance
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield " + getDescription().getVersion() +
                " enabled. Claims loaded: " + plotManager.getClaimCount());
    }

    @Override
    public void onDisable() {
        plotManager.saveAll();
        saveConfig();
        getLogger().info("ProShield disabled.");
    }

    /** /proshield reload path */
    public void reloadAllConfigs() {
        reloadConfig();
        if (protectionListener != null) protectionListener.reloadProtectionConfig();
        if (pvpListener != null)         pvpListener.reloadPvpFlag();
        if (damageListener != null)      damageListener.reloadDamageConfig();
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
        Bukkit.removeRecipe(key);
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
