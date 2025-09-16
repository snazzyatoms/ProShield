package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.ChatListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.tasks.EntityBorderRepelTask;
import com.snazzyatoms.proshield.tasks.EntityMobRepelTask;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
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

    private FileConfiguration messagesConfig;

    private final Set<UUID> bypassing = new HashSet<>();
    private boolean debugEnabled = false;

    // Repel tasks
    private EntityMobRepelTask mobRepelTask;
    private EntityBorderRepelTask borderRepelTask;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadMessagesConfig();

        this.messages = new MessagesUtil(this);
        this.roleManager = new ClaimRoleManager(this);
        this.plotManager = new PlotManager(this);
        this.expansionRequestManager = new ExpansionRequestManager(this);
        this.guiManager = new GUIManager(this);

        // Register commands
        PluginCommand psCommand = getCommand("proshield");
        if (psCommand != null) {
            ProShieldCommand executor = new ProShieldCommand(this, guiManager, plotManager, messages);
            psCommand.setExecutor(executor);
            psCommand.setTabCompleter(executor);
        }

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this, guiManager), this);

        // Tasks for mob repel / despawn
        this.mobRepelTask = new EntityMobRepelTask(this);
        this.borderRepelTask = new EntityBorderRepelTask(this);
        mobRepelTask.runTaskTimer(this, 0L, 20L * 30); // every 30s
        borderRepelTask.runTaskTimer(this, 0L, 20L * 5); // every 5s

        getLogger().info("ProShield v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (mobRepelTask != null) mobRepelTask.cancel();
        if (borderRepelTask != null) borderRepelTask.cancel();
        getLogger().info("ProShield disabled.");
    }

    // ==================================================
    // Config + Messages Handling
    // ==================================================
    public void loadMessagesConfig() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public void saveMessagesConfig() {
        try {
            messagesConfig.save(new File(getDataFolder(), "messages.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================================================
    // Getters
    // ==================================================
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

    public ExpansionRequestManager getExpansionRequestManager() {
        return expansionRequestManager;
    }

    public Set<UUID> getBypassing() {
        return bypassing;
    }

    // ==================================================
    // Debug + Bypass
    // ==================================================
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void toggleDebug() {
        debugEnabled = !debugEnabled;
    }

    public boolean isBypassing(UUID uuid) {
        return bypassing.contains(uuid);
    }

    // Utility: reload both configs
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        loadMessagesConfig();
    }

    // ==================================================
    // Utilities
    // ==================================================
    public void sendDebug(Player player, String msg) {
        if (debugEnabled) {
            messages.send(player, "&8[Debug] " + msg);
        }
    }
}
