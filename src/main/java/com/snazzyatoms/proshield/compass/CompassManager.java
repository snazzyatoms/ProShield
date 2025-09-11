package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * CompassManager
 *
 * Handles giving players the ProShield compass and routing clicks to GUIManager.
 */
public class CompassManager {

    private final ProShield plugin;

    public CompassManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Gives a compass to the player.
     *
     * @param player      the player
     * @param adminStyled true if admin (compass styled differently)
     */
    public void giveCompass(Player player, boolean adminStyled) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            if (adminStyled) {
                meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "ProShield Admin Compass");
                meta.setLore(java.util.List.of(
                        ChatColor.GRAY + "Right-click to open the",
                        ChatColor.GRAY + "ProShield admin menu."
                ));
            } else {
                meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "ProShield Compass");
                meta.setLore(java.util.List.of(
                        ChatColor.GRAY + "Right-click to open the",
                        ChatColor.GRAY + "ProShield player menu."
                ));
            }
            meta.addEnchant(Enchantment.UNBREAKING, 1, true); // was DURABILITY
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            compass.setItemMeta(meta);
        }

        // Give without duplicating if already has one
        if (!player.getInventory().contains(compass)) {
            player.getInventory().addItem(compass);
        }
    }

    /**
     * Determines if the given item is a ProShield compass.
     */
    public static boolean isProShieldCompass(ItemStack stack) {
        if (stack == null || stack.getType() != Material.COMPASS) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;

        String dn = ChatColor.stripColor(meta.getDisplayName());
        return dn.equalsIgnoreCase("ProShield Compass")
                || dn.equalsIgnoreCase("ProShield Admin Compass");
    }

    /**
     * Opens the GUI when a player right-clicks with the compass.
     */
    public static void openFromCompass(Player player, ItemStack stack) {
        ProShield plugin = ProShield.getInstance();
        GUIManager gui = plugin.getGuiManager();

        if (stack == null || !isProShieldCompass(stack)) return;

        boolean isAdmin = player.hasPermission("proshield.admin");
        if (isAdmin) {
            gui.openMain(player); // default open main first
            // Admin can then navigate to admin menu
        } else {
            gui.openMain(player);
        }
    }
}
