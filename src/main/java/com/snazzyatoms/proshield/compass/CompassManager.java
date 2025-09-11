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
 * - Prevents duplicates in inventory
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
     * Give a compass to a player if they don't already have one.
     *
     * @param player Target player
     * @param admin  If true → admin style compass
     */
    public void giveCompass(Player player, boolean admin) {
        // Prevent duplicates
        if (hasCompass(player)) {
            return;
        }

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        if (meta != null) {
            if (admin) {
                meta.setDisplayName("§cAdmin Compass");
                meta.setLore(Arrays.asList(
                        "§7Right-click to open Admin tools",
                        "§7Includes all normal player menus"
                ));
            } else {
                meta.setDisplayName("§aProShield Compass");
                meta.setLore(Arrays.asList(
                        "§7Right-click to manage claims",
                        "§7Open menus for trust, flags, roles"
                ));
            }

            // Cosmetic enchant to make it glow
            meta.addEnchant(Enchantment.DURABILITY, 1, true); // ✅ safe cross-version
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            compass.setItemMeta(meta);
        }

        player.getInventory().addItem(compass);
    }

    /**
     * Check if player already has a ProShield compass.
     */
    public boolean hasCompass(Player player) {
        return Arrays.stream(player.getInventory().getContents())
                .anyMatch(this::isProShieldCompass);
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

        String name = item.getItemMeta().getDisplayName();
        if (name.contains("Admin")) {
            guiManager.openAdminMenu(player);
        } else {
            guiManager.openMain(player);
        }
    }
}
