package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.cache.GUICache;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final GUICache cache;

    public GUIListener(ProShield plugin, GUIManager gui, GUICache cache) {
        this.plugin = plugin;
        this.gui = gui;
        this.cache = cache;
    }

    // ==========================
    // INVENTORY CLICK HANDLER
    // ==========================
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        ItemStack clicked = e.getCurrentItem();

        if (inv == null || clicked == null || clicked.getType() == Material.AIR) return;

        String title = ChatColor.stripColor(inv.getTitle());
        if (title == null) return;

        e.setCancelled(true); // Always cancel GUI clicks

        // ==========================
        // PLAYER GUI
        // ==========================
        if (title.contains("ProShield Menu") && !player.hasPermission("proshield.admin")) {
            switch (clicked.getType()) {
                case GRASS_BLOCK:
                    player.performCommand("proshield claim");
                    player.closeInventory();
                    break;
                case BARRIER:
                    player.performCommand("proshield unclaim");
                    player.closeInventory();
                    break;
                case BOOK:
                    player.performCommand("proshield info");
                    break;
                case PLAYER_HEAD:
                    player.performCommand("proshield trustmenu"); // new trust GUI (future)
                    break;
                case SKELETON_SKULL:
                    player.performCommand("proshield untrustmenu");
                    break;
                case PAPER:
                    player.performCommand("proshield rolemenu");
                    break;
                case CHEST:
                    player.performCommand("proshield transfermenu");
                    break;
                case REDSTONE_TORCH:
                    player.performCommand("proshield flags");
                    break;
                case OAK_SIGN:
                    player.performCommand("proshield help");
                    break;
                case ARROW: // Back
                    gui.openMain(player, false);
                    break;
                default:
                    break;
            }
        }

        // ==========================
        // ADMIN GUI
        // ==========================
        if (title.contains("ProShield Menu") && player.hasPermission("proshield.admin")) {
            switch (clicked.getType()) {
                case FLINT_AND_STEEL:
                    player.performCommand("proshield admin fire");
                    break;
                case TNT:
                    player.performCommand("proshield admin explosions");
                    break;
                case ENDER_PEARL:
                    player.performCommand("proshield admin entitygrief");
                    break;
                case LEVER:
                    player.performCommand("proshield admin interactions");
                    break;
                case IRON_SWORD:
                    player.performCommand("proshield admin pvp");
                    break;
                case HOPPER:
                    player.performCommand("proshield admin keepitems");
                    break;
                case LAVA_BUCKET:
                    player.performCommand("proshield purgeexpired");
                    break;
                case BOOK:
                    player.performCommand("proshield help admin");
                    break;
                case COMMAND_BLOCK:
                    player.performCommand("proshield debug toggle");
                    break;
                case CHEST_MINECART:
                    player.performCommand("proshield admin compasspolicy");
                    break;
                case REDSTONE_BLOCK:
                    player.performCommand("proshield reload");
                    player.sendMessage(ChatColor.GREEN + "ProShield configuration reloaded.");
                    break;
                case ENDER_EYE:
                    player.performCommand("proshield admin tp");
                    break;
                case ARROW: // Back
                    gui.openMain(player, true);
                    break;
                default:
                    break;
            }
        }
    }

    // ==========================
    // INVENTORY CLOSE CLEANUP
    // ==========================
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        // optional: clear cache if needed
    }
}
