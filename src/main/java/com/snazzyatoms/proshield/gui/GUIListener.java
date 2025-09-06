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
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final ProShield plugin;

    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
    }

    /** ==============================
     *  OPEN GUI WITH COMPASS
     *  ============================== */
    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        if (ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("ProShield Compass")) {
            event.setCancelled(true);
            plugin.getGuiManager().openMainGUI(event.getPlayer());
        }
    }

    /** ==============================
     *  HANDLE GUI CLICKS
     *  ============================== */
    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "ProShield Menu")) {
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) return;
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

            switch (name) {
                case "Create Claim" -> {
                    if (plugin.getPlotManager().createClaim(player)) {
                        player.sendMessage(ChatColor.GREEN + "✅ Claim created successfully!");
                    } else {
                        player.sendMessage(ChatColor.RED + "❌ You already own a claim.");
                    }
                }
                case "Claim Info" -> {
                    String info = plugin.getPlotManager().getClaimInfo(player.getLocation());
                    if (info != null) {
                        player.sendMessage(ChatColor.YELLOW + "📖 " + info);
                    } else {
                        player.sendMessage(ChatColor.RED + "❌ No claim found here.");
                    }
                }
                case "Remove Claim" -> {
                    if (plugin.getPlotManager().removeClaim(player)) {
                        player.sendMessage(ChatColor.RED + "🗑️ Claim removed.");
                    } else {
                        player.sendMessage(ChatColor.RED + "❌ You don’t own a claim.");
                    }
                }
            }
        }
    }
}
