package com.snazzyatoms.proshield.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIManager {

    public static final String TITLE = ChatColor.DARK_GREEN + "Claim Management";

    public void openClaimGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        // Filler
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 0; i < gui.getSize(); i++) gui.setItem(i, filler);

        // Create
        ItemStack create = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta cMeta = create.getItemMeta();
        cMeta.setDisplayName(ChatColor.GREEN + "Create Claim");
        cMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Protect this area from others.",
                ChatColor.YELLOW + "Click to create your claim here."
        ));
        create.setItemMeta(cMeta);

        // Info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta iMeta = info.getItemMeta();
        iMeta.setDisplayName(ChatColor.AQUA + "Claim Info");
        iMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "View your claim details.",
                ChatColor.YELLOW + "Click to show center & radius."
        ));
        info.setItemMeta(iMeta);

        // Remove
        ItemStack remove = new ItemStack(Material.BARRIER);
        ItemMeta rMeta = remove.getItemMeta();
        rMeta.setDisplayName(ChatColor.RED + "Remove Claim");
        rMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Delete your claim permanently.",
                ChatColor.YELLOW + "Click to remove."
        ));
        remove.setItemMeta(rMeta);

        gui.setItem(11, create);
        gui.setItem(13, info);
        gui.setItem(15, remove);

        player.openInventory(gui);
    }
}
