package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUIListener - Handles GUI click events for ProShield
 *
 * v1.2.5:
 *   - Player GUI: Claim, Info, Unclaim, Trust, Untrust, Roles, Flags, Transfer
 *   - Admin GUI: Full admin tools (reload, debug, compass, spawn guard, TP tools)
 *   - Back button navigation (cached menus)
 *   - Integrated with GUICache + GUIManager
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;

    public GUIListener(ProShield plugin, GUIManager gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        // Cancel item movement inside ProShield GUIs
        if (title.startsWith("ProShield")) {
            event.setCancelled(true);
        }

        /* ---------------------------------------------------------
         * 🔹 MAIN PLAYER MENU
         * --------------------------------------------------------- */
        if (title.equalsIgnoreCase("ProShield Menu")) {
            switch (itemName.toLowerCase()) {
                case "claim chunk" -> {
                    player.performCommand("proshield claim");
                    player.closeInventory();
                }
                case "claim info" -> {
                    player.performCommand("proshield info");
                    player.closeInventory();
                }
                case "unclaim" -> {
                    player.performCommand("proshield unclaim");
                    player.closeInventory();
                }
                case "trust player" -> gui.openTrustMenu(player);
                case "untrust player" -> gui.openUntrustMenu(player);
                case "roles" -> gui.openRolesMenu(player);
                case "claim flags" -> gui.openFlagsMenu(player);
                case "transfer ownership" -> gui.openTransferMenu(player);
                case "help" -> {
                    player.performCommand("proshield help");
                    player.closeInventory();
                }
                case "admin menu" -> {
                    if (player.hasPermission("proshield.admin")) {
                        gui.openAdmin(player);
                    } else {
                        player.sendMessage("§cYou do not have permission for this.");
                    }
                }
            }
        }

        /* ---------------------------------------------------------
         * 🔹 ADMIN MENU
         * --------------------------------------------------------- */
        else if (title.equalsIgnoreCase("ProShield Admin")) {
            switch (itemName.toLowerCase()) {
                case "toggle fire" -> {
                    player.performCommand("proshield admin fire");
                    player.closeInventory();
                }
                case "toggle explosions" -> {
                    player.performCommand("proshield admin explosions");
                    player.closeInventory();
                }
                case "entity grief" -> {
                    player.performCommand("proshield admin entitygrief");
                    player.closeInventory();
                }
                case "interactions" -> {
                    player.performCommand("proshield admin interactions");
                    player.closeInventory();
                }
                case "pvp" -> {
                    player.performCommand("proshield admin pvp");
                    player.closeInventory();
                }
                case "keep items" -> {
                    player.performCommand("proshield admin keepitems");
                    player.closeInventory();
                }
                case "purge expired" -> {
                    player.performCommand("proshield purgeexpired 30 dryrun");
                    player.closeInventory();
                }
                case "debug mode" -> {
                    player.performCommand("proshield debug toggle");
                    player.closeInventory();
                }
                case "compass drop" -> {
                    player.performCommand("proshield admin compass");
                    player.closeInventory();
                }
                case "reload config" -> {
                    player.performCommand("proshield reload");
                    player.closeInventory();
                }
                case "spawn guard" -> {
                    player.performCommand("proshield admin spawnguard");
                    player.closeInventory();
                }
                case "tp tools" -> {
                    player.performCommand("proshield admin tp");
                    player.closeInventory();
                }
                case "help" -> {
                    player.performCommand("proshield help admin");
                    player.closeInventory();
                }
                case "back" -> gui.openMain(player);
            }
        }

        /* ---------------------------------------------------------
         * 🔹 TRUST MENU
         * --------------------------------------------------------- */
        else if (title.equalsIgnoreCase("Trust Players")) {
            if (itemName.equalsIgnoreCase("trust player")) {
                player.sendMessage("§aUse /proshield trust <player> [role] to trust someone.");
                player.closeInventory();
            } else if (itemName.equalsIgnoreCase("back")) {
                gui.openMain(player);
            }
        }

        /* ---------------------------------------------------------
         * 🔹 UNTRUST MENU
         * --------------------------------------------------------- */
        else if (title.equalsIgnoreCase("Untrust Players")) {
            if (itemName.equalsIgnoreCase("untrust player")) {
                player.sendMessage("§cUse /proshield untrust <player> to remove trust.");
                player.closeInventory();
            } else if (itemName.equalsIgnoreCase("back")) {
                gui.openMain(player);
            }
        }

        /* ---------------------------------------------------------
         * 🔹 ROLES MENU
         * --------------------------------------------------------- */
        else if (title.equalsIgnoreCase("Roles Manager")) {
            switch (itemName.toLowerCase()) {
                case "visitor" -> player.performCommand("proshield trust <player> visitor");
                case "member" -> player.performCommand("proshield trust <player> member");
                case "container" -> player.performCommand("proshield trust <player> container");
                case "builder" -> player.performCommand("proshield trust <player> builder");
                case "co-owner" -> player.performCommand("proshield trust <player> coowner");
                case "back" -> gui.openMain(player);
            }
            player.closeInventory();
        }

        /* ---------------------------------------------------------
         * 🔹 FLAGS MENU
         * --------------------------------------------------------- */
        else if (title.equalsIgnoreCase("Claim Flags")) {
            switch (itemName.toLowerCase()) {
                case "pvp" -> player.performCommand("proshield admin flag pvp");
                case "explosions" -> player.performCommand("proshield admin flag explosions");
                case "fire" -> player.performCommand("proshield admin flag fire");
                case "entity grief" -> player.performCommand("proshield admin flag entitygrief");
                case "interactions" -> player.performCommand("proshield admin flag interactions");
                case "back" -> gui.openMain(player);
            }
            player.closeInventory();
        }

        /* ---------------------------------------------------------
         * 🔹 TRANSFER MENU
         * --------------------------------------------------------- */
        else if (title.equalsIgnoreCase("Transfer Ownership")) {
            if (itemName.equalsIgnoreCase("transfer claim")) {
                player.sendMessage("§eUse /proshield transfer <player> to transfer ownership.");
                player.closeInventory();
            } else if (itemName.equalsIgnoreCase("back")) {
                gui.openMain(player);
            }
        }
    }
}
