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
 * ✅ Back button returns to the correct parent (player or admin).
 * ✅ Static menu (items can’t be moved).
 */
public class TrustMenuListener implements Listener {

    private final GUIManager gui;

    public TrustMenuListener(ProShield plugin, GUIManager gui) {
        this.gui = gui;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrustClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        switch (name) {
            case "back" -> {
                boolean fromAdmin = player.hasPermission("proshield.admin");
                if (fromAdmin) gui.openAdminMain(player);
                else gui.openMain(player);

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            default -> {
                // Placeholder: Trust is handled by /trust command
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }
    }
}
