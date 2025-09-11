package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Iterator;

/**
 * CompassManager
 *
 * ✅ Centralized compass logic
 * ✅ Player compass (normal look, aqua-themed)
 * ✅ Admin compass (golden + glowing)
 * ✅ Prevents duplicate compasses
 * ✅ Used by both /compass command & PlayerJoinListener
 */
public class CompassManager {

    private static final String PLAYER_COMPASS_NAME = ChatColor.AQUA + "ProShield Compass";
    private static final String ADMIN_COMPASS_NAME = ChatColor.GOLD + "ProShield Admin Compass";

    /**
     * Gives the correct compass to a player.
     * If the player is op or has proshield.admin → Admin compass.
     * Otherwise → Player compass.
     *
     * @param player Player receiving the compass
     * @param replace If true, remove existing ProShield compasses before giving a new one
     */
    public static void giveCompass(Player player, boolean replace) {
        if (replace) {
            removeOldCompasses(player);
        }

        ItemStack compass = player.isOp() || player.hasPermission("proshield.admin")
                ? createAdminCompass()
                : createPlayerCompass();

        // Only give if player doesn’t already have it
        if (!hasCompass(player, compass)) {
            player.getInventory().addItem(compass);
        }
    }

    /**
     * Removes old ProShield compasses from the player’s inventory.
     */
    private static void removeOldCompasses(Player player) {
        Iterator<ItemStack> it = player.getInventory().iterator();
        while (it.hasNext()) {
            ItemStack stack = it.next();
            if (stack == null || stack.getType() != Material.COMPASS) continue;
            if (!stack.hasItemMeta()) continue;

            ItemMeta meta = stack.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) continue;

            String name = ChatColor.stripColor(meta.getDisplayName());
            if (name.equalsIgnoreCase("ProShield Compass") ||
                name.equalsIgnoreCase("ProShield Admin Compass")) {
                it.remove();
            }
        }
    }

    /**
     * Checks if player already has this type of compass.
     */
    private static boolean hasCompass(Player player, ItemStack compass) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (!item.hasItemMeta() || !compass.hasItemMeta()) continue;
            if (item.getType() == Material.COMPASS &&
                ChatColor.stripColor(item.getItemMeta().getDisplayName())
                        .equalsIgnoreCase(ChatColor.stripColor(compass.getItemMeta().getDisplayName()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds the normal Player Compass.
     */
    private static ItemStack createPlayerCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = compass.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(PLAYER_COMPASS_NAME);
            meta.setLore(java.util.Arrays.asList(
                    ChatColor.GRAY + "Right-click to open ProShield menu",
                    ChatColor.DARK_AQUA + "Manage claims, trust players, and flags"
            ));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            compass.setItemMeta(meta);
        }
        return compass;
    }

    /**
     * Builds the glowing Admin Compass.
     */
    private static ItemStack createAdminCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = compass.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ADMIN_COMPASS_NAME);
            meta.setLore(java.util.Arrays.asList(
                    ChatColor.GRAY + "Right-click for ProShield menu",
                    ChatColor.GOLD + "Includes Admin Tools & Flags"
            ));
            meta.addEnchant(Enchantment.DURABILITY, 1, true); // glow
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            compass.setItemMeta(meta);
        }
        return compass;
    }
}
