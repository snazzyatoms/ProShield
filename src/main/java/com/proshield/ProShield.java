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

        // Register commands
        this.getCommand("proshield").setExecutor(new ProShieldCommand());

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlotProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new AdminJoinListener(), this);

        // Create and save default config
        saveDefaultConfig();

        Bukkit.getLogger().info("[ProShield] v" + getDescription().getVersion() + " has been enabled.");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("[ProShield] v" + getDescription().getVersion() + " has been disabled.");
    }

    public static ProShield getInstance() {
        return instance;
    }
}
