package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.compass.CompassListener;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.ChatListener;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.listeners.ProtectionListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProShield extends JavaPlugin implements Listener {

    private static ProShield instance;

    private MessagesUtil messages;
    private GUIManager guiManager;
    private ClaimRoleManager roleManager;
    private PlotManager plotManager;
    private CompassManager compassManager;

    private final Set<UUID> bypassing = new HashSet<>();
    private boolean debugEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        messages = new MessagesUtil(this);

        // Initialize managers
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this);
        guiManager = new GUIManager(this);
        compassManager = new CompassManager(this, guiManager);

        // Load persisted data
        roleManager.loadAll();
        plotManager.loadAll();

        // Register commands
        PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            ProShieldCommand executor = new ProShieldCommand(this, guiManager, plotManager, messages);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CompassListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(this, this); // for join-event below

        getLogger().info("ProShield enabled successfully.");
    }

    @Override
    public void onDisable() {
        // Save persisted data
        roleManager.saveAll();
        plotManager.saveAll();

        getLogger().info("ProShield disabled and data saved.");
    }

    // --------------------
    // Join listener
    // --------------------
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (getConfig().getBoolean("settings.give-compass-on-join", true)) {
            compassManager.giveCompass(event.getPlayer());
        }
    }

    // --------------------
    // Getters
    // --------------------
    public static ProShield getInstance() {
        return instance;
    }

    public MessagesUtil getMessagesUtil() {
        return messages;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public CompassManager getCompassManager() {
        return compassManager;
    }

    public Set<UUID> getBypassing() {
        return bypassing;
    }

    public boolean isBypassing(UUID uuid) {
        return bypassing.contains(uuid);
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void toggleDebug() {
        debugEnabled = !debugEnabled;
    }
}
