package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.managers.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIListener implements Listener {

    private final GUIManager guiManager;

    public GUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onPlayerUseCompass(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only check right-clicks
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.COMPASS) {
            return;
        }

        // Verify it’s the ProShield Admin Compass
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        if (!displayName.equalsIgnoreCase("ProShield Admin Compass")) {
            return;
        }

        // Check permission
        if (!player.hasPermission("proshield.compass")) {
            player.sendMessage(ChatColor.RED + "You don’t have permission to use this item.");
            return;
        }

        // ✅ Open the Claim Management GUI
        guiManager.openClaimGUI(player);
        event.setCancelled(true);
    }
}
