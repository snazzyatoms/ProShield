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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        Bukkit.getPluginManager().registerEvents(new ChatListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new CompassListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(this, this); // join listener

        // Generate README.md
        generateReadme();

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
    // README.md Generator
    // --------------------
    private void generateReadme() {
        File readmeFile = new File(getDataFolder(), "README.md");
        String version = getDescription().getVersion();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder sb = new StringBuilder();
        sb.append("# ProShield README\n\n");
        sb.append("**Version:** ").append(version).append("\n");
        sb.append("**Last generated:** ").append(timestamp).append("\n\n");

        // Quick Start
        sb.append("## Quick Start\n");
        sb.append("- `/claim` → Claim your current land\n");
        sb.append("- `/unclaim` → Unclaim your land\n");
        sb.append("- `/proshield info` → Show claim details\n");
        sb.append("- `/proshield compass` → Get your ProShield Compass\n");
        sb.append("- `/proshield admin` → Open Admin Tools (requires permission)\n\n");

        // Permissions
        sb.append("## Permissions by Role\n");

        sb.append("### Player\n");
        sb.append("- `proshield.player.access` → Full access to player features (claims, flags, roles, transfer, compass)\n");
        sb.append("- `proshield.unlimited` → Ignore max-claims limit\n\n");

        sb.append("### Admin\n");
        sb.append("- `proshield.admin` → Access Admin Tools menu (reload, debug, bypass, general admin functions)\n\n");

        sb.append("### Senior Admin\n");
        sb.append("- `proshield.admin.expansions` → Approve/Deny expansion requests\n");
        sb.append("- `proshield.admin.worldcontrols` → Manage per-world controls\n\n");

        // Menus
        sb.append("## GUI Menus & Required Permissions\n");
        if (getConfig().isConfigurationSection("gui.menus")) {
            for (String menuKey : getConfig().getConfigurationSection("gui.menus").getKeys(false)) {
                sb.append("### ").append(menuKey).append("\n");
                var menuSec = getConfig().getConfigurationSection("gui.menus." + menuKey + ".items");
                if (menuSec != null) {
                    for (String slot : menuSec.getKeys(false)) {
                        var item = menuSec.getConfigurationSection(slot);
                        if (item == null) continue;
                        String name = item.getString("name", "Unnamed");
                        String perm = item.getString("permission", "none");
                        sb.append("- ").append(name)
                          .append(" → Requires: `").append(perm).append("`\n");
                    }
                }
                sb.append("\n");
            }
        }

        // Flags
        sb.append("## Default Claim Flags\n");
        Map<String, String> flagDescriptions = Map.of(
            "pvp", "Disables player-vs-player combat in claims",
            "explosions", "Prevents TNT, creeper, and other explosion damage",
            "fire", "Stops fire spread and burning in claims",
            "containers", "Protects chests, furnaces, hoppers, etc.",
            "armor-stands", "Protects armor stands",
            "item-frames", "Protects item frames",
            "buckets", "Disables bucket use inside claims",
            "pets", "Prevents harming tamed animals",
            "safezone", "Enforces safe-zone mechanics (no combat, griefing, etc.)"
        );

        if (getConfig().isConfigurationSection("claims.default-flags")) {
            getConfig().getConfigurationSection("claims.default-flags").getValues(false).forEach((k, v) -> {
                String desc = flagDescriptions.getOrDefault(k, "No description available");
                sb.append("- ").append(k).append(": ").append(v).append(" → ").append(desc).append("\n");
            });
        }

        try {
            if (!readmeFile.exists()) {
                readmeFile.getParentFile().mkdirs();
                readmeFile.createNewFile();
            }
            String newContent = sb.toString();

            // Only rewrite if different
            String oldContent = Files.exists(readmeFile.toPath()) ? Files.readString(readmeFile.toPath()) : "";
            if (!oldContent.equals(newContent)) {
                Files.writeString(readmeFile.toPath(), newContent);
                getLogger().info("README.md updated for version " + version);
            } else {
                getLogger().info("README.md already up-to-date.");
            }
        } catch (IOException e) {
            getLogger().warning("Failed to generate README.md: " + e.getMessage());
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
