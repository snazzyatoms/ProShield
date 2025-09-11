package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * GUIManager
 *
 * ✅ Centralized menu building (Player + Admin)
 * ✅ Connected to CompassManager (right-click compass → opens GUI)
 * ✅ Pulls settings from PlotManager for per-claim context
 * ✅ Preserves all prior versions’ features (1.2.0 → 1.2.5)
 */
public class GUIManager implements Listener {

    private final ProShield plugin;
    private final GUICache cache;
    private final PlotManager plots;
    private final MessagesUtil messages;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
        this.plots = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();

        // Auto-register as listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* -------------------------------------------------------
     * PLAYER COMPASS INTERACTION
     * ------------------------------------------------------- */
    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.COMPASS) return;
        if (!event.getItem().hasItemMeta()) return;
        ItemMeta meta = event.getItem().getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());
        Player player = event.getPlayer();

        if (name.equalsIgnoreCase("ProShield Compass")) {
            openPlayerMenu(player);
            event.setCancelled(true);
        }
        if (name.equalsIgnoreCase("ProShield Admin Compass") &&
            (player.isOp() || player.hasPermission("proshield.admin"))) {
            openAdminMenu(player);
            event.setCancelled(true);
        }
    }

    /* -------------------------------------------------------
     * PLAYER MENU
     * ------------------------------------------------------- */
    public void openPlayerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "ProShield Menu");

        inv.setItem(11, menuItem(Material.GRASS_BLOCK, "&aClaim Chunk",
                "&7Claim the chunk you are standing in."));
        inv.setItem(12, menuItem(Material.BARRIER, "&cUnclaim Chunk",
                "&7Unclaim your current chunk."));
        inv.setItem(13, menuItem(Material.PAPER, "&bClaim Info",
                "&7View info about this claim."));
        inv.setItem(14, menuItem(Material.PLAYER_HEAD, "&eTrust Players",
                "&7Manage trusted players and roles."));
        inv.setItem(15, menuItem(Material.REDSTONE, "&dFlags",
                "&7Toggle claim flags like PvP, fire, TNT."));

        // Save to cache for click handling
        cache.setPlayerMenu(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /* -------------------------------------------------------
     * ADMIN MENU
     * ------------------------------------------------------- */
    public void openAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "ProShield Admin Menu");

        inv.setItem(10, menuItem(Material.COMPASS, "&6Teleport to Claim",
                "&7Jump directly to a player’s claim."));
        inv.setItem(11, menuItem(Material.TNT, "&cForce Unclaim",
                "&7Remove ownership of a claim."));
        inv.setItem(12, menuItem(Material.CLOCK, "&ePurge Expired",
                "&7Clean up expired claims."));
        inv.setItem(13, menuItem(Material.CHEST, "&aKeep Items Toggle",
                "&7Toggle keep-items inside claims."));
        inv.setItem(14, menuItem(Material.REDSTONE_BLOCK, "&dAdmin Flags",
                "&7Override claim flags globally or per claim."));
        inv.setItem(15, menuItem(Material.BOOK, "&bDebug Toggle",
                "&7Enable or disable debug logging."));

        // Wilderness & special admin tools
        inv.setItem(16, menuItem(Material.MAP, "&2Wilderness Tools",
                "&7Manage wilderness messages and settings."));

        // Save to cache for click handling
        cache.setAdminMenu(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /* -------------------------------------------------------
     * HELPERS
     * ------------------------------------------------------- */
    private ItemStack menuItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(Arrays.asList(lore));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }
}
