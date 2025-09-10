package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.*;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProShield extends JavaPlugin {

    /** Legacy static prefix (kept for older listeners that still reference it). */
    public static final String PREFIX = ChatColor.DARK_AQUA + "[ProShield] " + ChatColor.RESET;

    // Core singletons
    private MessagesUtil messages;
    private GUIManager guiManager;
    private PlotManager plotManager;
    private ClaimRoleManager roleManager;

    // Runtime state
    private final Set<UUID> bypassing = ConcurrentHashMap.newKeySet();
    private volatile boolean debugEnabled = false;

    @Override
    public void onEnable() {
        // Config bootstrapping
        saveDefaultConfig();
        // Ensure messages.yml exists and load it
        saveResource("messages.yml", false);
        messages = new MessagesUtil(this); // reads /plugins/ProShield/messages.yml

        // Instantiate managers
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this);
        guiManager = new GUIManager(this);

        // Register listeners (keep/add all we’ve built)
        registerListeners(
                new PlayerJoinListener(this, plotManager),
                new ClaimMessageListener(this, plotManager),
                new SpawnClaimGuardListener(this, plotManager),
                new MobBorderRepelListener(this, plotManager),
                new DamageProtectionListener(this, plotManager, roleManager),
                new BlockProtectionListener(this, plotManager, roleManager),
                new InteractionProtectionListener(this, plotManager, roleManager),
                new ExplosionProtectionListener(this, plotManager, roleManager),
                new FireProtectionListener(this, plotManager),
                new BucketProtectionListener(this, plotManager),
                new ItemProtectionListener(this, plotManager, roleManager),
                new KeepDropsListener(this, plotManager),
                new EntityGriefProtectionListener(this, plotManager, roleManager),
                new PvpProtectionListener(this, plotManager, roleManager)
        );

        // Register commands (main dispatcher + split commands)
        wireCommand("proshield", new ProShieldCommand(this, plotManager, roleManager, guiManager));
        wireCommand("claim", new ClaimSubCommand(this, plotManager));
        wireCommand("unclaim", new UnclaimSubCommand(this, plotManager));
        wireCommand("info", new InfoSubCommand(this, plotManager, roleManager));
        wireCommand("trust", new TrustCommand(this, plotManager, roleManager));
        wireCommand("untrust", new UntrustCommand(this, plotManager, roleManager));
        wireCommand("trusted", new TrustedListCommand(this, plotManager, roleManager));
        wireCommand("roles", new RolesCommand(this, plotManager, roleManager));
        wireCommand("transfer", new TransferCommand(this, plotManager));
        wireCommand("preview", new PreviewSubCommand(this, plotManager));
        wireCommand("compass", new CompassSubCommand(this, guiManager));

        // Optional: schedule recurring tasks owned by listeners (repel, expiry, etc.)
        // Those are started inside the listeners/managers themselves in our design.

        if (isDebugEnabled()) {
            getLogger().info("[ProShield] Debug mode is ON");
        }
        getLogger().info("[ProShield] Enabled v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        // Persist anything necessary; our managers write via config on change, so nothing heavy here.
        getLogger().info("[ProShield] Disabled");
    }

    /* -------------------------------------------------------
     * Accessors / Exposure
     * ------------------------------------------------------- */

    public MessagesUtil getMessagesUtil() {
        return messages;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /* -------------------------------------------------------
     * Bypass / Debug toggles (as requested)
     * ------------------------------------------------------- */

    /** Toggle bypass for a player. Returns the new state (true = now bypassing). */
    public boolean toggleBypass(Player player) {
        UUID id = player.getUniqueId();
        boolean nowBypassing;
        if (bypassing.contains(id)) {
            bypassing.remove(id);
            nowBypassing = false;
            messages.send(player, "admin.bypass-off");
        } else {
            bypassing.add(id);
            nowBypassing = true;
            messages.send(player, "admin.bypass-on");
        }
        return nowBypassing;
    }

    /** Check whether the player is currently bypassing claim protections. */
    public boolean isBypassing(Player player) {
        return player != null && bypassing.contains(player.getUniqueId());
    }

    /** Toggle debug flag globally and announce to console + player (if provided). */
    public boolean toggleDebug() {
        debugEnabled = !debugEnabled;
        getLogger().info("[ProShield] Debug: " + (debugEnabled ? "ENABLED" : "DISABLED"));
        return debugEnabled;
    }

    /* -------------------------------------------------------
     * Reload entrypoint (used by /proshield reload & GUI)
     * ------------------------------------------------------- */

    /** Soft reload of config + messages + caches + manager settings. */
    public void reloadAll() {
        // Bukkit config.yml
        reloadConfig();

        // Messages
        if (messages != null) {
            messages.reload(); // re-read messages.yml
        } else {
            messages = new MessagesUtil(this);
        }

        // Managers
        if (plotManager != null) {
            // keep a method with this name to satisfy call sites from earlier patches
            plotManager.reloadFromConfig();
        }
        if (roleManager != null) {
            roleManager.reloadFromConfig();
        }

        // GUI cache clear
        if (guiManager != null) {
            guiManager.clearCache();
        }

        // Announce
        messages.broadcastConsole("messages.reloaded", getServer().getConsoleSender());
    }

    /* -------------------------------------------------------
     * Helpers
     * ------------------------------------------------------- */

    private void wireCommand(String label, Object executor) {
        PluginCommand cmd = getCommand(label);
        if (cmd != null) {
            if (executor instanceof org.bukkit.command.CommandExecutor ce) {
                cmd.setExecutor(ce);
            }
            if (executor instanceof org.bukkit.command.TabCompleter tc) {
                cmd.setTabCompleter(tc);
            }
        } else {
            getLogger().warning("Command not found in plugin.yml: " + label);
        }
    }

    private void registerListeners(Listener... listeners) {
        for (Listener l : listeners) {
            Bukkit.getPluginManager().registerEvents(l, this);
        }
    }

    /* -------------------------------------------------------
     * Legacy convenience (used by some older code paths)
     * ------------------------------------------------------- */

    /** Prefix string for ad-hoc messages (prefer MessagesUtil in new code). */
    public String getPrefix() {
        return PREFIX;
    }
}
