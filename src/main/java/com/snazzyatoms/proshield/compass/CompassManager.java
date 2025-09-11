// src/main/java/com/snazzyatoms/proshield/compass/CompassManager.java
package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * CompassManager
 *
 * Handles giving players/admins the ProShield compass.
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
     * Give a compass to a player.
     *
     * @param player Target player
     * @param admin  If true → admin style compass
     */
    public void giveCompass(Player player, boolean admin) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        if (meta != null) {
            if (admin) {
                meta.setDisplayName("§cAdmin Compass");
                meta.setLore(Arrays.asList(
                        "§7Right-click to open ProShield menus",
                        "§7Extra admin tools available"
                ));
            } else {
                meta.setDisplayName("§aProShield Compass");
                meta.setLore(Arrays.asList(
                        "§7Right-click to manage claims",
                        "§7Trust, flags, roles and more"
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

        // ✅ Always open the main menu first
        guiManager.openMain(player);

        // ✅ Admins will see the "Admin Tab" inside the main GUI
        if (player.isOp() || player.hasPermission("proshield.admin")) {
            // Optionally highlight the admin tab or send a message
            plugin.getMessagesUtil().send(player, "prefix",
                    "&eOpened ProShield menu &7(Admin features available).");
        }
    }
}
