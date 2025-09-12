// src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.*;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.gui.listeners.RoleFlagsListener;
import com.snazzyatoms.proshield.gui.listeners.RolesListener;
import com.snazzyatoms.proshield.gui.listeners.TrustListener;
import com.snazzyatoms.proshield.gui.listeners.UntrustListener;
import com.snazzyatoms.proshield.plots.*;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private MessagesUtil messages;
    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(plotManager);
        this.guiManager = new GUIManager(this, new GUICache(), roleManager);
        this.messages = new MessagesUtil(this);

        // Commands
        getCommand("trust").setExecutor(new TrustCommand(this, plotManager, roleManager));
        getCommand("untrust").setExecutor(new UntrustCommand(this, plotManager, roleManager));
        getCommand("roles").setExecutor(new RolesCommand(this, plotManager, roleManager, guiManager));
        getCommand("flags").setExecutor(new FlagsCommand(this, guiManager));
        getCommand("transfer").setExecutor(new TransferCommand(this, plotManager, roleManager));

        // Listeners
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(this, plotManager, roleManager), this);
        getServer().getPluginManager().registerEvents(new ItemProtectionListener(plotManager, roleManager, messages), this);
        getServer().getPluginManager().registerEvents(new InteractionProtectionListener(this, plotManager, roleManager), this);
        getServer().getPluginManager().registerEvents(new TrustListener(this, plotManager, roleManager, guiManager), this);
        getServer().getPluginManager().registerEvents(new UntrustListener(this, plotManager, roleManager, guiManager), this);
        getServer().getPluginManager().registerEvents(new RolesListener(this, roleManager), this);
        getServer().getPluginManager().registerEvents(new RoleFlagsListener(this, roleManager), this);

        getLogger().info("ProShield enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ProShield disabled.");
    }

    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public MessagesUtil getMessagesUtil() {
        return messages;
    }
}
