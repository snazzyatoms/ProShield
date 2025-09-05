package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.listeners.AdminJoinListener;
import com.snazzyatoms.proshield.listeners.PlotProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("ProShield enabled!");

        // Register commands
        this.getCommand("proshield").setExecutor(new ProShieldCommand(this));

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlotProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new AdminJoinListener(this), this);

        saveDefaultConfig(); // creates config folder + file if missing
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield disabled!");
    }
}
