package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.PlayerCommandDispatcher;
import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.compass.CompassListener;
import com.snazzyatoms.proshield.gui.ChatListener;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.MobProtectionListener;
import com.snazzyatoms.proshield.plots.PlotListener;
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
    private PlotManager plotManager;
    private ClaimRoleManager roleManager;

    private GUICache guiCache;
    private GUIManager guiManager;

    // Server-wide bypass set (restored)
    private final Set<UUID> bypassing = new HashSet<>();

    private boolean debugEnabled = false;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        messages = new MessagesUtil(this);
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(plotManager);

        guiCache = new GUICache();
        guiManager = new GUIManager(this);

        registerCommands();
        registerListeners();

        getLogger().info("ProShield v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (plotManager != null) {
            plotManager.saveAll();
        }
        getLogger().info("ProShield disabled.");
    }

    private void registerCommands() {
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(
                new ProShieldCommand(this, guiManager, plotManager, messages)
            );
        }

        PlayerCommandDispatcher playerCommandDispatcher =
            new PlayerCommandDispatcher(this, plotManager, roleManager, messages);

        if (getCommand("claim") != null) getCommand("claim").setExecutor(playerCommandDispatcher);
        if (getCommand("unclaim") != null) getCommand("unclaim").setExecutor(playerCommandDispatcher);
        if (getCommand("trust") != null) getCommand("trust").setExecutor(playerCommandDispatcher);
        if (getCommand("untrust") != null) getCommand("untrust").setExecutor(playerCommandDispatcher);
        if (getCommand("roles") != null) getCommand("roles").setExecutor(playerCommandDispatcher);
        if (getCommand("transfer") != null) getCommand("transfer").setExecutor(playerCommandDispatcher);
    }

    private void registerListeners() {
        // GUI click events
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);

        // Manual deny reason via chat
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);

        // Plot + compass + mob listeners
        Bukkit.getPluginManager().registerEvents(new PlotListener(this, plotManager, roleManager, messages), this);
        Bukkit.getPluginManager().registerEvents(new CompassListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new MobProtectionListener(this, plotManager), this);
    }

    // Getters
    public MessagesUtil getMessagesUtil() { return messages; }
    public PlotManager getPlotManager() { return plotManager; }
    public ClaimRoleManager getRoleManager() { return roleManager; }
    public GUICache getGuiCache() { return guiCache; }
    public GUIManager getGuiManager() { return guiManager; }

    // Bypass set (restored)
    public Set<UUID> getBypassing() { return bypassing; }

    // Debug support
    public boolean isDebugEnabled() { return debugEnabled; }
    public void setDebugEnabled(boolean enabled) { this.debugEnabled = enabled; }
    public void toggleDebug() {
        this.debugEnabled = !this.debugEnabled;
        if (messages != null) {
            messages.debug("Debug mode toggled: " + (this.debugEnabled ? "ON" : "OFF"));
        }
    }
}
