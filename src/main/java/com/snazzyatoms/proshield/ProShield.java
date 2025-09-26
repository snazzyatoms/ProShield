// src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.listeners.MobControlTasks;
import com.snazzyatoms.proshield.plots.ClaimProtectionListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
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
    private CompassManager compassManager;

    private final Set<UUID> bypassing = new HashSet<>();
    private boolean debugEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        // =====================
        // 📌 Core init
        // =====================
        saveDefaultConfig();
        generateMessagesFile(); // ensure messages.yml or localized messages_xx.yml exists

        messages                = new MessagesUtil(this);
        plotManager             = new PlotManager(this);
        roleManager             = new ClaimRoleManager(this);
        expansionRequestManager = new ExpansionRequestManager(this);
        guiManager              = new GUIManager(this);
        compassManager          = new CompassManager(this); // handles join + give

        // =====================
        // 📌 Listener registration
        // =====================
        getServer().getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        getServer().getPluginManager().registerEvents(new ClaimProtectionListener(this, plotManager), this);

        // Mob control tasks self-register inside constructor
        new MobControlTasks(this);

        // =====================
        // 📌 Command registration
        // =====================
        PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            ProShieldCommand dispatcher = new ProShieldCommand(this, compassManager);
            cmd.setExecutor(dispatcher);
            cmd.setTabCompleter(dispatcher);
        } else {
            getLogger().warning("[ProShield] Command /proshield not found in plugin.yml!");
        }

        getLogger().info("[ProShield] Enabled v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (plotManager != null) plotManager.saveAll();
        if (expansionRequestManager != null) expansionRequestManager.save();

        getLogger().info("[ProShield] Disabled cleanly.");
    }

    // ==========================
    // 🔧 Helpers & Getters
    // ==========================

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

    public CompassManager getCompassManager() {
        return compassManager;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void toggleDebug() {
        debugEnabled = !debugEnabled;
    }

    public boolean toggleBypass(UUID uuid) {
        if (bypassing.contains(uuid)) {
            bypassing.remove(uuid);
            return false;
        } else {
            bypassing.add(uuid);
            return true;
        }
    }

    public boolean isBypassing(Player player) {
        return bypassing.contains(player.getUniqueId());
    }

    public Set<UUID> getBypassing() {
        return bypassing;
    }

    /** Reload messages.yml safely */
    public void loadMessagesConfig() {
        messages.reload();
    }

    /**
     * Ensure messages.yml (or localized messages_xx.yml) exists in plugin folder.
     */
    private void generateMessagesFile() {
        String lang = getConfig().getString("settings.language", "en").toLowerCase();
        String fileName = lang.equals("en") ? "messages.yml" : "messages_" + lang + ".yml";

        File langFile = new File(getDataFolder(), fileName);
        if (!langFile.exists()) {
            saveResource(fileName, false); // Copies from plugin JAR into /plugins/ProShield/
            getLogger().info("[ProShield] Generated " + fileName + " for language: " + lang);
        }
    }
}
