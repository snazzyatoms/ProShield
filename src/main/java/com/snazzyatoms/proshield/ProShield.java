// path: src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.BlockProtectionListener;
import com.snazzyatoms.proshield.plots.ClaimMessageListener;
import com.snazzyatoms.proshield.plots.ClaimRoleManager;
import com.snazzyatoms.proshield.plots.EntityDamageProtectionListener;
import com.snazzyatoms.proshield.plots.ItemProtectionListener;
import com.snazzyatoms.proshield.plots.KeepDropsListener;
import com.snazzyatoms.proshield.plots.PlayerDamageProtectionListener;
import com.snazzyatoms.proshield.plots.PlayerJoinListener;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PvpProtectionListener;
import com.snazzyatoms.proshield.util.ClaimPreviewTask;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProShield extends JavaPlugin {

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

        // Ensure default config exists and has current version header
        saveDefaultConfig();
        getConfig().set("version", getDescription().getVersion());
        saveConfig();

        // Core managers
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this);           // <— FIX: only plugin arg
        plotManager.setRoleManager(roleManager);

        guiManager = new GUIManager(this, plotManager);
        guiManager.registerCompassRecipe();

        // Preview utility
        ClaimPreviewTask.init(this);

        // Commands
        PluginCommand cmd = getCommand("proshield");
        if (cmd != null) {
            cmd.setExecutor(new ProShieldCommand(this, plotManager));
            // If you add a real TabCompleter class, register it here:
            // cmd.setTabCompleter(new ProShieldTabCompleter(this, plotManager));
        }

        // Listeners — ctor signatures aligned to your classes
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new GUIListener(plotManager, guiManager), this);
        pm.registerEvents(new BlockProtectionListener(plotManager), this);
        pm.registerEvents(new PvpProtectionListener(plotManager), this);
        pm.registerEvents(new PlayerDamageProtectionListener(this, plotManager), this);
        pm.registerEvents(new ItemProtectionListener(plotManager), this);             // <— FIX: PlotManager only
        pm.registerEvents(new KeepDropsListener(plotManager), this);                  // <— FIX: PlotManager only
        pm.registerEvents(new EntityDamageProtectionListener(plotManager), this);     // <— FIX: PlotManager only
        pm.registerEvents(new PlayerJoinListener(this, plotManager, guiManager), this);
        pm.registerEvents(new ClaimMessageListener(plotManager, roleManager), this);  // <— FIX: needs roleManager

        getLogger().info("ProShield " + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (plotManager != null) {
            plotManager.saveAll();
        }
        getLogger().info("ProShield disabled.");
    }

    /** Hook for /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        // maintain version header on reload
        getConfig().set("version", getDescription().getVersion());
        saveConfig();

        if (plotManager != null) plotManager.reloadFromConfig();
        if (guiManager != null) guiManager.onConfigReload();
    }
}
