package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    // ---- Static access (legacy-safe) ----
    private static ProShield INSTANCE;
    public static ProShield getInstance() { return INSTANCE; }

    // ---- Core managers ----
    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;

    // ---- Runtime flags ----
    private boolean debug;

    // ---- Accessors expected elsewhere ----
    public GUIManager getGuiManager() { return guiManager; }
    public PlotManager getPlotManager() { return plotManager; }
    public ClaimRoleManager getRoleManager() { return roleManager; }

    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Load + migrate config
        saveDefaultConfig();
        migrateConfigTo124();

        // Boot managers
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.plotManager.setRoleManager(roleManager);
        this.guiManager  = new GUIManager(this, plotManager);

        // Register commands
        ProShieldCommand command = new ProShieldCommand(this, plotManager, guiManager);
        if (getCommand("proshield") != null) {
            getCommand("proshield").setExecutor(command);
            try {
                getCommand("proshield").setTabCompleter(command);
            } catch (Throwable ignored) {}
        }

        // Register listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GUIListener(plotManager, guiManager), this);
        pm.registerEvents(new BlockProtectionListener(plotManager), this);
        pm.registerEvents(new PvpProtectionListener(plotManager), this);
        pm.registerEvents(new ItemProtectionListener(plotManager), this);
        pm.registerEvents(new KeepDropsListener(plotManager), this);
        pm.registerEvents(new EntityDamageProtectionListener(plotManager), this);
        pm.registerEvents(new PlayerDamageProtectionListener(this, plotManager), this);
        pm.registerEvents(new ClaimMessageListener(plotManager, roleManager), this);
        pm.registerEvents(new PlayerJoinListener(this, plotManager, guiManager), this);

        // NEW: Spawn guard + mob repel
        pm.registerEvents(new SpawnClaimGuardListener(plotManager), this);
        pm.registerEvents(new MobBorderRepelListener(plotManager), this);

        // Log ready
        getLogger().info("ProShield " + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        try {
            if (plotManager != null) plotManager.saveAll();
        } catch (Throwable ignored) {}
        INSTANCE = null;
    }

    /**
     * Reloads config and re-applies cached settings everywhere needed.
     * Safe to call from command and Admin GUI.
     */
    public void reloadAllConfigs() {
        reloadConfig();
        migrateConfigTo124();

        // Update debug flag from config (optional)
        this.debug = getConfig().getBoolean("proshield.debug", false);

        // Push into sub-systems that cache values
        try {
            if (guiManager != null) guiManager.onConfigReload();
        } catch (Throwable ignored) {}

        try {
            if (plotManager != null) plotManager.reloadFromConfig();
        } catch (Throwable ignored) {}

        try {
            if (roleManager != null) roleManager.reloadFromConfigSafe();
        } catch (Throwable ignored) {}

        getServer().getScheduler().runTask(this, () ->
            Bukkit.getOnlinePlayers().forEach(p ->
                p.sendMessage(color(getConfig().getString("messages.prefix", "&3[ProShield]&r ") +
                        "&aConfig reloaded.")))
        );
    }

    // --- Helpers ---

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Ensures config has 1.2.4 defaults and required nodes.
     * Non-destructive: only sets nodes if missing.
     */
    private void migrateConfigTo124() {
        var cfg = getConfig();

        // Version stamp
        cfg.set("version", "1.2.4");

        // Prefix
        if (!cfg.isSet("messages.prefix")) {
            cfg.set("messages.prefix", "&3[ProShield]&r ");
        }

        // Global limits
        if (!cfg.isSet("limits.max-claims")) {
            cfg.set("limits.max-claims", 5);
        }

        // Expiry
        if (!cfg.isSet("expiry.enabled")) cfg.set("expiry.enabled", false);
        if (!cfg.isSet("expiry.days"))    cfg.set("expiry.days", 30);

        // Spawn guard
        if (!cfg.isSet("spawn.block-claiming")) cfg.set("spawn.block-claiming", true);
        if (!cfg.isSet("spawn.radius"))         cfg.set("spawn.radius", 32);

        // Protection – Damage
        if (!cfg.isSet("protection.damage.enabled")) cfg.set("protection.damage.enabled", true);
        if (!cfg.isSet("protection.damage.protect-owner-and-trusted")) cfg.set("protection.damage.protect-owner-and-trusted", true);
        if (!cfg.isSet("protection.damage.cancel-all")) cfg.set("protection.damage.cancel-all", true);

        // PvP
        if (!cfg.isSet("protection.pvp-in-claims")) cfg.set("protection.pvp-in-claims", false);

        // Interactions
        if (!cfg.isSet("protection.interactions.enabled")) cfg.set("protection.interactions.enabled", true);
        if (!cfg.isSet("protection.interactions.mode"))    cfg.set("protection.interactions.mode", "blacklist");

        // Explosions
        if (!cfg.isSet("protection.explosions.enabled"))    cfg.set("protection.explosions.enabled", true);
        if (!cfg.isSet("protection.explosions.creeper"))    cfg.set("protection.explosions.creeper", true);
        if (!cfg.isSet("protection.explosions.tnt"))        cfg.set("protection.explosions.tnt", true);
        if (!cfg.isSet("protection.explosions.wither"))     cfg.set("protection.explosions.wither", true);
        if (!cfg.isSet("protection.explosions.wither_skull")) cfg.set("protection.explosions.wither_skull", true);
        if (!cfg.isSet("protection.explosions.end_crystal")) cfg.set("protection.explosions.end_crystal", true);
        if (!cfg.isSet("protection.explosions.ender_dragon")) cfg.set("protection.explosions.ender_dragon", true);

        // Fire
        if (!cfg.isSet("protection.fire.enabled")) cfg.set("protection.fire.enabled", true);
        if (!cfg.isSet("protection.fire.spread"))  cfg.set("protection.fire.spread", true);
        if (!cfg.isSet("protection.fire.burn"))    cfg.set("protection.fire.burn", true);
        if (!cfg.isSet("protection.fire.ignite.flint_and_steel")) cfg.set("protection.fire.ignite.flint_and_steel", true);
        if (!cfg.isSet("protection.fire.ignite.lava"))            cfg.set("protection.fire.ignite.lava", true);
        if (!cfg.isSet("protection.fire.ignite.lightning"))       cfg.set("protection.fire.ignite.lightning", true);
        if (!cfg.isSet("protection.fire.ignite.explosion"))       cfg.set("protection.fire.ignite.explosion", true);
        if (!cfg.isSet("protection.fire.ignite.spread"))          cfg.set("protection.fire.ignite.spread", true);

        // Buckets
        if (!cfg.isSet("protection.buckets.block-empty")) cfg.set("protection.buckets.block-empty", true);
        if (!cfg.isSet("protection.buckets.block-fill"))  cfg.set("protection.buckets.block-fill", true);

        // Entity grief
        if (!cfg.isSet("protection.entity-grief.enabled"))    cfg.set("protection.entity-grief.enabled", true);
        if (!cfg.isSet("protection.entity-grief.enderman"))   cfg.set("protection.entity-grief.enderman", true);
        if (!cfg.isSet("protection.entity-grief.ravager"))    cfg.set("protection.entity-grief.ravager", true);
        if (!cfg.isSet("protection.entity-grief.silverfish")) cfg.set("protection.entity-grief.silverfish", true);
        if (!cfg.isSet("protection.entity-grief.ender-dragon")) cfg.set("protection.entity-grief.ender-dragon", true);
        if (!cfg.isSet("protection.entity-grief.wither"))       cfg.set("protection.entity-grief.wither", true);

        // Enderman teleport
        if (!cfg.isSet("protection.entity-teleport.enderman")) cfg.set("protection.entity-teleport.enderman", true);

        // Entities inside claims
        if (!cfg.isSet("protection.entities.item-frames"))   cfg.set("protection.entities.item-frames", true);
        if (!cfg.isSet("protection.entities.armor-stands"))  cfg.set("protection.entities.armor-stands", true);
        if (!cfg.isSet("protection.entities.passive-animals")) cfg.set("protection.entities.passive-animals", true);
        if (!cfg.isSet("protection.entities.tamed-pets"))    cfg.set("protection.entities.tamed-pets", true);

        // Mob control inside claims
        if (!cfg.isSet("protection.mobs.block-spawn"))                cfg.set("protection.mobs.block-spawn", true);
        if (!cfg.isSet("protection.mobs.allow-spawner-spawn"))        cfg.set("protection.mobs.allow-spawner-spawn", true);
        if (!cfg.isSet("protection.mobs.border-repel.enabled"))       cfg.set("protection.mobs.border-repel.enabled", true);
        if (!cfg.isSet("protection.mobs.border-repel.radius"))        cfg.set("protection.mobs.border-repel.radius", 1.5D);
        if (!cfg.isSet("protection.mobs.border-repel.horizontal-push")) cfg.set("protection.mobs.border-repel.horizontal-push", 0.6D);
        if (!cfg.isSet("protection.mobs.border-repel.vertical-push"))   cfg.set("protection.mobs.border-repel.vertical-push", 0.15D);
        if (!cfg.isSet("protection.mobs.border-repel.interval-ticks"))  cfg.set("protection.mobs.border-repel.interval-ticks", 20);

        // Claims – keep items
        if (!cfg.isSet("claims.keep-items.enabled"))         cfg.set("claims.keep-items.enabled", false);
        if (!cfg.isSet("claims.keep-items.despawn-seconds")) cfg.set("claims.keep-items.despawn-seconds", 900);

        // Auto-give compass
        if (!cfg.isSet("autogive.compass-on-join")) cfg.set("autogive.compass-on-join", true);
        if (!cfg.isSet("compass.drop-if-full"))     cfg.set("compass.drop-if-full", true);

        // GUI slots sanity (non-destructive; your full layout lives in config.yml)
        String baseMain = "gui.slots.main.";
        if (!cfg.isSet(baseMain + "create")) cfg.set(baseMain + "create", 11);
        if (!cfg.isSet(baseMain + "info"))   cfg.set(baseMain + "info", 13);
        if (!cfg.isSet(baseMain + "remove")) cfg.set(baseMain + "remove", 15);
        if (!cfg.isSet(baseMain + "help"))   cfg.set(baseMain + "help", 31);
        if (!cfg.isSet(baseMain + "admin"))  cfg.set(baseMain + "admin", 33);
        if (!cfg.isSet(baseMain + "back"))   cfg.set(baseMain + "back", 48);

        saveConfig();
    }
}
