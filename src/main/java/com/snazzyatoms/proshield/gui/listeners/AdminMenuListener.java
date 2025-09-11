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
 * AdminMenuListener
 *
 * ✅ Handles clicks in the Admin menu.
 * ✅ Opens Trust, Untrust, Roles, and Flags menus with admin context.
 * ✅ Back button returns to Admin main menu.
 */
public class AdminMenuListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public AdminMenuListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdminMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("proshield admin menu")) return;

        e.setCancelled(true); // prevent item movement

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            case "trust menu" -> guiManager.openTrustMenu(player, true);
            case "untrust menu" -> guiManager.openUntrustMenu(player, true);
            case "roles" -> guiManager.openRolesGUI(player, null, true);
            case "flags" -> guiManager.openFlagsMenu(player, true);
            case "back" -> guiManager.openAdminMain(player);
            default -> { return; }
        }

        // 🔊 Click feedback
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }
}
