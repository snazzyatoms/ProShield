package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final PlotManager plotManager;
    private final GUIManager guiManager;

    public GUIListener(PlotManager plotManager, GUIManager guiManager) {
        this.plotManager = plotManager;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if ("ProShield Compass".equalsIgnoreCase(name)) {
            event.setCancelled(true);
            guiManager.openMainGUI(event.getPlayer());
        }
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.equals(ChatColor.DARK_GREEN + "ProShield Menu")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        Location loc = player.getLocation();
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        switch (name) {
            case "Create Claim":
                if (plotManager.createClaim(player.getUniqueId(), loc)) {
                    player.sendMessage(ChatColor.GREEN + "✅ Claim created.");
                } else {
                    player.sendMessage(ChatColor.RED + "❌ Already claimed.");
                }
                break;

            case "Claim Info":
                if (plotManager.isClaimed(loc)) {
                    boolean owner = plotManager.isOwner(player.getUniqueId(), loc);
                    player.sendMessage(ChatColor.YELLOW + "📖 This chunk is claimed. Owner: " +
                            (owner ? "You" : "Someone else"));
                } else {
                    player.sendMessage(ChatColor.GRAY + "No claim here.");
                }
                break;

            case "Remove Claim":
                if (plotManager.removeClaim(player.getUniqueId(), loc)) {
                    player.sendMessage(ChatColor.RED + "🗑️ Claim removed.");
                } else {
                    player.sendMessage(ChatColor.RED + "❌ You do not own this claim.");
                }
                break;
        }
    }
}
