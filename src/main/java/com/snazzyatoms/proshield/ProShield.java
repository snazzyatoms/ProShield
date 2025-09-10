package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.*;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.listeners.*;
import com.snazzyatoms.proshield.plots.*;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Load config
        saveDefaultConfig();

        // Managers
        this.roleManager = new ClaimRoleManager(this);
        this.plotManager = new PlotManager(this, roleManager);
        this.guiManager = new GUIManager(this, plotManager);

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(plotManager, guiManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new ClaimMessageListener(plotManager, roleManager), this);
        getServer().getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new GUIListener(guiManager), this);
        getServer().getPluginManager().registerEvents(new MobBorderRepelListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new SpawnGuardListener(plotManager), this);

        // Commands
        registerCommand("proshield", new ProShieldCommand(this, plotManager, guiManager));
        registerCommand("trustmenu", new TrustMenuCommand(guiManager));
        registerCommand("untrustmenu", new UntrustMenuCommand(guiManager));
        registerCommand("rolemenu", new RoleMenuCommand(guiManager));
        registerCommand("flagmenu", new FlagMenuCommand(guiManager));
        registerCommand("transfermenu", new TransferMenuCommand(guiManager));

        getLogger().info("✅ ProShield enabled (v" + getDescription().getVersion() + ")");
    }

    @Override
    public void onDisable() {
        if (plotManager != null) {
            plotManager.saveClaims();
        }
        getLogger().info("⛔ ProShield disabled");
    }

    private void registerCommand(String name, Object executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor((org.bukkit.command.CommandExecutor) executor);
            if (executor instanceof org.bukkit.command.TabCompleter) {
                cmd.setTabCompleter((org.bukkit.command.TabCompleter) executor);
            }
        } else {
            getLogger().warning("⚠️ Command not found in plugin.yml: " + name);
        }
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

    public void reloadProShield() {
        reloadConfig();
        roleManager.reloadFromConfig();
        plotManager.reloadFromConfig();
        guiManager.onConfigReload();

        getLogger().info("♻️ ProShield config reloaded.");
    }
}
