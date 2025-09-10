package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProShield extends JavaPlugin {

    private static ProShield instance;

    private FileConfiguration config;
    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.config = getConfig();

        // Core managers
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.guiManager = new GUIManager(this, plotManager);

        // Register commands
        ProShieldCommand cmd = new ProShieldCommand(this, plotManager, guiManager);
        getCommand("proshield").setExecutor(cmd);
        getCommand("proshield").setTabCompleter(cmd);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new SpawnGuardListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MobBorderRepelListener(this), this);

        getLogger().info("ProShield v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        instance = null;
        getLogger().info("ProShield disabled.");
    }

    /**
     * Reload all plugin configs, plots, and GUIs.
     */
    public void reloadAllConfigs() {
        reloadConfig();
        this.config = getConfig();

        plotManager.reloadFromConfig();
        roleManager.reloadFromConfig();

        // Invalidate GUIs
        guiManager.onConfigReload();

        getLogger().info("ProShield config reloaded.");
    }

    public FileConfiguration getConfiguration() {
        return this.config;
    }

    public PlotManager getPlotManager() {
        return this.plotManager;
    }

    public ClaimRoleManager getRoleManager() {
        return this.roleManager;
    }

    public GUIManager getGuiManager() {
        return this.guiManager;
    }

    public static ProShield getInstance() {
        return instance;
    }

    /**
     * Helper for prefixing messages with plugin prefix.
     */
    public String prefixed(String msg) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                getConfiguration().getString("messages.prefix", "&3[ProShield]&r "));
        return prefix + ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Give compass to a player on join if enabled in config.
     * Handles admin vs player compass.
     */
    public void giveJoinCompass(Player p) {
        boolean auto = getConfiguration().getBoolean("autogive.compass-on-join", true);
        if (!auto) return;

        boolean preferAdmin = p.isOp() || p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui");
        guiManager.giveCompass(p, preferAdmin);
    }
}
