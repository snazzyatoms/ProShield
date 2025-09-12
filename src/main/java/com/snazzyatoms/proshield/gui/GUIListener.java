package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUIListener
 * - Handles all GUI clicks in one place (main, trust, untrust, roles, flags, admin).
 * - Config-driven: looks up menus/items from config.yml instead of hardcoding slots.
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil messages;

    public GUIListener(ProShield plugin, GUIManager gui, PlotManager plots, ClaimRoleManager roles, MessagesUtil messages) {
        this.plugin = plugin;
        this.gui = gui;
        this.plots = plots;
        this.roles = roles;
        this.messages = messages;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        if (title == null) return;

        // Identify which menu this is (by matching config titles)
        String menuId = getMenuIdFromTitle(title);
        if (menuId == null) return;

        event.setCancelled(true); // Prevent taking items

        // Find which item in config matches the clicked slot
        String itemId = getItemIdFromSlot(menuId, event.getRawSlot());
        if (itemId == null) return;

        handleMenuClick(player, menuId, itemId);
    }

    /* ====================================================
     * CONFIG LOOKUPS
     * ==================================================== */

    private String getMenuIdFromTitle(String title) {
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("ui.menus");
        if (menus == null) return null;

        for (String id : menus.getKeys(false)) {
            String configTitle = menus.getString(id + ".title", "");
            if (title.equalsIgnoreCase(org.bukkit.ChatColor.translateAlternateColorCodes('&', configTitle))) {
                return id;
            }
        }
        return null;
    }

    private String getItemIdFromSlot(String menuId, int slot) {
        ConfigurationSection items = plugin.getConfig().getConfigurationSection("ui.menus." + menuId + ".items");
        if (items == null) return null;

        for (String id : items.getKeys(false)) {
            if (items.getInt(id + ".slot", -1) == slot) {
                return id;
            }
        }
        return null;
    }

    /* ====================================================
     * MENU ACTIONS
     * ==================================================== */
    private void handleMenuClick(Player player, String menuId, String itemId) {
        Plot plot = plots.getPlot(player.getLocation());

        switch (menuId) {
            case "main" -> handleMainMenu(player, itemId, plot);
            case "roles" -> handleRolesMenu(player, itemId, plot);
            case "trust" -> gui.rememberTarget(player, "PENDING_TRUST"); // Example placeholder
            case "untrust" -> gui.rememberTarget(player, "PENDING_UNTRUST");
            case "flags" -> messages.send(player, "flags.toggle", Map.of("flag", itemId, "state", "TBD"));
            case "admin" -> handleAdminMenu(player, itemId);
            default -> messages.debug(player, "Clicked " + itemId + " in menu " + menuId);
        }
    }

    private void handleMainMenu(Player player, String itemId, Plot plot) {
        switch (itemId) {
            case "claim" -> {
                if (plot == null) {
                    plots.createPlot(player.getUniqueId(), player.getLocation().getChunk());
                    messages.send(player, "claim.success");
                } else {
                    messages.send(player, "claim.already-owned", Map.of("owner", "someone"));
                }
            }
            case "unclaim" -> {
                if (plot != null && plot.isOwner(player.getUniqueId())) {
                    plots.removePlot(plot);
                    messages.send(player, "claim.unclaimed");
                } else {
                    messages.send(player, "claim.not-owner");
                }
            }
            case "info" -> gui.openInfoMenu(player, plot);
            case "trust" -> gui.openTrustMenu(player);
            case "untrust" -> gui.openUntrustMenu(player);
            case "roles" -> gui.openRolesMenu(player);
            case "flags" -> gui.openFlagsMenu(player);
            default -> messages.debug(player, "Unhandled main menu item: " + itemId);
        }
    }

    private void handleRolesMenu(Player player, String itemId, Plot plot) {
        if (plot == null) {
            messages.send(player, "roles.no-claim");
            return;
        }

        UUID targetId = player.getUniqueId(); // For now, act on self. Can extend.
        ClaimRole role = switch (itemId.toLowerCase()) {
            case "builder" -> ClaimRole.BUILDER;
            case "moderator" -> ClaimRole.MODERATOR;
            case "manager" -> ClaimRole.MANAGER;
            default -> ClaimRole.TRUSTED;
        };

        roles.assignRole(plot.getId(), targetId, role);
        messages.send(player, "roles.updated", Map.of(
                "player", player.getName(),
                "role", role.getDisplayName(),
                "claim", plot.getDisplayNameSafe()
        ));
    }

    private void handleAdminMenu(Player player, String itemId) {
        switch (itemId) {
            case "teleport" -> player.sendMessage("Teleport to a claim (not yet implemented).");
            case "purge" -> player.sendMessage("Purge expired claims (not yet implemented).");
            default -> messages.debug(player, "Admin menu item clicked: " + itemId);
        }
    }
}
