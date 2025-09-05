package com.proshield;

import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("ProShield has been enabled!");

        // Register commands and listeners
        registerCommands();
        registerListeners();

        // Optional: Hook into DiscordSRV if available
        if (getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
            getLogger().info("DiscordSRV detected! Hooking into it...");
            hookIntoDiscordSRV();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield has been disabled!");
    }

    private void registerCommands() {
        // Example: register your plugin commands here
        // this.getCommand("proshield").setExecutor(new ProShieldCommand());
    }

    private void registerListeners() {
        // Example: register your plugin listeners here
        // getServer().getPluginManager().registerEvents(new PlotProtectionListener(), this);
    }

    private void hookIntoDiscordSRV() {
        try {
            // TODO: Add actual DiscordSRV integration here
            // Example: DiscordSRV.getPlugin().sendMessage("ProShield is active!");
            getLogger().info("Successfully hooked into DiscordSRV!");
        } catch (Exception e) {
            getLogger().warning("Failed to hook into DiscordSRV: " + e.getMessage());
        }
    }
}
