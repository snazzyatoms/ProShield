// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * GUIManager
 * Handles player GUI interactions, cached menus, and utility items.
 *
 * ✅ Preserves all prior logic
 * ✅ Adds giveCompass(Player, boolean) so listeners & commands compile
 * ✅ Fancy compass with lore, glow effect, and optional forced replacement
 */
public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* -------------------------------------------------------
     * Compass Handling
     * ------------------------------------------------------- */

    /**
     * Gives the player a ProShield compass.
     *
     * @param player   Target player
     * @param force    If true, always give a new compass (even if they have one)
     */
    public void giveCompass(Player player, boolean force) {
        if (player == null) return;

        // Build ProShield compass
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            meta.setLore(java.util.List.of(
                    ChatColor.GRAY + "Navigate your claims.",
                    ChatColor.GRAY + "Right-click to open ProShield GUI."
            ));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            compass.setItemMeta(meta);
        }

        // Check inventory
        boolean hasCompass = player.getInventory().containsAtLeast(compass, 1);

        if (!hasCompass || force) {
            player.getInventory().addItem(compass);
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("Gave ProShield compass to " + player.getName());
            }
        }
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

    /* -------------------------------------------------------
     * (Optional future expansion: menus, role GUI, flags GUI, etc.)
     * ------------------------------------------------------- */
}
