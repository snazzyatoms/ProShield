package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * GUIListener (ProShield v1.2.6 FINAL)
 *
 * - Routes ALL clicks into GUIManager.handleClick()
 * - Cancels vanilla inventory behavior inside ProShield GUIs
 * - Detects ProShield Compass right-click → opens main menu
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
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (guiManager == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Always cancel vanilla actions inside our GUIs
        event.setCancelled(true);

        try {
            guiManager.handleClick(event);
        } catch (Exception ex) {
            plugin.getLogger().warning("[GUIListener] Error handling click: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        // No explicit cleanup — GUIManager manages view stack
    }

    /** Compass right-click → open GUI */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return; // ignore off-hand
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String dn = meta.getDisplayName();
        String expected = plugin.getMessagesUtil().color(
                plugin.getMessagesUtil().getOrDefault("messages.compass.display-name", "&bProShield Compass")
        );
        if (!expected.equals(dn)) return; // not our compass

        event.setCancelled(true);
        guiManager.openMainMenu(player);
    }
}
