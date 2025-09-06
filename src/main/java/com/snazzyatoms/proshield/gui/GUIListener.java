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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final ProShield plugin;

    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent e) {
        // Only right hand triggers; ignore offhand duplicate fires
        if (e.getHand() != EquipmentSlot.HAND) return;

        ItemStack it = e.getItem();
        if (it == null || it.getType() != Material.COMPASS || !it.hasItemMeta()) return;

        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName());
        if (!"ProShield Admin Compass".equalsIgnoreCase(name)) return;

        e.setCancelled(true);
        plugin.getGuiManager().openClaimGUI(e.getPlayer());
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent e) {
        if (e.getView() == null || e.getView().getTitle() == null) return;
        String title = ChatColor.stripColor(e.getView().getTitle());
        if (!"Claim Management".equalsIgnoreCase(title)) return;

        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        Player p = (Player) e.getWhoClicked();

        switch (name.toLowerCase()) {
            case "create claim":
                plugin.getGuiManager().handleCreate(p);
                break;
            case "claim info":
                plugin.getGuiManager().handleInfo(p);
                break;
            case "remove claim":
                plugin.getGuiManager().handleRemove(p);
                break;
            default:
                break;
        }
    }
}
