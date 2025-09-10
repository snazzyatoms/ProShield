package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;
    private GUIManager guiManager;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Save config if not exists
        saveDefaultConfig();
        reloadConfig();

        // Managers
        this.plotManager = new PlotManager(this);
        this.guiManager = new GUIManager(this, plotManager);

        // Command executor
        ProShieldCommand commandExecutor = new ProShieldCommand(this, plotManager, guiManager);

        // 🔹 Auto-register ALL commands from plugin.yml
        if (getDescription().getCommands() != null) {
            getDescription().getCommands().keySet().forEach(cmd -> {
                if (getCommand(cmd) != null) {
                    getCommand(cmd).setExecutor(commandExecutor);
                    getCommand(cmd).setTabCompleter(commandExecutor);
                } else {
                    getLogger().warning("⚠️ Command '" + cmd + "' in plugin.yml could not be registered!");
                }
            });
        }

        // 🔹 Register listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new MobBorderRepelListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new SpawnClaimGuardListener(plotManager), this);

        getLogger().info("✅ ProShield " + getDescription().getVersion() + " enabled successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("⛔ ProShield disabled.");
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
