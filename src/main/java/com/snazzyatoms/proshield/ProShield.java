package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.compass.CompassListener;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.ChatListener;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.listeners.ProtectionListener;
import com.snazzyatoms.proshield.plots.PlotListener;
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

    private MessagesUtil messagesUtil;
    private FileConfiguration messagesConfig; // messages.yml (merged with defaults)

    private GUIManager guiManager;
    private ClaimRoleManager roleManager;
    private PlotManager plotManager;
    private CompassManager compassManager;

    // Admin bypass/debug
    private final Set<UUID> bypassing = new HashSet<>();
    private boolean debugEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        // Ensure data folder & write defaults
        saveDefaultConfig();
        mergeConfig("config.yml");
        loadMessagesConfig(); // creates/merges messages.yml

        // Core utilities
        messagesUtil = new MessagesUtil(this, messagesConfig);

        // Managers
        plotManager   = new PlotManager(this);
        roleManager   = new ClaimRoleManager(this);
        guiManager    = new GUIManager(this);
        compassManager= new CompassManager(this, guiManager);

        // Load persisted data
        roleManager.loadAll();
        plotManager.loadAll();

        // Commands
        PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            ProShieldCommand executor = new ProShieldCommand(this, guiManager, plotManager, messagesUtil);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // Listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new CompassListener(this), this); // right-click compass → open GUI
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(this), this); // explosions, fire, ignite
        Bukkit.getPluginManager().registerEvents(new PlotListener(this, plotManager), this); // block place/break
        Bukkit.getPluginManager().registerEvents(this, this); // join-event below

        // Generate README from config → items and permissions always in sync
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

    // ---------------------------------------------------------------------
    // messages.yml loader/merger (PUBLIC so commands can force reload cleanly)
    // ---------------------------------------------------------------------
    public void loadMessagesConfig() {
        try {
            if (!getDataFolder().exists()) {
                // noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "messages.yml");
            if (!file.exists()) {
                saveResource("messages.yml", false);
            }

            YamlConfiguration current = new YamlConfiguration();
            current.load(file);

            // Merge defaults from the jar
            YamlConfiguration defaults = new YamlConfiguration();
            try (InputStreamReader reader =
                         new InputStreamReader(Objects.requireNonNull(getResource("messages.yml")), StandardCharsets.UTF_8)) {
                defaults.load(reader);
            }

            boolean changed = false;
            for (String key : defaults.getKeys(true)) {
                if (!current.contains(key)) {
                    current.set(key, defaults.get(key));
                    getLogger().info("[Messages] Added missing key: " + key);
                    changed = true;
                }
            }
            if (changed) {
                current.save(file);
                getLogger().info("[Messages] messages.yml updated with new defaults.");
            }
            messagesConfig = current;
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("Failed to load/merge messages.yml: " + e.getMessage());
            messagesConfig = getConfig(); // worst-case fallback to avoid NPEs
        }
    }

    // --------------------
    // Auto-Merge Config
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
    // README.md Generator
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
            sb.append("2. Manage trusted players with `/trust <player> [role]` or via the GUI.\n");
            sb.append("3. Admins: `/proshield admin` for Admin Tools.\n\n");

            sb.append("## 📖 Permissions Overview\n\n");
            sb.append("- `proshield.player.access` → Basic player tools (claims, roles, compass).\n");
            sb.append("- `proshield.admin` → Admin tools (reload, debug, bypass, admin GUI).\n");
            sb.append("- `proshield.admin.expansions` → Expansion requests (approve/deny).\n");
            sb.append("- `proshield.admin.worldcontrols` → Global/world protection controls.\n\n");

            sb.append("## 🧭 GUI Menus & Required Permissions\n");
            if (cfg.isConfigurationSection("gui.menus")) {
                for (String menu : Objects.requireNonNull(cfg.getConfigurationSection("gui.menus")).getKeys(false)) {
                    String base = "gui.menus." + menu;
                    String title = cfg.getString(base + ".title", "Untitled");
                    sb.append("\n### ").append(menu).append("\n");
                    sb.append("- **Title:** ").append(title).append("\n");
                    if (cfg.isConfigurationSection(base + ".items")) {
                        sb.append("- **Items:**\n");
                        for (String slot : Objects.requireNonNull(cfg.getConfigurationSection(base + ".items")).getKeys(false)) {
                            String iBase = base + ".items." + slot;
                            String name = cfg.getString(iBase + ".name", "Unnamed");
                            String perm = cfg.getString(iBase + ".permission", "none");
                            sb.append("  - ").append(name).append(" → Requires `").append(perm).append("`\n");
                        }
                    } else {
                        sb.append("- *(No items configured)*\n");
                    }
                }
            } else {
                sb.append("\n*(No menus configured in config.yml)*\n");
            }
            sb.append("\n");

            // Write using Java NIO (no external deps)
            Files.writeString(readme.toPath(), sb.toString(), StandardCharsets.UTF_8);
            getLogger().info("Generated README.md");
        } catch (Exception e) {
            getLogger().severe("Failed to generate README.md: " + e.getMessage());
        }
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
    // Getters (used across plugin)
    // --------------------
    public static ProShield getInstance() { return instance; }

    public MessagesUtil getMessagesUtil() { return messagesUtil; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }

    public GUIManager getGuiManager() { return guiManager; }
    public ClaimRoleManager getRoleManager() { return roleManager; }
    public PlotManager getPlotManager() { return plotManager; }

    public CompassManager getCompassManager() { return compassManager; }

    public Set<UUID> getBypassing() { return bypassing; }
    public boolean isBypassing(UUID uuid) { return bypassing.contains(uuid); }

    public boolean isDebugEnabled() { return debugEnabled; }
    public void toggleDebug() { debugEnabled = !debugEnabled; }
}
