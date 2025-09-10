package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.GUICache;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;
    private GUICache guiCache;

    private MobBorderRepelListener mobBorderRepelListener;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        reloadConfig();

        // Core managers
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this);
        guiCache = new GUICache();
        guiManager = new GUIManager(this, plotManager, roleManager, guiCache);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(guiManager, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);

        // Register Mob Repel Listener
        mobBorderRepelListener = new MobBorderRepelListener(this, plotManager);
        Bukkit.getPluginManager().registerEvents(mobBorderRepelListener, this);

        // Commands
        ProShieldCommand commandExecutor = new ProShieldCommand(this, plotManager, guiManager);
        PluginCommand mainCommand = getCommand("proshield");
        if (mainCommand != null) {
            mainCommand.setExecutor(commandExecutor);
            mainCommand.setTabCompleter(commandExecutor);
        }

        getLogger().info("✅ ProShield enabled (v" + getDescription().getVersion() + ")");
    }

    @Override
    public void onDisable() {
        getLogger().info("⛔ ProShield disabled");
    }

    /**
     * Reload configs, GUIs, roles, and mob repel settings
     */
    public void reloadAllConfigs() {
        reloadConfig();
        roleManager.reloadFromConfig();
        guiManager.reloadFromConfig();
        if (mobBorderRepelListener != null) {
            mobBorderRepelListener.reloadFromConfig();
        }
        getLogger().info("♻️ ProShield config reloaded successfully");
    }

    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public GUICache getGuiCache() {
        return guiCache;
    }
}
