// path: src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
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

    // listeners we keep so we can reload their configs
    private BlockProtectionListener protectionListener;
    private PvpProtectionListener pvpListener;
    private GUIListener guiListener;

    // debug toggle (used by GUI + command)
    private boolean debug = false;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }

    /** Debug accessors used by GUI + command */
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) {
        this.debug = debug;
        // reflect immediately into config so it survives restarts if the server stops unexpectedly
        getConfig().set("proshield.debug", debug);
        saveConfig();
        getLogger().info("[Debug] set to: " + debug);
    }

    @Override
    public void onEnable() {
        instance = this;

        // config bootstrap
        saveDefaultConfig();
        ensureConfigScaffolding();
        ensureConfigVersion("1.2.4"); // make sure header shows the running version (non-destructive)

        // load debug toggle from config
        this.debug = getConfig().getBoolean("proshield.debug", false);

        // managers
        plotManager = new PlotManager(this);
        guiManager  = new GUIManager(this, plotManager);

        // listeners
        protectionListener = new BlockProtectionListener(plotManager);
        pvpListener        = new PvpProtectionListener(plotManager);
        guiListener        = new GUIListener(plotManager, guiManager);

        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(pvpListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // command
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

        // recipe (admin compass)
        registerCompassRecipe();

        // expiry maintenance
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield 1.2.4 enabled. Claims loaded: " + plotManager.getClaimCount() +
                " | debug=" + debug);
    }

    @Override
    public void onDisable() {
        // persist claims + current debug toggle
        plotManager.saveAll();
        getConfig().set("proshield.debug", debug);
        saveConfig();
        getLogger().info("ProShield disabled.");
    }

    /** Called by /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        // keep non-destructive version stamp aligned
        ensureConfigVersion("1.2.4");

        // re-read debug
        this.debug = getConfig().getBoolean("proshield.debug", false);

        if (protectionListener != null) protectionListener.reloadProtectionConfig();
        if (pvpListener != null)         pvpListener.reloadPvpFlag();
        if (plotManager != null)         plotManager.reloadFromConfig();

        getLogger().info("ProShield configs reloaded. debug=" + debug);
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
            int removed = plotManager.cleanupExpiredClaims(days, true); // commit
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

    /** Ensure baseline config sections exist without clobbering user settings. */
    private void ensureConfigScaffolding() {
        boolean changed = false;

        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
            changed = true;
        }
        if (!getConfig().isConfigurationSection("proshield")) {
            getConfig().createSection("proshield");
            changed = true;
        }
        if (!getConfig().contains("proshield.debug")) {
            getConfig().set("proshield.debug", false);
            changed = true;
        }
        if (changed) saveConfig();
    }

    /**
     * Non-destructive: only updates displayed version keys if present or missing.
     * Does NOT reset the whole config.
     */
    private void ensureConfigVersion(String version) {
        boolean changed = false;

        // top-level version string
        if (!getConfig().contains("version")) {
            getConfig().set("version", version);
            changed = true;
        } else {
            String v = String.valueOf(getConfig().get("version"));
            if (!version.equals(v)) {
                getConfig().set("version", version);
                changed = true;
            }
        }

        // comment-only headers can’t be changed via Bukkit API; we keep string fields in sync instead.
        if (changed) saveConfig();
    }
}
