// path: src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;
    private GUIManager guiManager;

    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

   @Override
public void onEnable() {
    instance = this;

    // Ensure config exists
    saveDefaultConfig();
    if (!getConfig().isConfigurationSection("claims")) {
        getConfig().createSection("claims");
        saveConfig();
    }

    // Managers
    plotManager = new PlotManager(this);
    guiManager = new GUIManager(this);

    // Commands (pass plugin + plotManager)
    getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

    // Listeners (pass plotManager where needed)
Bukkit.getPluginManager().registerEvents(new GUIListener(plotManager), this);
Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);


    registerCompassRecipe();

    getLogger().info("ProShield enabled. Claims loaded: " + plotManager.getClaimCount());
}


    @Override
    public void onDisable() {
        // Persist everything
        plotManager.saveAll();
        saveConfig();
        getLogger().info("ProShield disabled.");
    }

    private void registerCompassRecipe() {
        // Cosmetic, optional recipe that crafts the admin compass (iron + redstone + compass)
        ItemStack compass = GUIManager.createAdminCompass();
        NamespacedKey key = new NamespacedKey(this, "proshield_admin_compass");
        ShapedRecipe recipe = new ShapedRecipe(key, compass);
        recipe.shape("IRI", "RCR", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.COMPASS);

        // Avoid duplicate on reload
        Bukkit.removeRecipe(key);
        Bukkit.addRecipe(recipe);
    }
}
