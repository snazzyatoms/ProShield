package com.snazzyatoms.proshield;

import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("ProShield has been enabled!");
        // Register commands, listeners, managers here
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield has been disabled!");
    }
}

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
