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

public class CompassManager {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public CompassManager(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void giveCompass(Player player, boolean admin) {
        // ✅ Prevent duplicate compasses
        for (ItemStack item : player.getInventory().getContents()) {
            if (isProShieldCompass(item)) {
                return; // Already has one
            }
        }

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

            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            compass.setItemMeta(meta);
        }

        player.getInventory().addItem(compass);
    }

    public boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;

        String name = item.getItemMeta().getDisplayName();
        return name.contains("ProShield Compass") || name.contains("Admin Compass");
    }

    public void openFromCompass(Player player, ItemStack item) {
        if (!isProShieldCompass(item)) return;

        if (player.isOp() || player.hasPermission("proshield.admin")) {
            guiManager.openAdminMain(player);
        } else {
            guiManager.openMain(player);
        }
    }
}
