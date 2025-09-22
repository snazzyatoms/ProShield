// src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.PlayerCommandDispatcher;
import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
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
    private ExpansionRequestManager expansionRequestManager;
    private CompassManager compassManager;

    private final Set<UUID> bypassing = new HashSet<>();
    private boolean debugEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        messages = new MessagesUtil(this);
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this);
        expansionRequestManager = new ExpansionRequestManager(this);
        guiManager = new GUIManager(this);

        // Initialize CompassManager (handles compass creation/distribution)
        compassManager = new CompassManager(this, guiManager);

        // Register GUI listener (handles clicks + compass right-click)
        getServer().getPluginManager().registerEvents(new GUIListener(this, guiManager), this);

        // Register command dispatcher for fallback/admin commands
        new PlayerCommandDispatcher(this);

        // Register /proshield command
        PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            ProShieldCommand dispatcher = new ProShieldCommand(this, compassManager);
            cmd.setExecutor(dispatcher);
            cmd.setTabCompleter(dispatcher);
        }

        getLogger().info("[ProShield] Enabled v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        // Save data safely
        if (plotManager != null) plotManager.saveAll();
        if (expansionRequestManager != null) expansionRequestManager.save();

        getLogger().info("[ProShield] Disabled cleanly.");
    }

    /* -------------------
     * Helpers & Getters
     * ------------------- */

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
}
