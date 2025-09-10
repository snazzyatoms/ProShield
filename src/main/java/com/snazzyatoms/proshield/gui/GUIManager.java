package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.commands.*;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUIManager handles ProShield's custom GUIs:
 * - Claim GUI
 * - Role management GUI
 * - Preview windows
 *
 * Preserves prior logic and enhances consistency:
 * - Ensures all GUIs are cached by player UUID.
 * - Hooks correctly with updated RolesCommand constructor.
 * - Keeps responsibility limited to GUIs (not listeners/commands).
 */
public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    // Cache of open GUIs by player UUID
    private final Map<UUID, Inventory> guiCache = new HashMap<>();

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
        this.messages = plugin.getMessagesUtil();
    }

    /** Opens the roles GUI for a given player and plot. */
    public void openRolesGUI(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dClaim Roles");
        // TODO: Populate roles GUI slots with trusted players + role buttons
        guiCache.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /** Opens the main claim GUI for a player. */
    public void openClaimGUI(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(null, 27, "§bClaim Menu");
        // TODO: Populate claim GUI with management buttons
        guiCache.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /** Clears cached GUIs (used on /proshield reload). */
    public void clearCache() {
        guiCache.clear();
    }

    /* -------------------------------------------------------
     * Command wiring helpers (for GUIs & dispatchers)
     * ------------------------------------------------------- */

    public void registerCommands() {
        // Main ProShield dispatcher is already handled in ProShield.java
        // Here we only ensure subcommands exist & hook properly
        plugin.getCommand("claim").setExecutor(new ClaimSubCommand(plugin, plotManager));
        plugin.getCommand("unclaim").setExecutor(new UnclaimSubCommand(plugin, plotManager));
        plugin.getCommand("info").setExecutor(new InfoSubCommand(plugin, plotManager, roleManager));
        plugin.getCommand("trusted").setExecutor(new TrustedListCommand(plugin, plotManager, roleManager));
        plugin.getCommand("roles").setExecutor(new RolesCommand(plugin, plotManager, roleManager, this));
        plugin.getCommand("transfer").setExecutor(new TransferCommand(plugin, plotManager));
        plugin.getCommand("preview").setExecutor(new PreviewSubCommand(plugin, plotManager));
        plugin.getCommand("compass").setExecutor(new CompassSubCommand(plugin, this));
    }

    /** Safe getter for cached GUI by player. */
    public Inventory getCachedGUI(Player player) {
        return guiCache.get(player.getUniqueId());
    }
}
