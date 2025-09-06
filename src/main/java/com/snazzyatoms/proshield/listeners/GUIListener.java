package com.snazzyatoms.proshield.GUI;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final GUIManager guiManager;

    public GUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    // Right-click compass -> open GUI
    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.COMPASS || item.getItemMeta() == null)
            return;

        String display = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (display == null) return;

        String expected = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                "&aProShield Admin Compass"));

        if (display.equalsIgnoreCase(expected)) {
            event.setCancelled(true);
            guiManager.openClaimGUI(player);
        }
    }

    // GUI button clicks
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView() == null || event.getView().getTitle() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        String expectedTitle = ChatColor.stripColor(guiManager.getTitle());
        if (!expectedTitle.equalsIgnoreCase(title)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || clicked.getItemMeta().getDisplayName() == null) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        guiManager.handleClick(player, name);
    }
}
