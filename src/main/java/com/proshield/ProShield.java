package com.snazzyatoms.proshield;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.listeners.AdminJoinListener;
import com.snazzyatoms.proshield.listeners.CompassListener;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    @Override
    public void onEnable() {
        instance = this;

        // Log startup
        getLogger().info("ProShield v" + getDescription().getVersion() + " has been enabled!");

        // Save default config if missing
        saveDefaultConfig();

        // Register command executor
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(new ProShieldCommand(this));
        }

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new AdminJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new CompassListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield has been disabled.");
    }

    public static ProShield getInstance() {
        return instance;
    }
}
