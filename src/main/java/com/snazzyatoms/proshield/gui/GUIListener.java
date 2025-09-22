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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * GUIListener (ProShield v1.2.6 FINAL + Compass Support)
 *
 * - Routes ALL clicks into GUIManager.handleClick()
 * - Cancels vanilla inventory behavior inside ProShield GUIs
 * - Detects ProShield Compass right-click to open Main Menu
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
    }

    // Legacy fallback
    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = (guiManager != null ? guiManager : plugin.getGuiManager());
    }

    // ---------------- GUI Click Routing ----------------

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (guiManager == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Always cancel vanilla actions inside ProShield menus
        event.setCancelled(true);

        try {
            guiManager.handleClick(event); // ✅ Central router does all the work
        } catch (Exception ex) {
            plugin.getLogger().warning("[GUIListener] Error handling click for "
                    + player.getName() + " in GUI: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        // No explicit cleanup needed — GUIManager v1.2.6 manages its own View stack
    }

    // ---------------- Compass Open Trigger ----------------

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (guiManager == null) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) return;

        // Check if it's the ProShield Compass (prefix handled in messages.yml)
        String name = meta.getDisplayName();
        if (!name.contains("ProShield")) return; // adjust if you want stricter check

        event.setCancelled(true); // prevent compass vanilla action
        guiManager.openMainMenu(player); // ✅ open GUI
    }
}
