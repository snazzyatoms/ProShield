package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.PlayerCommandDispatcher;
import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.MobProtectionListener;
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
    private ClaimRoleManager roleManager;
    private PlotManager plotManager;
    private ExpansionRequestManager expansionRequestManager;

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

        this.messages = new MessagesUtil(this);
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.expansionRequestManager = new ExpansionRequestManager(this);
        this.guiManager = new GUIManager(this);

        // Listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new MobProtectionListener(this, plotManager), this);

        // Commands
        PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            ProShieldCommand executor = new ProShieldCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // Player dispatcher (GUI + command handling)
        new PlayerCommandDispatcher(this, plotManager);

        getLogger().info("✅ ProShield enabled. Running version " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        plotManager.saveAll();
        getLogger().info("🛑 ProShield disabled.");
    }

    // Accessors
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

    public Set<UUID> getBypassing() {
        return bypassing;
    }

    public boolean isBypassing(UUID uuid) {
        return bypassing.contains(uuid);
    }

    public void toggleDebug() {
        debugEnabled = !debugEnabled;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void loadMessagesConfig() {
        saveResource("messages.yml", false);
    }
}
