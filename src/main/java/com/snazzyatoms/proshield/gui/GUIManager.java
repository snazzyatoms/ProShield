package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    public static final String MAIN_TITLE = ChatColor.AQUA + "ProShield";
    public static final String ADMIN_TITLE = ChatColor.GOLD + "ProShield Admin";
    public static final String CLAIMS_TITLE = ChatColor.YELLOW + "Claims Nearby";

    private final ProShield plugin;

    public GUIManager(ProShield plugin) { this.plugin = plugin; }

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(p, 27, MAIN_TITLE);
        inv.setItem(11, named(Material.GRASS_BLOCK, ChatColor.GREEN + "Create Claim"));
        inv.setItem(13, named(Material.PAPER, ChatColor.AQUA + "Claim Info"));
        inv.setItem(15, named(Material.BARRIER, ChatColor.RED + "Remove Claim"));
        p.openInventory(inv);
    }

    public void openAdminMenu(Player p) {
        Inventory inv = Bukkit.createInventory(p, 27, ADMIN_TITLE);
        inv.setItem(10, named(Material.LEVER, ChatColor.YELLOW + "Toggle Bypass"));
        inv.setItem(12, named(Material.COMPASS, ChatColor.GOLD + "Give Admin Compass"));
        inv.setItem(14, named(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "Teleport to Claim"));
        inv.setItem(16, named(Material.BARRIER, ChatColor.RED + "Force Unclaim (Here)"));
        inv.setItem(22, named(Material.REPEATER, ChatColor.AQUA + "Reload Configs"));
        p.openInventory(inv);
    }

    public void openAdminClaims(Player p, List<String> claimKeys) {
        int size = Math.min(54, Math.max(9, ((claimKeys.size() + 8) / 9) * 9));
        Inventory inv = Bukkit.createInventory(p, size, CLAIMS_TITLE);
        for (int i = 0; i < Math.min(size, claimKeys.size()); i++) {
            String key = claimKeys.get(i);
            inv.setItem(i, named(Material.MAP, ChatColor.YELLOW + key));
        }
        p.openInventory(inv);
    }

    public static ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "ProShield Compass");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-Click: Open ProShield");
        lore.add(ChatColor.DARK_GRAY + "Sneak + Right-Click: Admin Menu");
        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack named(Material mat, String name) {
        ItemStack is = new ItemStack(mat);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        is.setItemMeta(im);
        return is;
    }
}
