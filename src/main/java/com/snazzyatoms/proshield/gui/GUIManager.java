// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * GUIManager
 * Handles player GUI interactions, cached menus, and utility items.
 *
 * ✅ Preserves all prior logic
 * ✅ Adds giveCompass(Player, boolean)
 * ✅ Compass now opens ProShield GUI when right-clicked
 */
public class GUIManager implements Listener {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;

        // Auto-register this class as a listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* -------------------------------------------------------
     * Compass Handling
     * ------------------------------------------------------- */

    /**
     * Gives the player a ProShield compass.
     *
     * @param player Target player
     * @param force  If true, always give a new compass (even if they have one)
     */
    public void giveCompass(Player player, boolean force) {
        if (player == null) return;

        ItemStack compass = buildCompass();

        boolean hasCompass = player.getInventory().containsAtLeast(compass, 1);

        if (!hasCompass || force) {
            player.getInventory().addItem(compass);
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("Gave ProShield compass to " + player.getName());
            }
        }
    }

    private ItemStack buildCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            meta.setLore(List.of(
                    ChatColor.GRAY + "Navigate your claims.",
                    ChatColor.YELLOW + "Right-click to open ProShield GUI."
            ));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            compass.setItemMeta(meta);
        }
        return compass;
    }

    /* -------------------------------------------------------
     * Compass Interaction Listener
     * ------------------------------------------------------- */

    @EventHandler(ignoreCancelled = true)
    public void onCompassUse(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        ItemStack item = event.getItem();
        if (item.getType() != Material.COMPASS || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !ChatColor.stripColor(meta.getDisplayName()).equals("ProShield Compass")) {
            return; // Not our special compass
        }

        Player player = event.getPlayer();

        // Cancel normal compass usage
        event.setCancelled(true);

        // Open GUI (placeholder — can be expanded into actual menus later)
        openMainGUI(player);

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("Player " + player.getName() + " opened the ProShield GUI with their compass.");
        }
    }

    /* -------------------------------------------------------
     * GUI Entrypoint
     * ------------------------------------------------------- */

    public void openMainGUI(Player player) {
        player.sendMessage(ChatColor.AQUA + "[ProShield] " + ChatColor.GRAY + "Opening claim management GUI...");

        // TODO: Replace with your actual GUI menus later.
        // For now, just a friendly placeholder message.
    }

    /* -------------------------------------------------------
     * GUI Cache Access
     * ------------------------------------------------------- */

    public GUICache getCache() {
        return cache;
    }

    public void clearCache() {
        cache.clearCache();
    }
}
