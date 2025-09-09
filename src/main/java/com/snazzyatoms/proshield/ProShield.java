package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;

public class ProShield extends JavaPlugin {

    // -------- Static instance for convenience (used by various listeners)
    private static ProShield instance;
    public static ProShield getInstance() { return instance; }

    // -------- Core managers
    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;

    // -------- Runtime flags
    private boolean debug = false;

    // Expose managers for other classes if needed
    public PlotManager getPlotManager() { return plotManager; }
    public ClaimRoleManager getRoleManager() { return roleManager; }
    public GUIManager getGuiManager() { return guiManager; }

    public boolean isDebug() { return debug; }
    public void setDebug(boolean value) {
        this.debug = value;
        getLogger().info("[Debug] set to " + value);
    }

    @Override
    public void onEnable() {
        instance = this;

        // Load & migrate config once
        saveDefaultConfig();
        tinyMigrationsTo124();
        this.debug = getConfig().getBoolean("proshield.debug", false);

        // Managers
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this); // current signature requires (ProShield)
        this.guiManager  = new GUIManager(this, plotManager);

        // Listeners (all features wired)
        registerListener(new GUIListener(plotManager, guiManager));
        registerListener(new BlockProtectionListener(plotManager));
        registerListener(new PvpProtectionListener(plotManager));
        registerListener(new ItemProtectionListener(plotManager));
        registerListener(new KeepDropsListener(plotManager));
        registerListener(new EntityDamageProtectionListener(plotManager));
        registerListener(new PlayerJoinListener(this, plotManager, guiManager));
        registerListener(new PlayerDamageProtectionListener(this, plotManager));
        registerListener(new ClaimMessageListener(plotManager, roleManager));

        // Commands
        var cmd = getCommand("proshield");
        if (cmd != null) {
            ProShieldCommand executor = new ProShieldCommand(this, plotManager, guiManager);
            cmd.setExecutor(executor);
            if (executor instanceof TabCompleter) {
                cmd.setTabCompleter((TabCompleter) executor);
            }
        } else {
            getLogger().warning("Command 'proshield' not found in plugin.yml!");
        }

        getLogger().info("ProShield v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        try {
            if (plotManager != null) plotManager.saveAll();
        } catch (Exception ignored) {}
        getLogger().info("ProShield disabled.");
    }

    /** Public entry used by /proshield reload */
    public void reloadAllConfigs() {
        reloadConfig();
        tinyMigrationsTo124();
        this.debug = getConfig().getBoolean("proshield.debug", false);

        if (plotManager != null) plotManager.reloadFromConfig();
        if (guiManager  != null) guiManager.onConfigReload();

        getLogger().info("Configuration reloaded.");
    }

    private void registerListener(Listener l) {
        Bukkit.getPluginManager().registerEvents(l, this);
    }

    /**
     * Minimal & safe migrations for 1.2.4:
     * - Force version: "1.2.4"
     * - Ensure claims.keep-items block exists and despawn range is sane
     * - Ensure protection.damage block (owner/trusted protection)
     * - Ensure spawn deny settings exist
     * - Ensure GUI slot blocks exist (so GUIManager finds them)
     */
    private void tinyMigrationsTo124() {
        // version bump
        getConfig().set("version", "1.2.4");
        if (!getConfig().isSet("messages.prefix")) {
            getConfig().set("messages.prefix", "&3[ProShield]&r ");
        }

        // keep-items (added in 1.2.2)
        if (!getConfig().isConfigurationSection("claims.keep-items")) {
            getConfig().set("claims.keep-items.enabled", false);
            getConfig().set("claims.keep-items.despawn-seconds", 900);
        } else {
            int sec = Math.max(300, Math.min(900, getConfig().getInt("claims.keep-items.despawn-seconds", 900)));
            getConfig().set("claims.keep-items.despawn-seconds", sec);
        }

        // Damage protection inside claims (1.2.4)
        if (!getConfig().isConfigurationSection("protection.damage")) {
            getConfig().set("protection.damage.enabled", true);
            getConfig().set("protection.damage.protect-owner-and-trusted", true);
            getConfig().set("protection.damage.cancel-all", true);
            // granular (used if cancel-all=false)
            getConfig().set("protection.damage.pve", true);
            getConfig().set("protection.damage.projectiles", true);
            getConfig().set("protection.damage.environment", true);
            getConfig().set("protection.damage.fire-lava", true);
            getConfig().set("protection.damage.fall", true);
            getConfig().set("protection.damage.explosions", true);
            getConfig().set("protection.damage.drown-void-suffocate", true);
            getConfig().set("protection.damage.poison-wither", true);
        }

        // Spawn / hub no-claim zones (1.2.4)
        if (!getConfig().isConfigurationSection("spawn-protection")) {
            getConfig().set("spawn-protection.enabled", true);
            getConfig().set("spawn-protection.radius", 24); // blocks around world spawn
            getConfig().set("spawn-protection.allow-ops", true); // ops may still claim if true
        }

        // Compass fallback
        if (!getConfig().isSet("compass.drop-if-full")) {
            getConfig().set("compass.drop-if-full", true);
        }

        // GUI slots
        ensureGuiSlots();

        // Protection master switches (read by admin GUI)
        if (!getConfig().isConfigurationSection("protection.fire")) {
            getConfig().set("protection.fire.enabled", true);
        }
        if (!getConfig().isConfigurationSection("protection.explosions")) {
            getConfig().set("protection.explosions.enabled", true);
        }
        if (!getConfig().isConfigurationSection("protection.entity-grief")) {
            getConfig().set("protection.entity-grief.enabled", true);
        }
        if (!getConfig().isSet("protection.pvp-in-claims")) {
            // false = PvP blocked in claims by default
            getConfig().set("protection.pvp-in-claims", false);
        }

        saveConfig();
    }

    private void ensureGuiSlots() {
        // main
        ensurePath("gui.slots.main.create", 11);
        ensurePath("gui.slots.main.info", 13);
        ensurePath("gui.slots.main.remove", 15);
        ensurePath("gui.slots.main.admin", 33);
        ensurePath("gui.slots.main.help", 49);
        ensurePath("gui.slots.main.back", 48);

        // extended player actions
        ensurePath("gui.slots.main.trust", 20);
        ensurePath("gui.slots.main.roles", 21);
        ensurePath("gui.slots.main.transfer", 22);
        ensurePath("gui.slots.main.preview", 23);
        ensurePath("gui.slots.main.settings", 24);

        // admin
        ensurePath("gui.slots.admin.explosions", 10);
        ensurePath("gui.slots.admin.fire", 12);
        ensurePath("gui.slots.admin.entity-grief", 14);
        ensurePath("gui.slots.admin.pvp", 16);
        ensurePath("gui.slots.admin.keep-items", 28);
        ensurePath("gui.slots.admin.toggle-drop-if-full", 20);
        ensurePath("gui.slots.admin.help", 22);
        ensurePath("gui.slots.admin.back", 31);
    }

    private void ensurePath(String path, Object defVal) {
        if (!getConfig().isSet(path)) {
            getConfig().set(path, defVal);
        }
    }
}
