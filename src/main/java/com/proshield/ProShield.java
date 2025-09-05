package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.listeners.AdminJoinListener;
import com.snazzyatoms.proshield.listeners.PlotProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    @Override
    public void onEnable() {
        instance = this;

        // Config auto-create
        saveDefaultConfig();

        // Register commands
        getCommand("proshield").setExecutor(new ProShieldCommand(this));

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlotProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AdminJoinListener(this), this);

        getLogger().info("✅ ProShield 1.1.6 enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ ProShield disabled.");
    }

    public static ProShield getInstance() {
        return instance;
    }
}
