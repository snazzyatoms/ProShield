package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;

    public GUIListener(ProShield plugin, GUIManager gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    /* ===============================
     * COMPASS INTERACTION
     * =============================== */

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (!gui.isProShieldCompass(item)) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        boolean isAdmin = player.hasPermission("proshield.admin");
        gui.openMain(player, isAdmin);
    }

    /* ===============================
     * INVENTORY CLICKS
     * =============================== */

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (title == null) return;

        boolean isAdmin = title.toLowerCase().contains("admin");
        String menuTitle = isAdmin ? "proshield admin menu" : "proshield menu";

        if (!title.toLowerCase().contains("proshield")) return;

        ItemStack clicked = event.getCurrentItem();
        int slot = event.getSlot();

        gui.handleInventoryClick(player, slot, clicked, event, isAdmin);
    }
}
