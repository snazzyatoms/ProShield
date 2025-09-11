package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.*;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.*;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
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

        // Bootstrapping
        messages = new MessagesUtil(this, getConfig()); // constructor now takes config
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this);

        guiManager = new GUIManager(this, roleManager); // manager now needs roleManager
        guiCache = new GUICache(guiManager);           // cache links to GUI manager

        registerCommands();
        registerListeners();

        messages.send(getServer().getConsoleSender(), "prefix",
                "&aProShield v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        plotManager.saveAll(); // updated persistence method
        messages.send(getServer().getConsoleSender(), "prefix", "&cProShield disabled.");
    }

    /* -------------------------------------------------------
     * Command Registration
     * ------------------------------------------------------- */
    private void registerCommands() {
        registerCommand("proshield", new ProShieldCommand(this, plotManager, roleManager, guiManager));
        registerCommand("claim", new ClaimSubCommand(this, plotManager));
        registerCommand("unclaim", new UnclaimSubCommand(this, plotManager));
        registerCommand("info", new InfoSubCommand(this, plotManager, roleManager));
        registerCommand("trust", new TrustCommand(this, plotManager, roleManager));
        registerCommand("untrust", new UntrustCommand(this, plotManager, roleManager));
        registerCommand("trusted", new TrustedListCommand(this, plotManager, roleManager));
        registerCommand("roles", new RolesCommand(this, plotManager, roleManager, guiManager));
        registerCommand("transfer", new TransferCommand(this, plotManager));
        registerCommand("preview", new PreviewSubCommand(this, plotManager));
        registerCommand("compass", new CompassSubCommand(this, guiManager));
    }

    /* -------------------------------------------------------
     * Listener Registration
     * ------------------------------------------------------- */
    private void registerListeners() {
        register(
                new PlayerJoinListener(this, guiManager, plotManager),
                new ClaimMessageListener(this, plotManager),
                new SpawnClaimGuardListener(this, plotManager),
                new BlockProtectionListener(this, plotManager, roleManager),
                new InteractionProtectionListener(this, plotManager, roleManager),
                new ExplosionProtectionListener(this, plotManager, roleManager),
                new FireProtectionListener(plotManager, messages),
                new BucketProtectionListener(this, plotManager),
                new ItemProtectionListener(this, plotManager, roleManager),
                new KeepDropsListener(this, plotManager),
                new EntityGriefProtectionListener(this, plotManager, roleManager),
                new PvpProtectionListener(this, plotManager, roleManager),
                new DamageProtectionListener(plotManager, messages)
        );

        // Mob repel background task
        new EntityMobRepelTask(this, plotManager).start();
    }

    private void register(Listener... listeners) {
        for (Listener l : listeners) {
            Bukkit.getPluginManager().registerEvents(l, this);
        }
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
        }
    }

    /* -------------------------------------------------------
     * Accessors
     * ------------------------------------------------------- */
    public static ProShield getInstance() { return instance; }
    public MessagesUtil getMessagesUtil() { return messages; }
    public GUIManager getGuiManager() { return guiManager; }
    public PlotManager getPlotManager() { return plotManager; }
    public ClaimRoleManager getRoleManager() { return roleManager; }

    /* -------------------------------------------------------
     * Bypass & Debug
     * ------------------------------------------------------- */
    public boolean toggleBypass(Player player) {
        if (bypassing.contains(player.getUniqueId())) {
            bypassing.remove(player.getUniqueId());
            return false;
        } else {
            bypassing.add(player.getUniqueId());
            return true;
        }
    }

    public boolean isBypassing(Player player) {
        return bypassing.contains(player.getUniqueId());
    }

    public boolean toggleDebug() {
        debugEnabled = !debugEnabled;
        return debugEnabled;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /* -------------------------------------------------------
     * Reload Support
     * ------------------------------------------------------- */
    public void reloadAll() {
        reloadConfig();
        messages.reload();
        plotManager.reloadFromConfig();
        roleManager.reloadFromConfig();
        guiManager.clearCache();
        messages.broadcastConsole("messages.reloaded", getServer().getConsoleSender());
    }
}
