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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public final class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;
    private GUIManager guiManager;

    // Keep these so we can reload their cached config without re-registering
    private BlockProtectionListener protectionListener;
    private GUIListener guiListener;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlotManager() { return plotManager; }
    public GUIManager getGuiManager() { return guiManager; }

    @Override
    public void onEnable() {
        instance = this;

        // Ensure config exists and claims section present
        saveDefaultConfig();
        if (!getConfig().isConfigurationSection("claims")) {
            getConfig().createSection("claims");
            saveConfig();
        }

        // Managers
        plotManager = new PlotManager(this);
        guiManager = new GUIManager(this);

        // Listeners
        protectionListener = new BlockProtectionListener(plotManager);
        guiListener = new GUIListener(plotManager, guiManager);
        Bukkit.getPluginManager().registerEvents(protectionListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Commands
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));
        } else {
            getLogger().warning("Command 'proshield' not found in plugin.yml!");
        }

        // Items / recipes
        registerCompassRecipe();

        // Expiry pass + schedule
        maybeRunExpiryNow();
        scheduleDailyExpiry();

        getLogger().info("ProShield 1.1.9 enabled. Claims loaded: " + plotManager.getClaimCount());
    }

    @Override
    public void onDisable() {
        plotManager.saveAll();
        saveConfig();
        getLogger().info("ProShield disabled.");
    }

    /** Called by /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        // Re-cache protection settings (per-world + global)
        protectionListener.reloadProtectionConfig();
        // Reload internal plot cache from config
        plotManager.reloadFromConfig();
        getLogger().info("ProShield configuration reloaded.");
    }

    private void registerCompassRecipe() {
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

    private void maybeRunExpiryNow() {
        if (getConfig().getBoolean("expiry.enabled", false)) {
            int days = getConfig().getInt("expiry.days", 30);
            int removed = plotManager.cleanupExpiredClaims(days);
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
            int removed = plotManager.cleanupExpiredClaims(days);
            if (removed > 0) {
                getLogger().info("Daily expiry: removed " + removed + " expired claim(s).");
                plotManager.saveAll();
            }
        }, oneDayTicks, oneDayTicks);
    }
}
// /proshield settings adminUnlimited <on|off|toggle>
if (sub.equals("settings")) {
    if (args.length >= 2 && args[1].equalsIgnoreCase("adminUnlimited")) {

        // Only CONSOLE or a player with proshield.owner may toggle this
        boolean allowed = !(sender instanceof org.bukkit.entity.Player)
                || sender.hasPermission("proshield.owner");

        if (!allowed) {
            sender.sendMessage(prefix() + ChatColor.RED + "Only the server owner (or console) can change this.");
            return true;
        }

        boolean current = plugin.getConfig().getBoolean("permissions.admin-includes-unlimited", false);

        if (args.length == 2 || args[2].equalsIgnoreCase("toggle")) {
            current = !current;
        } else if (args[2].equalsIgnoreCase("on") || args[2].equalsIgnoreCase("true")) {
            current = true;
        } else if (args[2].equalsIgnoreCase("off") || args[2].equalsIgnoreCase("false")) {
            current = false;
        } else {
            sender.sendMessage(prefix() + ChatColor.YELLOW +
                    "Usage: /proshield settings adminUnlimited <on|off|toggle>");
            sender.sendMessage(prefix() + ChatColor.GRAY +
                    "Currently: " + (plugin.getConfig().getBoolean("permissions.admin-includes-unlimited", false) ? "ON" : "OFF"));
            return true;
        }

        plugin.getConfig().set("permissions.admin-includes-unlimited", current);
        plugin.saveConfig();
        sender.sendMessage(prefix() + ChatColor.GREEN +
                "Admin-includes-unlimited is now " + (current ? "ON" : "OFF") + ".");
        plugin.getLogger().info("[ProShield] permissions.admin-includes-unlimited set to " + current + " by " + sender.getName());
        return true;
    }

    // If /proshield settings but not recognized
    sender.sendMessage(prefix() + ChatColor.YELLOW +
            "Usage: /proshield settings adminUnlimited <on|off|toggle>");
    return true;
}
