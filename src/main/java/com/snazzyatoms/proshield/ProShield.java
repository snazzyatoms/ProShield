package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import com.snazzyatoms.proshield.util.ClaimPreviewTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plots;
    private ClaimRoleManager roles;
    private GUIManager gui;

    public static ProShield getInstance() { return instance; }
    public PlotManager getPlots() { return plots; }
    public ClaimRoleManager getRoles() { return roles; }
    public GUIManager getGui() { return gui; }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        ensureConfigVersion();

        this.plots = new PlotManager(this);
        this.roles = new ClaimRoleManager(this); // constructor expects plugin
        this.gui   = new GUIManager(this, plots);

        // Commands
        var cmd = new ProShieldCommand(this, plots, gui);
        getCommand("proshield").setExecutor(cmd);
        getCommand("proshield").setTabCompleter(cmd);

        // Listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(plots, gui), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plots), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plots), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plots), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plots), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageProtectionListener(plots), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageProtectionListener(this, plots), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plots, roles), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, plots, gui), this);
        Bukkit.getPluginManager().registerEvents(new SpawnGuardListener(this, plots), this);

        // Admin/Player compasses craft
        gui.registerCompassRecipe();

        // Border repel task
        EntityMobRepelTask.startIfEnabled(this, plots);

        getLogger().info("ProShield enabled " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        ClaimPreviewTask.stopAll();
        getLogger().info("ProShield disabled.");
    }

    public void reloadAllConfigs() {
        reloadConfig();
        ensureConfigVersion();
        plots.reloadFromConfig();
        gui.onConfigReload();
        EntityMobRepelTask.restartIfNeeded(this, plots);
    }

    private void ensureConfigVersion() {
        FileConfiguration cfg = getConfig();
        String v = cfg.getString("version", "1.0.0");
        if (!"1.2.4".equals(v)) {
            cfg.set("version", "1.2.4");
            // ensure keep-items exists
            if (!cfg.isConfigurationSection("claims.keep-items")) {
                cfg.set("claims.keep-items.enabled", false);
                cfg.set("claims.keep-items.despawn-seconds", 900);
            }
            // ensure spawn guard exists
            if (!cfg.isConfigurationSection("spawn")) {
                cfg.set("spawn.block-claiming", true);
                cfg.set("spawn.radius", 32);
            }
            saveConfig();
        }
    }
}
