package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.*;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.gui.listeners.*;
import com.snazzyatoms.proshield.plots.*;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private MessagesUtil messages;
    private GUIManager guiManager;
    private ClaimRoleManager roleManager;
    private PlotManager plotManager;
    private GUICache guiCache;

    private boolean debugEnabled = false;

    public static ProShield getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.messages = new MessagesUtil(this);
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(plotManager);
        this.guiCache = new GUICache();
        this.guiManager = new GUIManager(this, guiCache, roleManager);

        registerCommands();
        registerListeners();

        getLogger().info("[ProShield] Enabled v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("[ProShield] Disabled.");
    }

    private void registerCommands() {
        getCommand("proshield").setExecutor(new ProShieldCommand(this));
        getCommand("claim").setExecutor(new ClaimCommand(this, plotManager));
        getCommand("unclaim").setExecutor(new UnclaimCommand(this, plotManager, roleManager));
        getCommand("trust").setExecutor(new TrustCommand(this, plotManager, roleManager));
        getCommand("untrust").setExecutor(new UntrustCommand(this, plotManager, roleManager));
        getCommand("transfer").setExecutor(new TransferCommand(this, plotManager, roleManager));
        getCommand("roles").setExecutor(new RolesCommand(this, plotManager, roleManager, guiManager));
        getCommand("flags").setExecutor(new FlagsCommand(this, guiManager));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new TrustListener(this, plotManager, roleManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new UntrustListener(this, plotManager, roleManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new RolesListener(this, roleManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new RoleFlagsListener(this, roleManager), this);

        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager, roleManager, messages), this);
        Bukkit.getPluginManager().registerEvents(new InteractionProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new BucketProtectionListener(plotManager, roleManager, messages), this);
    }

    /* ====================================================
     * ACCESSORS (for other classes)
     * ==================================================== */
    public MessagesUtil getMessagesUtil() {
        return messages;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public GUICache getGuiCache() {
        return guiCache;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    /* ====================================================
     * Debug toggle
     * ==================================================== */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void toggleDebug() {
        this.debugEnabled = !this.debugEnabled;
        getLogger().info("[ProShield] Debug mode: " + (debugEnabled ? "ON" : "OFF"));
    }
}
