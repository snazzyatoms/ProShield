package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUIListener routes inventory interactions to GUIManager via GUICache.
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInv = event.getClickedInventory();
        ItemStack clicked = event.getCurrentItem();

        if (clickedInv == null || clicked == null) return;

        // Check if this item is registered in GUICache
        String action = GUICache.getAction(clicked);
        if (action == null) return; // Not a ProShield GUI button

        event.setCancelled(true); // Prevent item movement

        // Handle GUI actions
        handleButtonClick(player, action);
    }

    private void handleButtonClick(Player player, String action) {
        switch (action.toLowerCase()) {
            // ==== Player Actions ====
            case "claim":
                player.performCommand("proshield claim");
                break;
            case "info":
                player.performCommand("proshield info");
                break;
            case "unclaim":
                player.performCommand("proshield unclaim");
                break;
            case "trust":
                player.performCommand("proshield trustmenu");
                break;
            case "untrust":
                player.performCommand("proshield untrustmenu");
                break;
            case "roles":
                player.performCommand("proshield rolemenu");
                break;
            case "flags":
                player.performCommand("proshield flagmenu");
                break;
            case "transfer":
                player.performCommand("proshield transfermenu");
                break;
            case "help":
                player.performCommand("proshield help");
                break;
            case "back":
                guiManager.openMain(player, player.hasPermission("proshield.admin"));
                break;

            // ==== Admin Actions ====
            case "admin":
                guiManager.openAdmin(player);
                break;
            case "toggle_fire":
                player.performCommand("proshield toggle fire");
                break;
            case "toggle_explosions":
                player.performCommand("proshield toggle explosions");
                break;
            case "toggle_entity_grief":
                player.performCommand("proshield toggle entitygrief");
                break;
            case "toggle_interactions":
                player.performCommand("proshield toggle interactions");
                break;
            case "toggle_pvp":
                player.performCommand("proshield toggle pvp");
                break;
            case "toggle_keepitems":
                player.performCommand("proshield toggle keepitems");
                break;
            case "purge_expired":
                player.performCommand("proshield purgeexpired 30 dryrun");
                break;
            case "toggle_debug":
                player.performCommand("proshield debug toggle");
                break;
            case "reload":
                player.performCommand("proshield reload");
                break;
            case "tp_tools":
                player.performCommand("proshield tpmenu");
                break;
            case "toggle_spawnguard":
                player.performCommand("proshield toggle spawnguard");
                break;
            default:
                player.sendMessage("§c[ProShield] Unknown action: " + action);
                break;
        }
    }
}
