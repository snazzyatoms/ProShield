// path: src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final ProShield plugin;

    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle right-clicking the Admin Compass.
     */
    @EventHandler
    public void onPlayerUseCompass(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (displayName != null && displayName.equalsIgnoreCase("ProShield Admin Compass")) {
            event.setCancelled(true);
            plugin.getGuiManager().openMainMenu(event.getPlayer());
        }
    }

    /**
     * Handle clicks inside ProShield menus.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());

        // Main ProShield Menu
        if ("proshield menu".equalsIgnoreCase(title)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            switch (name.toLowerCase()) {
                case "manage your plots":
                    plugin.getGuiManager().openPlayerGUI(player);
                    break;

                case "admin compass":
                    player.getInventory().addItem(GUIManager.createAdminCompass());
                    player.sendMessage(ChatColor.YELLOW + "You received the ProShield Admin Compass!");
                    break;

                case "close menu":
                    player.closeInventory();
                    break;

                default:
                    player.sendMessage(ChatColor.GRAY + "Unknown option.");
            }
        }

        // Player Plot GUI
        else if ("your plots".equalsIgnoreCase(title)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            PlayerGUI gui = new PlayerGUI(plugin, player);
            gui.handleClick(clicked);
        }
    }
}
