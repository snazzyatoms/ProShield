// path: src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GUIManager {

    public static final String TITLE = ChatColor.DARK_AQUA + "ProShield";
    private final ProShield plugin;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public static ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        meta.setLore(List.of(ChatColor.GRAY + "Right-click to open ProShield"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    public void openMain(org.bukkit.entity.Player p) {
        Inventory inv = Bukkit.createInventory(p, 27, TITLE);

        inv.setItem(11, icon(Material.OAK_SAPLING, ChatColor.GREEN + "Create Claim",
                List.of(ChatColor.GRAY + "Claim the chunk you are standing in")));
        inv.setItem(13, icon(Material.PAPER, ChatColor.AQUA + "Claim Info",
                List.of(ChatColor.GRAY + "View owner & trusted players")));
        inv.setItem(15, icon(Material.BARRIER, ChatColor.RED + "Remove Claim",
                List.of(ChatColor.GRAY + "Unclaim this chunk (owner only)")));

        p.openInventory(inv);
    }

    private ItemStack icon(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }
}
