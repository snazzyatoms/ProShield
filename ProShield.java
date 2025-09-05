package com.snazzyatoms.proshield;

import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("ProShield has been enabled!");

        // Register listeners
        getServer().getPluginManager().registerEvents(new com.snazzyatoms.proshield.listeners.PlotProtectionListener(), this);

        // Register command
        getCommand("proshield").setExecutor(new com.snazzyatoms.proshield.commands.ProShieldCommand());
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield has been disabled!");
    }

    public static ProShield getInstance() {
        return instance;
    }
}
