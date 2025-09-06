package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    private final PlayerGUI playerGUI;

    public GUIListener(ProShield plugin) {
        this.playerGUI = plugin.getGuiManager().getPlayerGUI();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals("ProShield Menu")) return;

        event.setCancelled(true); // Prevent taking items
        int slot = event.getRawSlot();
        playerGUI.handleClick(player, slot);
    }
}
