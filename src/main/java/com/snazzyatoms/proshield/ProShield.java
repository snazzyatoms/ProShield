// path: src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    // Singleton (kept for compatibility with any leftover calls)
    private static ProShield INSTANCE;

    // Managers
    private PlotManager plots;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;

    // Flags
    private boolean debug;

    public static ProShield getInstance() { return INSTANCE; }

    public PlotManager getPlots() { return plots; }
    public ClaimRoleManager getRoleManager() { return roleManager; }
    public GUIManager getGui() { return guiManager; }

    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) {
        this.debug = debug;
        getConfig().set("proshield.debug", debug);
        saveConfig();
        getLogger().info("[ProShield] Debug " + (debug ? "enabled" : "disabled"));
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Load config (create defaults if missing)
        saveDefaultConfig();
        migrateAndNormalizeConfigVersion();

        // Load flags
        this.debug = getConfig().getBoolean("proshield.debug", false);

        // Init managers
        this.plots = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.plots.setRoleManager(roleManager);

        // GUI manager
        this.guiManager = new GUIManager(this, plots);

        // Listeners (ensure constructor signatures match your current classes)
        Bukkit.getPluginManager().registerEvents(new GUIListener(plots, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, plots, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plots), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plots), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageProtectionListener(plots), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plots), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plots), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plots, roleManager), this);
        // If you still keep a separate PlayerDamageProtectionListener, register it here similarly.

        // Command (3-arg: plugin, plots, gui)
        var exec = new ProShieldCommand(this, plots, guiManager);
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(exec);
            getCommand("proshield").setTabCompleter(exec);
        } else {
            getLogger().warning("[ProShield] Could not find command 'proshield' in plugin.yml!");
        }

        // Optional: craft recipe for getting a compass (player-side convenience)
        registerCompassRecipe();

        getLogger().info(color("&b[ProShield] Enabled v" + getDescription().getVersion() + " &7(with config " +
                getConfig().getString("version", "unknown") + ")"));
    }

    @Override
    public void onDisable() {
        try {
            if (plots != null) plots.saveAll();
        } catch (Exception e) {
            getLogger().warning("[ProShield] Failed saving claims on shutdown: " + e.getMessage());
        }
        getLogger().info("[ProShield] Disabled.");
    }

    /** Exposed for /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        migrateAndNormalizeConfigVersion();
        // Re-apply runtime flags
        this.debug = getConfig().getBoolean("proshield.debug", false);
        // If you cache any toggles elsewhere, refresh them here.
        registerCompassRecipe(); // harmless to re-register (overwrites by key)
        if (debug) getLogger().info("[ProShield] Config reloaded.");
    }

    /* -----------------------------------------------------------
     * Internal helpers
     * ----------------------------------------------------------- */

    private void migrateAndNormalizeConfigVersion() {
        // Ensure config has correct, current version string
        String current = getConfig().getString("version", "1.2.4");
        if (!"1.2.4".equals(current)) {
            getConfig().set("version", "1.2.4");
        }

        // Ensure a readable prefix exists
        if (!getConfig().isSet("messages.prefix")) {
            getConfig().set("messages.prefix", "&3[ProShield]&r ");
        }

        // Ensure baseline defaults for protections exist; they are safe to set if absent.
        // (Do not override admin’s choices if already present.)
        setDefaultIfMissing("protection.damage.enabled", true);
        setDefaultIfMissing("protection.damage.protect-owner-and-trusted", true);
        setDefaultIfMissing("protection.damage.cancel-all", true);

        setDefaultIfMissing("protection.pvp-in-claims", false);
        setDefaultIfMissing("protection.mob-grief", true);
        setDefaultIfMissing("protection.creeper-explosions", true);
        setDefaultIfMissing("protection.tnt-explosions", true);
        setDefaultIfMissing("protection.wither-explosions", true);

        // Entities & interactions
        setDefaultIfMissing("proshield.entities.item-frames", true);
        setDefaultIfMissing("proshield.entities.armor-stands", true);
        setDefaultIfMissing("proshield.entities.passive-animals", true);
        setDefaultIfMissing("proshield.entities.tamed-pets", true);

        // Keep items in claims
        setDefaultIfMissing("claims.keep-items.enabled", false);
        setDefaultIfMissing("claims.keep-items.despawn-seconds", 900);

        // Compass autogive/fallback
        setDefaultIfMissing("autogive.compass-on-join", true);
        setDefaultIfMissing("compass.drop-if-full", true);

        // Prevent claiming spawn (server-defined radius)
        setDefaultIfMissing("spawn-claim-protect.enabled", true);
        setDefaultIfMissing("spawn-claim-protect.world", "world");
        setDefaultIfMissing("spawn-claim-protect.radius-blocks", 32);

        saveConfig();
    }

    private void setDefaultIfMissing(String path, Object value) {
        if (!getConfig().isSet(path)) {
            getConfig().set(path, value);
        }
    }

    private void registerCompassRecipe() {
        try {
            // Simple shaped recipe: compass in center + iron + redstone
            NamespacedKey key = new NamespacedKey(this, "proshield_compass");
            ShapedRecipe recipe = new ShapedRecipe(key, makeRecipeCompass());
            recipe.shape("IRI", "RCR", "IRI");
            recipe.setIngredient('I', Material.IRON_INGOT);
            recipe.setIngredient('R', Material.REDSTONE);
            recipe.setIngredient('C', Material.COMPASS); // allows upgrading a normal compass
            // Register (replace if exists)
            Bukkit.removeRecipe(key);
            Bukkit.addRecipe(recipe);
            if (debug) getLogger().info("[ProShield] Registered compass recipe.");
        } catch (Throwable t) {
            // Some servers restrict recipe registration; ignore gracefully
            if (debug) getLogger().warning("[ProShield] Could not register compass recipe: " + t.getMessage());
        }
    }

    private ItemStack makeRecipeCompass() {
        ItemStack it = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        it.setItemMeta(meta);
        return it;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
