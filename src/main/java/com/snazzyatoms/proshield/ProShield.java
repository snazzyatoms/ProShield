// path: src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import com.snazzyatoms.proshield.util.ClaimPreviewTask;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProShield extends JavaPlugin {

    /* ===== singletons / managers ===== */
    private static ProShield instance;

    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;

    private boolean debug = false;

    public static ProShield getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        getLogger().info("[Debug] set to: " + debug);
    }

    @Override
    public void onEnable() {
        instance = this;

        // Ensure default config present
        saveDefaultConfig();

        // Managers
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this, plotManager);
        plotManager.setRoleManager(roleManager);

        guiManager = new GUIManager(this, plotManager);
        guiManager.registerCompassRecipe(); // exists in our GUIManager

        // Util init
        ClaimPreviewTask.init(this);

        // Command
        final PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            cmd.setExecutor(new ProShieldCommand(this, plotManager));
            cmd.setTabCompleter(new ProShieldCommand(this, plotManager));
        }

        // Listeners (match current ctor signatures)
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new GUIListener(plotManager, guiManager), this);
        pm.registerEvents(new BlockProtectionListener(plotManager), this);
        pm.registerEvents(new PvpProtectionListener(plotManager), this);
        pm.registerEvents(new PlayerDamageProtectionListener(this, plotManager), this);
        pm.registerEvents(new ItemProtectionListener(this, plotManager), this);
        pm.registerEvents(new KeepDropsListener(this, plotManager), this);
        pm.registerEvents(new EntityDamageProtectionListener(this, plotManager), this);
        pm.registerEvents(new PlayerJoinListener(this, plotManager, guiManager), this);
        pm.registerEvents(new ClaimMessageListener(plotManager, roleManager), this); // <-- FIX: pass roleManager too

        getLogger().info("ProShield " + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (plotManager != null) {
            plotManager.saveAll();
        }
        getLogger().info("ProShield disabled.");
    }

    /** Called by /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        if (plotManager != null) plotManager.reloadFromConfig();
        if (guiManager != null) guiManager.onConfigReload(); // exists in our GUIManager
    }
}
