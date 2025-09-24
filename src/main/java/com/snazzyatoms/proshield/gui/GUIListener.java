// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.compass.CompassManager; // 🔑 Import for COMPASS_NAME
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    // =====================
    // 📌 GUI click handling
    // =====================
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() == null) return;

        // ✅ Cancel vanilla behavior inside all ProShield GUIs
        event.setCancelled(true);

        // ✅ Delegate to GUIManager (routes by title)
        guiManager.handleClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        // ✅ Delay one tick so we know if another ProShield menu opened
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            String title = player.getOpenInventory().getTitle();
            if (title == null) {
                guiManager.clearNav(player);
                return;
            }

            String low = ChatColor.stripColor(title).toLowerCase();

            // ✅ Expanded checks to sync with GUIManager titles
            boolean isProShieldMenu =
                    low.contains("proshield") ||
                    low.contains("main") ||
                    low.contains("claim info") ||
                    low.contains("trusted") ||
                    low.contains("assign role") ||
                    low.contains("flags") ||
                    low.contains("admin") ||
                    low.contains("world") ||
                    low.contains("expansion") ||
                    low.contains("history") ||
                    low.contains("deny reason");

            if (!isProShieldMenu) {
                guiManager.clearNav(player);
            }
        }, 1L);
    }

    // =====================
    // 📌 Compass right-click
    // =====================
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Avoid off-hand double fire
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        // 🔑 Color-safe comparison against CompassManager.COMPASS_NAME
        String displayName = ChatColor.stripColor(meta.getDisplayName());
        String expected    = ChatColor.stripColor(CompassManager.COMPASS_NAME);

        if (!expected.equalsIgnoreCase(displayName)) return;

        // Cancel vanilla compass action
        event.setCancelled(true);

        // ✅ Open GUI
        guiManager.openMainMenu(player);
    }
}
