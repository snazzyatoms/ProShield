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
 * TrustMenuListener
 *
 * ✅ Handles clicks in the Trust menu.
 * ✅ Back button returns to Player or Admin parent menu.
 */
public class TrustMenuListener implements Listener {

    private final GUIManager guiManager;

    public TrustMenuListener(ProShield plugin, GUIManager guiManager) {
        this.guiManager = guiManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrustClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();
        if (!title.contains("trust player")) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        if (name.equals("back")) {
            boolean fromAdmin = title.contains("admin");
            if (fromAdmin) guiManager.openAdminMain(player); else guiManager.openMain(player);
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }
}
