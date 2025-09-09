package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private PlotManager plots;
    private GUIManager guiManager;

    // simple debug flag stored in memory + config
    private boolean debug;

    @Override
    public void onEnable() {
        // Config boot
        saveDefaultConfig();
        ensureConfigVersion("1.2.4");

        FileConfiguration cfg = getConfig();
        this.debug = cfg.getBoolean("proshield.debug", false);

        // Core managers
        this.plots = new PlotManager(this);
        this.guiManager = new GUIManager(plots); // GUIManager takes ONLY PlotManager

        // Listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, plots, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, plots, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageProtectionListener(this, plots), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(this, plots), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(this, plots), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(this, plots), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(this, plots), this);

        // Commands
        getCommand("proshield").setExecutor(new ProShieldCommand(this, plots, guiManager));

        getLogger().info("ProShield enabled. v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (plots != null) plots.saveAll();
    }

    /** Ensure config version header and add any missing defaults for 1.2.4 */
    private void ensureConfigVersion(String want) {
        String have = getConfig().getString("version", "0.0.0");
        if (!want.equals(have)) {
            getConfig().set("version", want);
            // make sure the new damage protection & spawn protection keys exist
            getConfig().addDefault("protection.damage.enabled", true);
            getConfig().addDefault("protection.damage.protect-owner-and-trusted", true);
            getConfig().addDefault("protection.damage.cancel-all", true);
            getConfig().addDefault("spawn.no-claim.enabled", false);
            getConfig().addDefault("spawn.no-claim.radius", 32);
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
    }

    // Debug convenience used by GUI & command
    public boolean isDebug() { return debug; }
    public void setDebug(boolean v) {
        this.debug = v;
        getConfig().set("proshield.debug", v);
        saveConfig();
    }

    public PlotManager getPlots() { return plots; }
    public GUIManager getGuiManager() { return guiManager; }
}
