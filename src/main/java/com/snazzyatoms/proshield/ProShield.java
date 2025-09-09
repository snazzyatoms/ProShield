// path: src/main/java/com/snazzyatoms/proshield/ProShield.java
package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import com.snazzyatoms.proshield.util.ClaimPreviewTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProShield extends JavaPlugin {

    // ---- Singleton (kept small to satisfy older refs like ProShield.getInstance()) ----
    private static ProShield INSTANCE;
    public static ProShield getInstance() { return INSTANCE; }

    // ---- Core services ----
    private PlotManager plotManager;
    private GUIManager guiManager;

    // Debug toggle (referenced by GUI/commands)
    private boolean debug;

    public PlotManager plots() { return plotManager; }
    public GUIManager gui() { return guiManager; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean v) { debug = v; }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Ensure defaults exist (and write version into config)
        getConfig().options().copyDefaults(true);
        getConfig().set("version", getDescription().getVersion());
        saveDefaultConfig();
        saveConfig();

        // Managers
        plotManager = new PlotManager(this);
        guiManager  = new GUIManager(this, plotManager); // NOTE: constructor requires (ProShield, PlotManager)

        // --- Listeners (match each class’ constructor signature) ---
        // GUI
        Bukkit.getPluginManager().registerEvents(new GUIListener(plotManager, guiManager), this);

        // Protections
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageProtectionListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(plotManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, plotManager, guiManager), this);

        // Claim border preview scheduler support (safe to have available)
        ClaimPreviewTask.init(this);

        // Register command executor
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plotManager));

        // Crafting (if you provide it from GUIManager)
        guiManager.registerCompassRecipe();

        // Log
        getLogger().info(() -> "ProShield " + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        try {
            if (plotManager != null) plotManager.saveAll();
        } catch (Exception ignored) {}
        getLogger().info("ProShield disabled.");
    }

    // Called from /proshield reload and by GUI admin toggles
    public void reloadAllConfigs() {
        reloadConfig();
        getConfig().set("version", getDescription().getVersion()); // keep version in file
        saveConfig();

        // Push new state everywhere
        if (plotManager != null) plotManager.reloadFromConfig();
        if (guiManager != null)   guiManager.onConfigReload();
    }
}
