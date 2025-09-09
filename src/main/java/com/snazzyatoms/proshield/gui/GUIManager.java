// path: src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    public static final String TITLE = ChatColor.AQUA + "ProShield";
    private static final NamespacedKey KEY_COMPASS = new NamespacedKey("proshield", "admin_compass");

    private final ProShield plugin;
    private final PlotManager plots;

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    // --- COMPASS ---

    /** Create the tagged ProShield Admin Compass. */
    public ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click to open the ProShield menu");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        // Tag it
        meta.getPersistentDataContainer().set(KEY_COMPASS, PersistentDataType.BYTE, (byte)1);
        it.setItemMeta(meta);
        return it;
    }

    /** Check if item is the tagged ProShield compass. */
    public static boolean isProShieldCompass(ItemStack it) {
        if (it == null || it.getType() != Material.COMPASS) return false;
        if (!it.hasItemMeta()) return false;
        var meta = it.getItemMeta();
        var pdc = meta.getPersistentDataContainer();
        Byte val = pdc.get(new NamespacedKey("proshield", "admin_compass"), PersistentDataType.BYTE);
        return val != null && val == (byte)1;
    }

    /** Give compass with “inventory full” fallback per config. */
    public void giveCompass(Player p, boolean silent) {
        ItemStack compass = createAdminCompass();
        var inv = p.getInventory();
        var add = inv.addItem(compass);
        if (!add.isEmpty()) {
            boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
            if (dropIfFull) {
                p.getWorld().dropItemNaturally(p.getLocation(), compass);
                if (!silent) p.sendMessage(plugin.getConfig().getString("messages.prefix", "") +
                        ChatColor.YELLOW + "Inventory full—dropped a ProShield Compass at your feet.");
            } else {
                if (!silent) p.sendMessage(plugin.getConfig().getString("messages.prefix", "") +
                        ChatColor.RED + "Inventory full. Free a slot or use /proshield compass.");
            }
        } else if (!silent) {
            p.sendMessage(plugin.getConfig().getString("messages.prefix", "") +
                    ChatColor.GREEN + "Gave you a ProShield Compass.");
        }
    }

    // --- GUI OPENERS ---

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE);
        // minimal layout so opening works even if items not fully configured
        inv.setItem(11, item(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Chunk", List.of(ChatColor.GRAY + "Claim the current chunk")));
        inv.setItem(13, item(Material.PAPER, ChatColor.AQUA + "Claim Info", List.of(ChatColor.GRAY + "View owner & trusted")));
        inv.setItem(15, item(Material.BARRIER, ChatColor.RED + "Unclaim Chunk", List.of(ChatColor.GRAY + "Remove your claim")));
        inv.setItem(31, item(Material.BOOK, ChatColor.BLUE + "Help", List.of(ChatColor.GRAY + "See commands you can use")));
        inv.setItem(33, item(Material.NETHER_STAR, ChatColor.GOLD + "Admin", List.of(ChatColor.GRAY + "Admin tools (if permitted)")));
        p.openInventory(inv);
    }

    private ItemStack item(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    // Called after /proshield reload
    public void onConfigReload() {
        // keep for future dynamic slot remaps, etc.
    }

    public void registerCompassRecipe() {
        // Optional: if you had a custom recipe, re-add it here
        // (left empty to avoid compile issues if not used)
    }
}
