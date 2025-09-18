package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.PlayerCommandDispatcher;
import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.listeners.MobControlTasks;
import com.snazzyatoms.proshield.plots.ClaimProtectionListener;
import com.snazzyatoms.proshield.plots.MobProtectionListener;
import com.snazzyatoms.proshield.plots.PlotListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private MessagesUtil messages;
    private GUIManager guiManager;
    private ClaimRoleManager roleManager;
    private PlotManager plotManager;
    private ExpansionRequestManager expansionRequestManager;

    private final Set<UUID> bypassing = new HashSet<>();
    private final Set<UUID> debugging = new HashSet<>();
    private boolean debugEnabled = false;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Configs
        saveDefaultConfig();
        loadMessagesConfig();

        // Core managers
        this.messages = new MessagesUtil(this);
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.expansionRequestManager = new ExpansionRequestManager(this);
        this.guiManager = new GUIManager(this);

        // Core listeners
        PlotListener plotListener = new PlotListener(this, plotManager);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(plotListener, this);
        Bukkit.getPluginManager().registerEvents(new MobProtectionListener(this, plotManager, plotListener), this);
        Bukkit.getPluginManager().registerEvents(new ClaimProtectionListener(this, plotManager), this);

        // Repeating tasks (mob repel + despawn)
        new MobControlTasks(this);

        // Commands
        PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            ProShieldCommand executor = new ProShieldCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // Compass dispatcher (player QoL)
        new PlayerCommandDispatcher(this);

        getLogger().info("✅ ProShield enabled. Running version " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        // Save state before shutdown
        if (plotManager != null) {
            plotManager.saveAll();
        }
        if (expansionRequestManager != null) {
            expansionRequestManager.saveAll();
        }
        getLogger().info("🛑 ProShield disabled.");
    }

    // Accessors
    public MessagesUtil getMessagesUtil() { return messages; }
    public GUIManager getGuiManager() { return guiManager; }
    public ClaimRoleManager getRoleManager() { return roleManager; }
    public PlotManager getPlotManager() { return plotManager; }
    public ExpansionRequestManager getExpansionRequestManager() { return expansionRequestManager; }

    // Admin bypass
    public Set<UUID> getBypassing() { return bypassing; }
    public boolean isBypassing(UUID uuid) { return bypassing.contains(uuid); }

    // Debug toggle
    public void toggleDebug() { debugEnabled = !debugEnabled; }
    public boolean isDebugEnabled() { return debugEnabled; }

    public void enableDebug(UUID uuid) { debugging.add(uuid); }
    public void disableDebug(UUID uuid) { debugging.remove(uuid); }
    public boolean isDebugging(UUID uuid) { return debugging.contains(uuid); }

    // Config / messages loader
    public void loadMessagesConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }
    }

    /** Reload both config.yml and messages.yml safely */
    public void reloadAllConfigs() {
        reloadConfig();
        loadMessagesConfig();
        if (messages != null) {
            messages.reload();
        }
    }
}
