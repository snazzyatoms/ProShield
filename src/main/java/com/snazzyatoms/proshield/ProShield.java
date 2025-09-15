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
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProShield extends JavaPlugin implements Listener {

    private static ProShield instance;

    private MessagesUtil messages;
    private GUIManager guiManager;
    private ClaimRoleManager roleManager;
    private PlotManager plotManager;
    private CompassManager compassManager;

    private FileConfiguration messagesConfig;

    private final Set<UUID> bypassing = new HashSet<>();
    private boolean debugEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        // Ensure data folder
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        // Save defaults and merge
        saveDefaultConfig();
        mergeConfig("config.yml");
        loadMessagesConfig();

        // Managers
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this);
        guiManager  = new GUIManager(this);
        compassManager = new CompassManager(this, guiManager);

        // Load persisted data
        roleManager.loadAll();
        plotManager.loadAll();

        // Commands
        PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            ProShieldCommand executor = new ProShieldCommand(this, guiManager, plotManager, messages);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // Listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new CompassListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(this, this);

        // README
        generateReadme();

        getLogger().info("ProShield enabled successfully.");
    }

    @Override
    public void onDisable() {
        roleManager.saveAll();
        plotManager.saveAll();
        getLogger().info("ProShield disabled and data saved.");
    }

    // --------------------
    // messages.yml loader
    // --------------------
    public void loadMessagesConfig() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = new YamlConfiguration();
        try {
            messagesConfig.load(file);

            YamlConfiguration defaults = new YamlConfiguration();
            try (InputStreamReader reader =
                         new InputStreamReader(Objects.requireNonNull(getResource("messages.yml")), StandardCharsets.UTF_8)) {
                defaults.load(reader);
            }

            boolean changed = false;
            for (String key : defaults.getKeys(true)) {
                if (!messagesConfig.contains(key)) {
                    messagesConfig.set(key, defaults.get(key));
                    getLogger().info("[Messages] Added missing key: " + key);
                    changed = true;
                }
            }
            if (changed) {
                messagesConfig.save(file);
                getLogger().info("[Messages] messages.yml updated with new defaults.");
            }
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("Failed to load messages.yml: " + e.getMessage());
        }

        // Init/refresh MessagesUtil after messagesConfig is ready
        messages = new MessagesUtil(this, messagesConfig);
    }

    public void reloadMessages() {
        loadMessagesConfig();
    }

    // --------------------
    // Auto-Merge config
    // --------------------
    private void mergeConfig(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
            return;
        }
        try {
            FileConfiguration current = new YamlConfiguration();
            current.load(file);

            FileConfiguration defaults = new YamlConfiguration();
            try (InputStreamReader reader =
                         new InputStreamReader(Objects.requireNonNull(getResource(fileName)), StandardCharsets.UTF_8)) {
                defaults.load(reader);
            }

            boolean changed = false;
            for (String key : defaults.getKeys(true)) {
                if (!current.contains(key)) {
                    current.set(key, defaults.get(key));
                    getLogger().info("[Config] Added missing key: " + key + " to " + fileName);
                    changed = true;
                }
            }
            if (changed) {
                current.save(file);
                getLogger().info("[Config] " + fileName + " updated with new defaults.");
            }
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("Failed to merge " + fileName + ": " + e.getMessage());
        }
    }

    // --------------------
    // README generator
    // --------------------
    private void generateReadme() {
        try {
            File readme = new File(getDataFolder(), "README.md");
            FileConfiguration cfg = getConfig();

            StringBuilder sb = new StringBuilder();
            sb.append("# 🛡 ProShield Documentation\n\n");
            sb.append("**Version:** ").append(getDescription().getVersion()).append("\n");
            sb.append("**Generated:** ")
              .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
              .append("\n\n");

            sb.append("## 🚀 Quick Start\n");
            sb.append("1. Use `/proshield claim` to protect your current chunk.\n");
            sb.append("2. Manage trusted players with `/trust <player>` or the Roles GUI.\n");
            sb.append("3. Admins use `/proshield admin` for tools (reload/debug/bypass/world controls).\n\n");

            sb.append("## 📖 Permissions Overview\n\n");
            sb.append("- `proshield.player.access` → Player features (claim, unclaim, info, roles, compass).\n");
            sb.append("- `proshield.admin` → Admin tools: reload, debug, bypass, admin GUI.\n");
            sb.append("- `proshield.admin.expansions` → Approve/Deny expansion requests.\n");
            sb.append("- `proshield.admin.worldcontrols` → Manage per-world controls.\n\n");

            sb.append("## 🧭 GUI Menus\n");
            if (cfg.isConfigurationSection("gui.menus")) {
                for (String menu : cfg.getConfigurationSection("gui.menus").getKeys(false)) {
                    sb.append("### ").append(menu).append("\n");
                    String title = cfg.getString("gui.menus." + menu + ".title", "Untitled");
                    sb.append("- **Title:** ").append(title).append("\n");
                    if (cfg.isConfigurationSection("gui.menus." + menu + ".items")) {
                        for (String slot : cfg.getConfigurationSection("gui.menus." + menu + ".items").getKeys(false)) {
                            String name = cfg.getString("gui.menus." + menu + ".items." + slot + ".name", "Unnamed");
                            String perm = cfg.getString("gui.menus." + menu + ".items." + slot + ".permission", "None");
                            sb.append("  - ").append(name).append(" → Requires `").append(perm).append("`\n");
                        }
                    }
                    sb.append("\n");
                }
            }

            Files.writeString(readme.toPath(), sb.toString(), StandardCharsets.UTF_8);
            getLogger().info("Generated README.md");
        } catch (Exception e) {
            getLogger().severe("Failed to generate README.md: " + e.getMessage());
        }
    }

    // --------------------
    // Join: give compass
    // --------------------
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (getConfig().getBoolean("settings.give-compass-on-join", true)) {
            compassManager.giveCompass(event.getPlayer());
        }
    }

    // --------------------
    // Getters & toggles
    // --------------------
    public static ProShield getInstance() { return instance; }
    public MessagesUtil getMessagesUtil() { return messages; }
    public GUIManager getGuiManager() { return guiManager; }
    public ClaimRoleManager getRoleManager() { return roleManager; }
    public PlotManager getPlotManager() { return plotManager; }
    public CompassManager getCompassManager() { return compassManager; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }
    public Set<UUID> getBypassing() { return bypassing; }
    public boolean isBypassing(UUID uuid) { return bypassing.contains(uuid); }
    public boolean isDebugEnabled() { return debugEnabled; }
    public void toggleDebug() { debugEnabled = !debugEnabled; }
}
