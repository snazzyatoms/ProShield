package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.managers.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIListener implements Listener {

    private final GUIManager guiManager;

    public GUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    /**
     * Handle right-click with the ProShield Admin Compass
     */
    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only care about right-click
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        // Match display name of ProShield Admin Compass
        if (ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("ProShield Admin Compass")) {
            // Check permission
            if (!player.hasPermission("proshield.compass")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use the ProShield Compass.");
                return;
            }

            // Cancel interaction and open GUI
            event.setCancelled(true);
            guiManager.openClaimGUI(player);
        }
    }

    /**
     * Handle clicks inside the Claim Management GUI
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Check GUI title from GUIManager
        if (title.equals(ChatColor.DARK_GREEN + "Claim Management")) {
            event.setCancelled(true); // Prevent item grabbing

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

            switch (name.toLowerCase()) {
                case "create claim":
                    player.performCommand("proshield createclaim");
                    player.closeInventory();
                    break;
                case "claim info":
                    player.performCommand("proshield claiminfo");
                    player.closeInventory();
                    break;
                case "remove claim":
                    player.performCommand("proshield removeclaim");
                    player.closeInventory();
                    break;
                default:
                    break;
            }
        }
    }
}
