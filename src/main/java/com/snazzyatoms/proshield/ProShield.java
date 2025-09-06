package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.BorderVisualizer;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;
    private GUIManager guiManager;
    private BorderVisualizer borderVisualizer;

    // Admin bypass memory
    private final Set<UUID> bypass = new HashSet<>();

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public BorderVisualizer getBorderVisualizer() { return borderVisualizer; }

    public boolean isBypassing(UUID uuid) { return bypass.contains(uuid); }
    public void toggleBypass(UUID uuid) {
        if (bypass.contains(uuid)) bypass.remove(uuid); else bypass.add(uuid);
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
            saveConfig();
        }

        plotManager = new PlotManager(this);
        guiManager = new GUIManager(this);
        borderVisualizer = new BorderVisualizer(this);

        // Commands
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

        // Listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        registerCompassRecipe();

        getLogger().info("ProShield 1.1.8 enabled. Claims loaded: " + plotManager.getClaimCount());
    }

    @Override
    public void onDisable() {
        plotManager.saveAll();
        saveConfig();
        getLogger().info("ProShield disabled.");
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
