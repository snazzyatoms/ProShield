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

    // Repel tasks
    private EntityMobRepelTask mobRepelTask;
    private EntityBorderRepelTask borderRepelTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Managers
        messages = new MessagesUtil(this);
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this);

        // ✅ Corrected GUICache (no args) + GUIManager
        guiCache = new GUICache();
        guiManager = new GUIManager(this, guiCache);

        registerCommands();
        registerListeners();

        // Start repel tasks
        mobRepelTask = new EntityMobRepelTask(this, plotManager);
        mobRepelTask.start();

        borderRepelTask = new EntityBorderRepelTask(this, plotManager);
        borderRepelTask.start();

        messages.send(getServer().getConsoleSender(), "prefix",
                "&aProShield v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        // Stop repel tasks cleanly
        if (mobRepelTask != null) mobRepelTask.stop();
        if (borderRepelTask != null) borderRepelTask.stop();

        // ✅ Explicitly save plots
        if (plotManager != null) {
            plotManager.saveAll();
        }

        if (messages != null) {
            messages.send(getServer().getConsoleSender(), "prefix", "&cProShield disabled.");
        }
    }

    private void registerCommands() {
        registerCommand("proshield", new ProShieldCommand(this, plotManager, guiManager));
        registerCommand("trust", new TrustCommand(this, plotManager, roleManager));
        registerCommand("untrust", new UntrustCommand(this, plotManager, roleManager));
        registerCommand("roles", new RolesCommand(this, plotManager, roleManager, guiManager));
        registerCommand("transfer", new TransferCommand(this, plotManager));
        registerCommand("flags", new FlagsCommand(guiManager)); // ✅ FIXED constructor
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, guiManager, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new InteractionProtectionListener(this, plotManager, roleManager), this);

        Bukkit.getPluginManager().registerEvents(new ExplosionProtectionListener(this, plotManager, messages), this);
        Bukkit.getPluginManager().registerEvents(new FireProtectionListener(this, plotManager, messages), this);
        Bukkit.getPluginManager().registerEvents(new BucketProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new DamageProtectionListener(plotManager, messages), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(this, plotManager), this); // ✅ fixed
        Bukkit.getPluginManager().registerEvents(new EntityGriefProtectionListener(this, plotManager, messages), this);

        // ✅ Fixed: now passes plotManager instead of this
        Bukkit.getPluginManager().registerEvents(new DamageProtectionListener(plotManager, messages), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plotManager, messages), this);

        // ✅ Fixed ClaimMessageListener args
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(this, plotManager, messages), this);

        Bukkit.getPluginManager().registerEvents(new SpawnGuardListener(this), this);

        // ✅ Fixed FlagsListener args (ProShield, PlotManager)
        Bukkit.getPluginManager().registerEvents(new FlagsListener(this, plotManager), this);
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
        }
    }

    public static ProShield getInstance() {
        return instance;
    }

    public MessagesUtil getMessagesUtil() {
        return messages;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public GUICache getGuiCache() {
        return guiCache;
    }

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
}
