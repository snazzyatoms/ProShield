package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.*;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
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

        messages    = new MessagesUtil(this);
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this);
        guiManager  = new GUIManager(this);

        // 🔄 Load trusted/roles persistence
        roleManager.loadAll();

        // Register command executor
        ProShieldCommand executor = new ProShieldCommand(this, guiManager, plotManager, messages);
        getCommand("proshield").setExecutor(executor);
        getCommand("claim").setExecutor(executor);
        getCommand("unclaim").setExecutor(executor);
        getCommand("trust").setExecutor(executor);
        getCommand("untrust").setExecutor(executor);
        getCommand("roles").setExecutor(executor);
        getCommand("transfer").setExecutor(executor);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new AdminGUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CompassListener(this), this);

        getLogger().info("ProShield enabled!");
    }

    @Override
    public void onDisable() {
        // 💾 Save trusted/roles persistence
        if (roleManager != null) {
            try {
                roleManager.saveAll();
            } catch (Exception e) {
                getLogger().warning("Failed to save roles.yml on shutdown: " + e.getMessage());
            }
        }
        getLogger().info("ProShield disabled!");
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

    public boolean isBypassing(UUID id) {
        return bypassing.contains(id);
    }
}
