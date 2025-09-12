package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Unified GUI listener
 * - Handles ALL menu clicks from config-driven menus
 * - Reads "actions" from config.yml and executes them
 * - Supports:
 *    command:<cmd>  → run command
 *    open:<menu>    → open another GUI menu
 *    custom:<id>    → plugin-specific handlers (trust, untrust, roles, transfer, etc.)
 *    command:close  → close the inventory
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public GUIListener(ProShield plugin, GUIManager guiManager, PlotManager plots, MessagesUtil messages) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.plots = plots;
        this.messages = messages;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        // Check if this inventory matches a configured menu
        ConfigurationSection menus = plugin.getConfig().getConfigurationSection("gui.menus");
        if (menus == null) return;

        String menuKey = menus.getKeys(false).stream()
                .filter(k -> title.equalsIgnoreCase(plugin.getConfig().getString("gui.menus." + k + ".title")))
                .findFirst().orElse(null);

        if (menuKey == null) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        ConfigurationSection itemSec = plugin.getConfig().getConfigurationSection("gui.menus." + menuKey + ".items." + slot);
        if (itemSec == null) return;

        List<String> actions = itemSec.getStringList("actions");
        if (actions == null || actions.isEmpty()) return;

        for (String action : actions) {
            handleAction(player, action, menuKey);
        }
    }

    private void handleAction(Player player, String action, String currentMenu) {
        if (action.startsWith("command:")) {
            String cmd = action.substring("command:".length()).trim();
            if (cmd.equalsIgnoreCase("close")) {
                player.closeInventory();
            } else {
                Bukkit.dispatchCommand(player, cmd);
            }
        } else if (action.startsWith("open:")) {
            String menu = action.substring("open:".length()).trim();
            guiManager.openMenu(player, menu);
        } else if (action.startsWith("custom:")) {
            String custom = action.substring("custom:".length()).trim();
            handleCustomAction(player, custom);
        }
    }

    /**
     * Handles custom plugin-specific actions that can't be represented
     * as just "open menu" or "run command".
     */
    private void handleCustomAction(Player player, String custom) {
        switch (custom.toLowerCase()) {
            case "trustplayer" -> {
                messages.send(player, "trust.added", "Type the player’s name in chat to trust them.");
                player.closeInventory();
            }
            case "untrustplayer" -> {
                messages.send(player, "untrust.removed", "Type the player’s name in chat to untrust them.");
                player.closeInventory();
            }
            case "transferclaim" -> {
                messages.send(player, "transfer.success", "Type the player’s name in chat to transfer ownership.");
                player.closeInventory();
            }
            case "info" -> {
                Plot plot = plots.getPlot(player.getLocation());
                if (plot == null) {
                    messages.send(player, "claim.not-owner", "You are not inside a claim.");
                } else {
                    messages.send(player, "claim.info", "Owner: " + plot.getOwner().toString());
                }
            }
            case "roles" -> {
                Plot plot = plots.getPlot(player.getLocation());
                guiManager.openRolesMenu(player, plot);
            }
            default -> messages.debug(player, "Unknown custom action: " + custom);
        }
    }
}
