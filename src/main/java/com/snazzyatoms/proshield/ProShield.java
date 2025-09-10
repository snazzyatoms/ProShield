package com.snazzyatoms.proshield;

import com.snazzyatoms.proshield.commands.ProShieldCommand;
import com.snazzyatoms.proshield.gui.GUIListener;
import com.snazzyatoms.proshield.gui.GUIManager;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.*;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ProShield extends JavaPlugin {

    private static ProShield instance;
    private PlotManager plotManager;
    private ClaimRoleManager roleManager;
    private GUIManager guiManager;
    private GUICache guiCache;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // Core managers
        this.plotManager = new PlotManager(this);
        this.roleManager = new ClaimRoleManager(this);
        this.guiCache = new GUICache(this);
        this.guiManager = new GUIManager(this, plotManager, roleManager, guiCache);

        // Commands
        ProShieldCommand commandExecutor = new ProShieldCommand(this, plotManager, guiManager);
        getCommand("proshield").setExecutor(commandExecutor);
        getCommand("proshield").setTabCompleter(commandExecutor);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new ClaimMessageListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new InteractionProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new PvpProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new ExplosionProtectionListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new FireProtectionListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new BucketProtectionListener(this, plotManager, roleManager), this);
        Bukkit.getPluginManager().registerEvents(new EntityProtectionListener(this, plotManager), this);

        // ✅ ItemProtectionListener now handles both role-based pickup/drop AND keep-drops logic
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(this, plotManager, roleManager), this);

        // Spawn + Mob repel
        Bukkit.getPluginManager().registerEvents(new SpawnGuardListener(this, plotManager), this);
        Bukkit.getPluginManager().registerEvents(new MobBorderRepelListener(this, plotManager), this);

        getLogger().info("✅ ProShield enabled. Version " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("⛔ ProShield disabled.");
    }

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

    public GUICache getGuiCache() {
        return guiCache;
    }

    public String getPrefix() {
        return getConfig().getString("messages.prefix", "&3[ProShield]&r ");
    }
}
