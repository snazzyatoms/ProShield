// path: src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;
    private GUIManager guiManager;

    // admin.yml
    private File adminFile;
    private FileConfiguration adminCfg;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public FileConfiguration getAdminConfig() { return adminCfg; }

    @Override
    public void onEnable() {
        instance = this;

        // configs
        saveDefaultConfig();
        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
            saveConfig();
        }
        loadOrCreateAdminYaml();

        // managers
        plotManager = new PlotManager(this);
        guiManager = new GUIManager(this);

        // command
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

        // listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(plotManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        registerCompassRecipe();

        getLogger().info("ProShield enabled. Claims loaded: " + plotManager.getClaimCount());
    }

    @Override
    public void onDisable() {
        plotManager.saveAll();
        saveConfig();
        getLogger().info("ProShield disabled.");
    }

    public void reloadAllConfigs() {
        reloadConfig();
        loadOrCreateAdminYaml();
        plotManager.reloadFromConfig();
    }

    private void loadOrCreateAdminYaml() {
        if (adminFile == null) adminFile = new File(getDataFolder(), "admin.yml");
        if (!adminFile.exists()) {
            saveResource("admin.yml", false);
        }
        adminCfg = YamlConfiguration.loadConfiguration(adminFile);
        // ensure defaults (only first time if new keys are missing)
        boolean changed = false;
        if (!adminCfg.isSet("defaults.give-compass-on-join")) { adminCfg.set("defaults.give-compass-on-join", true); changed = true; }
        if (!adminCfg.isSet("defaults.compass-slot")) { adminCfg.set("defaults.compass-slot", 8); changed = true; }
        if (!adminCfg.isSet("admin-menu.open-when-sneaking-with-compass")) { adminCfg.set("admin-menu.open-when-sneaking-with-compass", true); changed = true; }
        if (!adminCfg.isSet("teleport.enabled")) { adminCfg.set("teleport.enabled", true); changed = true; }
        if (!adminCfg.isSet("teleport.max-list")) { adminCfg.set("teleport.max-list", 27); changed = true; }
        if (changed) {
            try { adminCfg.save(adminFile); } catch (IOException ignored) {}
        }
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
}
