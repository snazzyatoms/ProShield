package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final GUICache cache;

    public GUIListener(ProShield plugin, GUIManager gui, GUICache cache) {
        this.plugin = plugin;
        this.gui = gui;
        this.cache = cache;
    }

    /* -----------------------------------------------------
     * Inventory Click Handling
     * --------------------------------------------------- */

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null) return;

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String title = ChatColor.stripColor(e.getView().getTitle());
        String name = ChatColor.stripColor(meta.getDisplayName());
        e.setCancelled(true);

        // --- MAIN MENU ---
        if (title.equalsIgnoreCase("ProShield")) {
            switch (name) {
                case "Claim Land" -> plugin.getCommandHandler().claim(player);
                case "Claim Info" -> plugin.getCommandHandler().info(player);
                case "Unclaim Land" -> plugin.getCommandHandler().unclaim(player);

                case "Trust Player" -> gui.openTrust(player);
                case "Untrust Player" -> gui.openUntrust(player);
                case "Roles" -> gui.openRoles(player);
                case "Claim Flags" -> gui.openFlags(player);
                case "Transfer Ownership" -> gui.openTransfer(player);

                case "Help" -> plugin.getCommandHandler().help(player);
                case "Admin Menu" -> {
                    if (player.hasPermission("proshield.admin")) {
                        gui.openAdmin(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "No permission.");
                    }
                }
            }
        }

        // --- TRUST MENU ---
        if (title.equalsIgnoreCase("Trust a Player")) {
            if (name.equalsIgnoreCase("Back")) gui.openMain(player);
            else if (name.equalsIgnoreCase("Enter Player Name")) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Type a player name in chat to trust.");
                plugin.getCommandHandler().awaitTrust(player);
            }
        }

        // --- UNTRUST MENU ---
        if (title.equalsIgnoreCase("Untrust a Player")) {
            if (name.equalsIgnoreCase("Back")) gui.openMain(player);
            else if (name.equalsIgnoreCase("Enter Player Name")) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Type a player name in chat to untrust.");
                plugin.getCommandHandler().awaitUntrust(player);
            }
        }

        // --- ROLES MENU ---
        if (title.equalsIgnoreCase("Manage Roles")) {
            if (name.equalsIgnoreCase("Back")) gui.openMain(player);
            else {
                player.closeInventory();
                plugin.getCommandHandler().setRole(player, name);
            }
        }

        // --- FLAGS MENU ---
        if (title.equalsIgnoreCase("Claim Flags")) {
            if (name.equalsIgnoreCase("Back")) gui.openMain(player);
            else {
                player.closeInventory();
                plugin.getCommandHandler().toggleFlag(player, name);
            }
        }

        // --- TRANSFER MENU ---
        if (title.equalsIgnoreCase("Transfer Ownership")) {
            if (name.equalsIgnoreCase("Back")) gui.openMain(player);
            else if (name.equalsIgnoreCase("Enter Player Name")) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Type a player name in chat to transfer claim ownership.");
                plugin.getCommandHandler().awaitTransfer(player);
            }
        }

        // --- ADMIN MENU ---
        if (title.equalsIgnoreCase("Admin Menu")) {
            switch (name) {
                case "Fire Toggle" -> plugin.getCommandHandler().toggleFire(player);
                case "Explosions Toggle" -> plugin.getCommandHandler().toggleExplosions(player);
                case "Entity Grief Toggle" -> plugin.getCommandHandler().toggleEntityGrief(player);
                case "Interactions Toggle" -> plugin.getCommandHandler().toggleInteractions(player);
                case "PvP Toggle" -> plugin.getCommandHandler().togglePvP(player);

                case "Keep Items Toggle" -> plugin.getCommandHandler().toggleKeepItems(player);
                case "Purge Expired Claims" -> plugin.getCommandHandler().purgeExpired(player);
                case "Debug Toggle" -> plugin.getCommandHandler().toggleDebug(player);
                case "Compass Drop Setting" -> plugin.getCommandHandler().toggleCompassDrop(player);
                case "Reload Config" -> plugin.reloadAllConfigs();
                case "Teleport Tools" -> plugin.getCommandHandler().adminTeleport(player);

                case "Back" -> gui.openMain(player);
                case "Help" -> plugin.getCommandHandler().help(player);
            }
        }
    }

    /* -----------------------------------------------------
     * Cache Cleanup
     * --------------------------------------------------- */

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        cache.clear(e.getPlayer());
    }
}
