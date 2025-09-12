// src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.*;
import com.snazzyatoms.proshield.compass.CompassManager;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.gui.listeners.RoleFlagsListener;
import com.snazzyatoms.proshield.gui.listeners.RolesListener;
import com.snazzyatoms.proshield.gui.listeners.TrustListener;
import com.snazzyatoms.proshield.gui.listeners.UntrustListener;
import com.snazzyatoms.proshield.plots.*;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;

    private MessagesUtil messages;
    private GUIManager guiManager;
    private GUICache guiCache;
    private ClaimRoleManager roleManager;
    private PlotManager plotManager;
    private CompassManager compassManager;

    // Tasks
    private EntityMobRepelTask mobRepelTask;
    private EntityBorderRepelTask borderRepelTask;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.messages = new MessagesUtil(this);
        this.guiCache = new GUICache();
        this.roleManager = new ClaimRoleManager(null); // temp, set below
        this.plotManager = new PlotManager(this, roleManager);
        this.roleManager = new ClaimRoleManager(plotManager);
        this.guiManager = new GUIManager(this, guiCache, roleManager);
        this.compassManager = new CompassManager(this, plotManager, guiManager);

        registerCommands();
        registerListeners();

        getLogger().info("✅ ProShield v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ ProShield disabled.");
    }

    private void registerCommands() {
        register("proshield", new ProShieldCommand(this, plotManager, guiManager, compassManager));
        register("claim", new ClaimCommand(this, plotManager));
        register("unclaim", new UnclaimCommand(this, plotManager, roleManager));
        register("trust", new TrustCommand(this, plotManager, roleManager));
        register("untrust", new UntrustCommand(this, plotManager, roleManager));
        register("transfer", new TransferCommand(this, plotManager, roleManager));
        register("roles", new RolesCommand(this, plotManager, roleManager, guiManager));
        register("flags", new FlagsCommand(this, guiManager));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, plotManager, roleManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new RolesListener(this, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new TrustListener(this, plotManager, roleManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new UntrustListener(this, plotManager, roleManager, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new RoleFlagsListener(this, roleManager), this);

        // Protection listeners
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager, roleManager, messages), this);
        Bukkit.getPluginManager().registerEvents(new InteractionProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new BucketProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plotManager), this);

        // Tasks
        this.mobRepelTask = new EntityMobRepelTask(plotManager);
        this.borderRepelTask = new EntityBorderRepelTask(plotManager);

        mobRepelTask.runTaskTimer(this, 20L, 20L);
        borderRepelTask.runTaskTimer(this, 20L, 20L);
    }

    private void register(String name, Object exec) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null && exec instanceof org.bukkit.command.CommandExecutor executor) {
            cmd.setExecutor(executor);
        }
    }

    /* -------------------------------------------------------
     * Getters
     * ------------------------------------------------------- */

    public static ProShield getInstance() {
        return instance;
    }

    public MessagesUtil getMessagesUtil() {
        return messages;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public GUICache getGuiCache() {
        return guiCache;
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
}
