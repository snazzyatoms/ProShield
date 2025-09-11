package com.snazzyatoms.proshield.gui.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * PlayerMenuListener
 *
 * ✅ Handles clicks in the Player menu.
 * ✅ Opens Trust, Untrust, Roles, and Flags menus.
 * ✅ Back button returns to Player main menu.
 */
public class PlayerMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public PlayerMenuListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("proshield menu")) return;

        e.setCancelled(true); // prevent item movement

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            case "trust menu" -> guiManager.openTrustMenu(player, false);
            case "untrust menu" -> guiManager.openUntrustMenu(player, false);
            case "roles" -> guiManager.openRolesGUI(player, null, false);
            case "flags" -> guiManager.openFlagsMenu(player, false);
            case "back" -> guiManager.openMain(player);
            default -> { return; }
        }

        // 🔊 Click feedback
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }
}
