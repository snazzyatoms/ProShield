// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Unified GUI Listener
 *
 * - Listens to all ProShield menu clicks.
 * - Reads "action:" from config.yml to determine what happens.
 * - Supports:
 *    - "command:/something" → Runs a Bukkit command as player
 *    - "open:menuName"      → Opens another menu defined in config.yml
 *    - "custom:actionName"  → Runs plugin-specific logic (trust/untrust/roles/etc.)
 *
 * This removes the need for multiple small listeners and unifies everything here.
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
        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        // ✅ Only handle ProShield menus
        if (!gui.isProShieldMenu(title)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        // Get actions from config
        List<String> actions = gui.getItemActions(title, event.getSlot());
        if (actions == null || actions.isEmpty()) return;

        for (String action : actions) {
            handleAction(player, title, action);
        }
    }

    /**
     * Central action dispatcher.
     */
    private void handleAction(Player player, String menuTitle, String action) {
        if (action.startsWith("command:")) {
            String cmd = action.substring("command:".length()).trim();
            Bukkit.dispatchCommand(player, cmd);

        } else if (action.startsWith("open:")) {
            String menu = action.substring("open:".length()).trim();
            gui.openMenu(player, menu);

        } else if (action.startsWith("custom:")) {
            String key = action.substring("custom:".length()).trim();
            runCustomAction(player, menuTitle, key);

        } else {
            plugin.getLogger().warning("Unknown GUI action: " + action);
        }
    }

    /**
     * Custom hard-coded actions (plugin-specific).
     */
    private void runCustomAction(Player player, String menuTitle, String key) {
        Plot plot = plots.getPlot(player.getLocation());
        switch (key.toLowerCase()) {
            case "trustplayer" -> {
                messages.send(player, "trust.opened");
                // In future: open chat prompt or GUI for trust
            }
            case "untrustplayer" -> {
                messages.send(player, "untrust.opened");
                // In future: open chat prompt or GUI for untrust
            }
            case "roles" -> {
                if (plot == null) {
                    messages.send(player, "roles.no-claim");
                } else {
                    gui.openRolesMenu(player, plot, player.hasPermission("proshield.admin"));
                }
            }
            case "flags" -> {
                gui.openFlagsMenu(player, player.hasPermission("proshield.admin"));
            }
            case "info" -> {
                gui.openInfoMenu(player, plot);
            }
            default -> plugin.getLogger().warning("Unhandled custom action: " + key);
        }
    }
}
