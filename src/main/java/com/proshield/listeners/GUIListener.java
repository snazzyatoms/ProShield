package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.managers.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Only handle clicks in our GUI
        if (event.getView().getTitle() == null ||
            !ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Claim Management")) {
            return;
        }

        event.setCancelled(true); // Stop item pickup

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());

        switch (displayName.toLowerCase()) {
            case "create claim":
                player.closeInventory();
                if (plotManager.createClaim(player)) {
                    player.sendMessage(ChatColor.GREEN + "[ProShield] Claim created successfully!");
                } else {
                    player.sendMessage(ChatColor.RED + "[ProShield] Failed to create claim (you may already own one).");
                }
                break;

            case "claim info":
                player.closeInventory();
                String info = plotManager.getClaimInfo(player);
                if (info != null) {
                    player.sendMessage(ChatColor.YELLOW + "[ProShield] " + info);
                } else {
                    player.sendMessage(ChatColor.RED + "[ProShield] You don’t own a claim yet.");
                }
                break;

            case "remove claim":
                player.closeInventory();
                if (plotManager.removeClaim(player)) {
                    player.sendMessage(ChatColor.RED + "[ProShield] Claim removed successfully.");
                } else {
                    player.sendMessage(ChatColor.RED + "[ProShield] No claim found to remove.");
                }
                break;

            default:
                break;
        }
    }
}
