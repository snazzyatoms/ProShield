package com.yourname.proshield;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config if missing
        saveDefaultConfig();

        getLogger().info("===================================");
        getLogger().info(" ProShield Enabled!");
        getLogger().info(" Version: " + getDescription().getVersion());
        getLogger().info("===================================");

        // Register commands
        getCommand("proshield").setExecutor(new ProShieldCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlotProtectionListener(this), this);

        // Handle Discord integration
        setupDiscordIntegration();
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield Disabled.");
    }

    private void setupDiscordIntegration() {
        boolean discordEnabled = getConfig().getBoolean("discord.enabled", false);

        if (!discordEnabled) {
            getLogger().info("Discord integration is disabled in config.yml.");
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null) {
            getLogger().info("DiscordSRV found! Hooking into Discord...");
            // TODO: Add Discord hook logic here if you want later
        } else {
            getLogger().warning("DiscordSRV is not installed! Skipping Discord integration.");
        }
    }

    public static ProShield getInstance() {
        return instance;
    }
}
