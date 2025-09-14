// src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.AdminGUIListener;
import com.snazzyatoms.proshield.gui.ChatListener;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.AdminGUIManager;
import com.snazzyatoms.proshield.plots.PlotManager;
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
    private AdminGUIManager adminGuiManager;
    private ClaimRoleManager roleManager;
    private PlotManager plotManager;

    private final Set<UUID> bypassing = new HashSet<>();
    private boolean debugEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        messages = new MessagesUtil(this);
        guiManager = new GUIManager(this);
        adminGuiManager = new AdminGUIManager(this);
        roleManager = new ClaimRoleManager();
        plotManager = new PlotManager(this);

        // --- Register command ---
        PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            cmd.setExecutor(new ProShieldCommand(this, guiManager, plotManager, messages));
        }

        // --- Register listeners ---
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new AdminGUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);

        getLogger().info("✅ ProShield enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ ProShield disabled.");
    }

    // --- Getters ---
    public static ProShield getInstance() {
        return instance;
    }

    public MessagesUtil getMessagesUtil() {
        return messages;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public AdminGUIManager getAdminGuiManager() {
        return adminGuiManager;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public Set<UUID> getBypassing() {
        return bypassing;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void toggleDebug() {
        this.debugEnabled = !this.debugEnabled;
    }
}
