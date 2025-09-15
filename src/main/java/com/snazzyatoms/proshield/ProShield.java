package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.ChatListener;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.compass.CompassListener;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private MessagesUtil messages;
    private GUIManager guiManager;
    private ClaimRoleManager roleManager;
    private PlotManager plotManager;
    private GUICache guiCache;

    private final Set<UUID> bypassing = new HashSet<>();
    private boolean debugEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadMessagesConfig();

        messages = new MessagesUtil(this);
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this, plotManager);
        guiManager = new GUIManager(this);
        guiCache = new GUICache();

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new CompassListener(this), this);

        // Register commands
        ProShieldCommand commandExecutor = new ProShieldCommand(this, guiManager, roleManager);
        getCommand("proshield").setExecutor(commandExecutor);
        getCommand("proshield").setTabCompleter(commandExecutor);

        getLogger().info("✅ ProShield v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ ProShield disabled.");
    }

    private void loadMessagesConfig() {
        saveResource("messages.yml", false);
    }

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

    public GUICache getGuiCache() {
        return guiCache;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public boolean isBypassing(Player player) {
        return bypassing.contains(player.getUniqueId());
    }

    public void toggleBypass(Player player) {
        if (bypassing.contains(player.getUniqueId())) {
            bypassing.remove(player.getUniqueId());
            messages.send(player, "&eBypass &cDISABLED");
        } else {
            bypassing.add(player.getUniqueId());
            messages.send(player, "&eBypass &aENABLED");
        }
    }
}
