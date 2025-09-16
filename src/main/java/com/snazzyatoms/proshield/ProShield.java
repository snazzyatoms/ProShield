package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.compass.CompassListener;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.ChatListener;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.listeners.MobControlTasks;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * ProShield main plugin class
 * v1.2.5 – fully patched with MobControlTasks
 */
public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private MessagesUtil messages;
    private GUIManager guiManager;
    private ClaimRoleManager roleManager;
    private PlotManager plotManager;
    private ExpansionRequestManager expansionRequestManager;
    private CompassManager compassManager;
    private GUICache guiCache;

    private final Set<UUID> bypassing = new HashSet<>();
    private boolean debugEnabled = false;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadMessagesConfig();

        messages = new MessagesUtil(this);
        roleManager = new ClaimRoleManager(this);
        plotManager = new PlotManager(this);
        expansionRequestManager = new ExpansionRequestManager(this);
        guiManager = new GUIManager(this);
        compassManager = new CompassManager(this, guiManager);
        guiCache = new GUICache();

        // Register commands
        PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            ProShieldCommand executor = new ProShieldCommand(this, guiManager, plotManager, messages);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new CompassListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(compassManager, this);

        // ✅ Start mob repel + despawn control (config-driven)
        new MobControlTasks(this);

        // Distribute compasses to online players if config allows
        compassManager.giveCompassToAll();

        getLogger().info("ProShield v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield disabled.");
    }

    /* ======================= Accessors ======================= */

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

    public ExpansionRequestManager getExpansionRequestManager() {
        return expansionRequestManager;
    }

    public CompassManager getCompassManager() {
        return compassManager;
    }

    public GUICache getGuiCache() {
        return guiCache;
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
        this.debugEnabled = !this.debugEnabled;
    }

    public void loadMessagesConfig() {
        saveResource("messages.yml", false);
    }
}
