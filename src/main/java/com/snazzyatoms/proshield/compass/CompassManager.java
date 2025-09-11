// src/main/java/com/snazzyatoms/proshield/compass/CompassManager.java
package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * CompassManager
 *
 * Handles giving players/admins the ProShield compass.
 * - Prevents duplicates (one per player)
 * - Distinguishes between normal and admin compasses
 * - Provides checks to see if an ItemStack is a ProShield compass
 * - Opens the correct GUI when right-clicked
 */
public class CompassManager {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public CompassManager(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    /**
     * Give a compass to a player, if they don't already have one.
     *
     * @param player Target player
     * @param admin  If true → give Admin compass
     */
    public void giveCompass(Player player, boolean admin) {
        // Check if they already have one
        if (hasCompass(player)) {
            return; // ✅ Do not give duplicates
        }

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        if (meta != null) {
            if (admin) {
                meta.setDisplayName("§cAdmin Compass");
                meta.setLore(Arrays.asList(
                        "§7Right-click to open ProShield menus",
                        "§7Includes Admin tools & player menus"
                ));
            } else {
                meta.setDisplayName("§aProShield Compass");
                meta.setLore(Arrays.asList(
                        "§7Right-click to manage claims",
                        "§7Trust, flags, roles, transfer & more"
                ));
            }

            // Cosmetic glow
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            compass.setItemMeta(meta);
        }

        player.getInventory().addItem(compass);
    }

    /**
     * Check if the player already has a ProShield compass.
     */
    public boolean hasCompass(Player player) {
        PlayerInventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (isProShieldCompass(item)) return true;
        }
        return false;
    }

    /**
     * Check if an ItemStack is a ProShield compass.
     */
    public boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;

        String name = item.getItemMeta().getDisplayName();
        return name.contains("ProShield Compass") || name.contains("Admin Compass");
    }

    /**
     * Open the correct GUI when right-clicked with compass.
     */
    public void openFromCompass(Player player, ItemStack item) {
        if (!isProShieldCompass(item)) return;

        // Always open player GUI
        guiManager.openMain(player);

        // Admins get notified they can access extra tools
        if (player.isOp() || player.hasPermission("proshield.admin")) {
            plugin.getMessagesUtil().send(player, "prefix",
                    "&eOpened ProShield menu &7(Admin features available).");
        }
    }
}
