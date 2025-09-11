// src/main/java/com/snazzyatoms/proshield/ProShield.java
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

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        messages = new MessagesUtil(this);
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this);
        guiCache = new GUICache(new GUIManager(this)); // GUIManager still manages GUIs
        guiManager = guiCache.getGuiManager();

        registerCommands();
        registerListeners();

        // ✅ Schedule mob repel task (runs every 5 seconds)
        Bukkit.getScheduler().runTaskTimer(this,
                new EntityMobRepelTask(this, plotManager),
                20L * 5, 20L * 5
        );

        messages.send(getServer().getConsoleSender(), "prefix", "&aProShield v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        plotManager.saveAll(); // ✅ corrected method name
        messages.send(getServer().getConsoleSender(), "prefix", "&cProShield disabled.");
    }

    private void registerCommands() {
        registerCommand("proshield", new ProShieldCommand(this, plotManager, guiManager));
        registerCommand("claim", new ClaimSubCommand(this, plotManager));
        registerCommand("unclaim", new UnclaimSubCommand(this, plotManager));
        registerCommand("info", new InfoSubCommand(this, plotManager, roleManager));
        registerCommand("trust", new TrustCommand(this, plotManager, roleManager));
        registerCommand("untrust", new UntrustCommand(this, plotManager, roleManager));
        registerCommand("trusted", new TrustedListCommand(this, plotManager, roleManager));
        registerCommand("roles", new RolesCommand(this, plotManager, roleManager));
        registerCommand("transfer", new TransferCommand(this, plotManager));
        registerCommand("preview", new PreviewSubCommand(this, plotManager));
        registerCommand("compass", new CompassSubCommand(this, guiManager));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, guiManager, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new InteractionProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new ExplosionProtectionListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new FireProtectionListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new BucketProtectionListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new EntityGriefProtectionListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new SpawnGuardListener(this), this);

        // Optionally, we could register border repel here too:
        // Bukkit.getScheduler().runTaskTimer(this, new EntityBorderRepelTask(this, plotManager), 20L * 10, 20L * 10);
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
