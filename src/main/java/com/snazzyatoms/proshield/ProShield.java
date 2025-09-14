// src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.*;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private MessagesUtil messages;
    private GUIManager guiManager;
    private PlotManager plotManager;
    private ClaimRoleManager roleManager;

    private final Set<UUID> bypassing = new HashSet<>();
    private boolean debugEnabled = false;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.messages = new MessagesUtil(this);
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.guiManager = new GUIManager(this);

        // Register GUI listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new AdminGUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);

        // Register unified command executor
        ProShieldCommand executor = new ProShieldCommand(this, guiManager, plotManager, messages);
        registerCommand("proshield", executor);
        registerCommand("claim", executor);
        registerCommand("unclaim", executor);
        registerCommand("trust", executor);
        registerCommand("untrust", executor);
        registerCommand("roles", executor);
        registerCommand("transfer", executor);

        getLogger().info("✅ ProShield enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ ProShield disabled.");
    }

    private void registerCommand(String name, ProShieldCommand executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
        } else {
            getLogger().warning("Command not found in plugin.yml: " + name);
        }
    }

    public MessagesUtil getMessagesUtil() {
        return messages;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
    }

    public Set<UUID> getBypassing() {
        return bypassing;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void toggleDebug() {
        debugEnabled = !debugEnabled;
    }
}
