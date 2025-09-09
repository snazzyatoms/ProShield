package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.plots.*;
import com.snazzyatoms.proshield.util.ClaimPreview;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    // ---- Singletons / managers ----
    private static ProShield instance;
    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;

    // ---- Runtime toggles ----
    private boolean debug = false;

    // ---- Tasks (repel) ----
    private EntityBorderRepelTask repelTask;
    private int repelTaskId = -1;

    // ---- Lifecycle ----
    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        migrateAndNormalizeConfig();  // ensures 1.2.4 keys & sane defaults

        // Core managers
        plotManager = new PlotManager(this);
        roleManager = new ClaimRoleManager(this);
        plotManager.setRoleManager(roleManager);

        // GUI manager (needs plugin + plots)
        guiManager = new GUIManager(this, plotManager);

        // Listeners (wire everything we’ve added in 1.2.4)
        getServer().getPluginManager().registerEvents(new GUIListener(plotManager, guiManager), this);
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new PvpProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageProtectionListener(this, plotManager), this);
        getServer().getPluginManager().registerEvents(new ItemProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new KeepDropsListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new EntityDamageProtectionListener(plotManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, plotManager, guiManager), this);
        getServer().getPluginManager().registerEvents(new ClaimMessageListener(plotManager, roleManager), this);

        // Commands
        ProShieldCommand cmdExec = new ProShieldCommand(this, plotManager);
        PluginCommand root = getCommand("proshield");
        if (root != null) {
            root.setExecutor(cmdExec);
            if (cmdExec instanceof TabCompleter tc) {
                root.setTabCompleter(tc);
            }
        }

        // Border repel scheduler (reads protection.mobs.border-repel.*)
        startRepelTask();

        getLogger().info("ProShield " + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        stopRepelTask();
        HandlerList.unregisterAll(this);
        getLogger().info("ProShield disabled.");
    }

    // ---- Public API-ish helpers ----
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

    public void setDebug(boolean value) {
        this.debug = value;
        getLogger().info("[Debug] " + (value ? "ON" : "OFF"));
    }

    // ---- Reload path (used by /proshield reload and Admin GUI Reload) ----
    /** Call this from command or GUI when reloading configs. */
    public void onConfigReload() {
        reloadConfig();
        migrateAndNormalizeConfig(); // ensure missing keys are added after reload

        // pass-through reloads
        if (plotManager != null) plotManager.reloadFromConfig();
        if (roleManager != null) roleManager.reloadFromConfigSafe();
        if (guiManager != null) guiManager.onConfigReload();

        // task settings may have changed (interval / enable)
        if (repelTask != null) repelTask.reloadSettings();
        stopRepelTask();
        startRepelTask();

        getLogger().info("ProShield configuration reloaded.");
    }

    // ---- Repel task wiring ----
    private void startRepelTask() {
        if (repelTaskId != -1) return; // already running
        boolean enabled = getConfig().getBoolean("protection.mobs.border-repel.enabled", true);
        int interval = Math.max(1, getConfig().getInt("protection.mobs.border-repel.interval-ticks", 20));
        repelTask = new EntityBorderRepelTask(this, plotManager);
        if (enabled) {
            repelTaskId = repelTask.runTaskTimer(this, interval, interval).getTaskId();
            if (isDebug()) getLogger().info("[Debug] BorderRepel task started @ " + interval + " ticks.");
        } else {
            if (isDebug()) getLogger().info("[Debug] BorderRepel disabled in config.");
        }
    }

    private void stopRepelTask() {
        if (repelTaskId != -1) {
            Bukkit.getScheduler().cancelTask(repelTaskId);
            repelTaskId = -1;
            if (isDebug()) getLogger().info("[Debug] BorderRepel task stopped.");
        }
    }

    // ---- Config migration / normalization to 1.2.4 ----
    private void migrateAndNormalizeConfig() {
        var cfg = getConfig();

        // Ensure version header
        cfg.set("version", "1.2.4");

        // Ensure spawn guard defaults
        if (!cfg.isConfigurationSection("spawn")) {
            cfg.createSection("spawn");
        }
        if (!cfg.contains("spawn.block-claiming")) cfg.set("spawn.block-claiming", true);
        if (!cfg.contains("spawn.radius")) cfg.set("spawn.radius", 32);

        // Ensure damage protection (owner/trusted) presence
        if (!cfg.isConfigurationSection("protection.damage")) {
            cfg.set("protection.damage.enabled", true);
            cfg.set("protection.damage.protect-owner-and-trusted", true);
            cfg.set("protection.damage.cancel-all", true);
            cfg.set("protection.damage.pve", true);
            cfg.set("protection.damage.projectiles", true);
            cfg.set("protection.damage.environment", true);
            cfg.set("protection.damage.fire-lava", true);
            cfg.set("protection.damage.fall", true);
            cfg.set("protection.damage.explosions", true);
            cfg.set("protection.damage.drown-void-suffocate", true);
            cfg.set("protection.damage.poison-wither", true);
        }

        // Ensure pvp toggle default
        if (!cfg.contains("protection.pvp-in-claims")) {
            cfg.set("protection.pvp-in-claims", false);
        }

        // Ensure interactions scaffold
        if (!cfg.isConfigurationSection("protection.interactions")) {
            cfg.set("protection.interactions.enabled", true);
            cfg.set("protection.interactions.mode", "blacklist");
            cfg.set("protection.interactions.categories", java.util.List.of(
                    "doors", "trapdoors", "fence_gates", "buttons", "levers", "pressure_plates"
            ));
            cfg.set("protection.interactions.list", new java.util.ArrayList<>());
        }

        // Ensure explosions scaffold
        if (!cfg.isConfigurationSection("protection.explosions")) {
            cfg.set("protection.explosions.enabled", true);
            cfg.set("protection.explosions.creeper", true);
            cfg.set("protection.explosions.tnt", true);
            cfg.set("protection.explosions.wither", true);
            cfg.set("protection.explosions.wither_skull", true);
            cfg.set("protection.explosions.end_crystal", true);
            cfg.set("protection.explosions.ender_dragon", true);
        }

        // Ensure fire scaffold
        if (!cfg.isConfigurationSection("protection.fire")) {
            cfg.set("protection.fire.enabled", true);
            cfg.set("protection.fire.spread", true);
            cfg.set("protection.fire.burn", true);
            cfg.set("protection.fire.ignite.flint_and_steel", true);
            cfg.set("protection.fire.ignite.lava", true);
            cfg.set("protection.fire.ignite.lightning", true);
            cfg.set("protection.fire.ignite.explosion", true);
            cfg.set("protection.fire.ignite.spread", true);
        }

        // Ensure buckets scaffold
        if (!cfg.isConfigurationSection("protection.buckets")) {
            cfg.set("protection.buckets.block-empty", true);
            cfg.set("protection.buckets.block-fill", true);
        }

        // Entity grief/teleport scaffolds
        if (!cfg.isConfigurationSection("protection.entity-grief")) {
            cfg.set("protection.entity-grief.enabled", true);
            cfg.set("protection.entity-grief.enderman", true);
            cfg.set("protection.entity-grief.ravager", true);
            cfg.set("protection.entity-grief.silverfish", true);
            cfg.set("protection.entity-grief.ender-dragon", true);
            cfg.set("protection.entity-grief.wither", true);
        }
        if (!cfg.isConfigurationSection("protection.entity-teleport")) {
            cfg.set("protection.entity-teleport.enderman", true);
        }

        // Entities (frames, stands, pets) scaffold
        if (!cfg.isConfigurationSection("protection.entities")) {
            cfg.set("protection.entities.item-frames", true);
            cfg.set("protection.entities.armor-stands", true);
            cfg.set("protection.entities.passive-animals", true);
            cfg.set("protection.entities.tamed-pets", true);
        }

        // Mobs scaffold + border-repel
        if (!cfg.isConfigurationSection("protection.mobs")) {
            cfg.set("protection.mobs.block-spawn", true);
            cfg.set("protection.mobs.allow-spawner-spawn", true);
        }
        if (!cfg.isConfigurationSection("protection.mobs.border-repel")) {
            cfg.set("protection.mobs.border-repel.enabled", true);
            cfg.set("protection.mobs.border-repel.radius", 1.5D);
            cfg.set("protection.mobs.border-repel.horizontal-push", 0.6D);
            cfg.set("protection.mobs.border-repel.vertical-push", 0.15D);
            cfg.set("protection.mobs.border-repel.interval-ticks", 20);
        }

        // Keep items scaffold
        if (!cfg.isConfigurationSection("claims.keep-items")) {
            cfg.set("claims.keep-items.enabled", false);
            cfg.set("claims.keep-items.despawn-seconds", 900);
        }

        // Autogive compass scaffold
        if (!cfg.contains("autogive.compass-on-join")) cfg.set("autogive.compass-on-join", true);
        if (!cfg.contains("compass.drop-if-full")) cfg.set("compass.drop-if-full", true);

        // GUI slots minimal scaffold (won’t override if present)
        if (!cfg.isConfigurationSection("gui.slots.main")) {
            cfg.set("gui.slots.main.create", 11);
            cfg.set("gui.slots.main.info", 13);
            cfg.set("gui.slots.main.remove", 15);
            cfg.set("gui.slots.main.help", 31);
            cfg.set("gui.slots.main.admin", 33);
            cfg.set("gui.slots.main.back", 48);
        }
        if (!cfg.isConfigurationSection("gui.slots.admin")) {
            cfg.set("gui.slots.admin.fire", 10);
            cfg.set("gui.slots.admin.explosions", 11);
            cfg.set("gui.slots.admin.entity-grief", 12);
            cfg.set("gui.slots.admin.interactions", 13);
            cfg.set("gui.slots.admin.pvp", 14);
            cfg.set("gui.slots.admin.keep-items", 20);
            cfg.set("gui.slots.admin.purge-expired", 21);
            cfg.set("gui.slots.admin.debug", 23);
            cfg.set("gui.slots.admin.compass-drop-if-full", 24);
            cfg.set("gui.slots.admin.spawn-guard", 25);
            cfg.set("gui.slots.admin.tp-tools", 30);
            cfg.set("gui.slots.admin.back", 31);
            cfg.set("gui.slots.admin.help", 22);
            cfg.set("gui.slots.admin.reload", 25); // if you want reload separate, move to an unused slot
        }

        saveConfig();
    }
}
