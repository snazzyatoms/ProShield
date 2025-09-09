package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
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
    public static final String TITLE_ADMIN = ChatColor.DARK_AQUA + "ProShield Admin";

    private final ProShield plugin;
    private final PlotManager plots;

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    public void reloadIconsFromConfig() {
        // reserved for dynamic skins; safe no-op for now
    }

    public static ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        m.setLore(List.of(
                ChatColor.GRAY + "Right-click to open the ProShield menu.",
                ChatColor.DARK_GRAY + "Use the Admin tab for tools."
        ));
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(m);
        return it;
    }

    public void openMain(org.bukkit.entity.Player p) {
        Inventory inv = Bukkit.createInventory(p, 27, TITLE);

        inv.setItem(11, named(Material.EMERALD_BLOCK, "&aCreate/Claim"));
        inv.setItem(13, named(Material.PAPER, "&bClaim Info"));
        inv.setItem(15, named(Material.BARRIER, "&cUnclaim"));

        inv.setItem(22, named(Material.BOOK, "&eHelp"));
        inv.setItem(26, named(Material.REDSTONE_TORCH, "&6Admin")); // open admin GUI (if perm)

        p.openInventory(inv);
    }

    public void openHelp(org.bukkit.entity.Player p) {
        Inventory inv = Bukkit.createInventory(p, 27, ChatColor.DARK_AQUA + "ProShield Help");

        inv.setItem(10, named(Material.MAP, "&f/proshield claim"));
        inv.setItem(11, named(Material.MAP, "&f/proshield unclaim"));
        inv.setItem(12, named(Material.MAP, "&f/proshield info"));
        inv.setItem(13, named(Material.MAP, "&f/proshield trust <player> [role]"));
        inv.setItem(14, named(Material.MAP, "&f/proshield untrust <player>"));
        inv.setItem(15, named(Material.MAP, "&f/proshield trusted"));

        // show admin ones only if they have perm
        if (p.hasPermission("proshield.admin")) {
            inv.setItem(16, named(Material.COMPASS, "&f/proshield preview [s]"));
            inv.setItem(19, named(Material.COMPASS, "&f/proshield transfer <player>"));
            inv.setItem(20, named(Material.REPEATER, "&f/proshield purgeexpired <days> [dryrun]"));
            inv.setItem(21, named(Material.LEVER, "&f/proshield debug <on|off|toggle>"));
            inv.setItem(22, named(Material.LEVER, "&f/proshield reload"));
        }

        // Back button
        inv.setItem(26, named(Material.ARROW, "&7Back"));

        p.openInventory(inv);
    }

    public void openAdmin(org.bukkit.entity.Player p) {
        Inventory inv = Bukkit.createInventory(p, 36, TITLE_ADMIN);

        inv.setItem(10, named(Material.COMPASS, "&fPreview Borders"));
        inv.setItem(12, named(Material.NAME_TAG, "&fTransfer Ownership"));
        inv.setItem(14, named(Material.HOPPER, "&fExpired Claims (Purge)"));
        inv.setItem(16, named(Material.REDSTONE_TORCH, "&fToggle Debug"));

        // Item-keep quick view (status)
        boolean keep = plugin.getConfig().getBoolean("item-keep.enabled", false);
        inv.setItem(22, named(keep ? Material.CLOCK : Material.GRAY_DYE,
                keep ? "&aItem-keep: Enabled" : "&7Item-keep: Disabled"));

        // Back button
        inv.setItem(33, named(Material.ARROW, "&7Back"));

        p.openInventory(inv);
    }

    private ItemStack named(Material m, String name) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }
}
