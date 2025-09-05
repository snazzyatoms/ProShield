package com.snazzyatoms.proshield;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("ProShield v1.0.6 has been enabled!");

        // Check if DiscordSRV is installed (soft dependency)
        if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null) {
            getLogger().info("DiscordSRV detected! Hooking into it...");
            hookIntoDiscordSRV();
        } else {
            getLogger().info("DiscordSRV not found. Continuing without Discord integration.");
        }

        // Register commands, events, etc.
        registerCommands();
        registerEvents();
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield v1.0.6 has been disabled.");
    }

    private void hookIntoDiscordSRV() {
        // Example placeholder hook — expand later if you add features
        getLogger().info("Successfully hooked into DiscordSRV!");
    }

    private void registerCommands() {
        // Example command registration
        // this.getCommand("proshield").setExecutor(new ProShieldCommand(this));
    }

    private void registerEvents() {
        // Example event registration
        // getServer().getPluginManager().registerEvents(new PlotProtectionListener(this), this);
    }
}
