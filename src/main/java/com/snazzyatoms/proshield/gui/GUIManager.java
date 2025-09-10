package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* ========================
       UTILS
    ======================== */

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fill(Inventory inv, ItemStack item) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, item);
            }
        }
    }

    /* ========================
       PLAYER GUI
    ======================== */

    public Inventory createPlayerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "ProShield Menu");

        inv.setItem(11, createItem(Material.GRASS_BLOCK, "§aClaim Land", "Claim this chunk"));
        inv.setItem(13, createItem(Material.BOOK, "§eClaim Info", "View claim details"));
        inv.setItem(15, createItem(Material.BARRIER, "§cUnclaim Land", "Unclaim this chunk"));

        inv.setItem(20, createItem(Material.PLAYER_HEAD, "§bTrust Player", "Grant access to a player"));
        inv.setItem(21, createItem(Material.SKELETON_SKULL, "§cUntrust Player", "Remove a player from claim"));
        inv.setItem(22, createItem(Material.NAME_TAG, "§eManage Roles", "Edit roles for trusted players"));
        inv.setItem(23, createItem(Material.PAPER, "§dTransfer Claim", "Transfer ownership of this claim"));
        inv.setItem(24, createItem(Material.REDSTONE, "§6Claim Flags", "Toggle PvP, fire, explosions, etc."));

        inv.setItem(31, createItem(Material.BOOKSHELF, "§9Help", "List commands and usage"));
        inv.setItem(48, createItem(Material.ARROW, "§7Back", "Return to previous menu"));

        fill(inv, createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        return inv;
    }

    /* ========================
       ADMIN GUI
    ======================== */

    public Inventory createAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "ProShield Admin Menu");

        inv.setItem(10, createItem(Material.FIRE_CHARGE, "§cToggle Fire"));
        inv.setItem(11, createItem(Material.TNT, "§cToggle Explosions"));
        inv.setItem(12, createItem(Material.ENDER_PEARL, "§cToggle Entity Grief"));
        inv.setItem(13, createItem(Material.DIAMOND_SWORD, "§cToggle PvP"));
        inv.setItem(14, createItem(Material.CHEST, "§cToggle Keep Items"));

        inv.setItem(20, createItem(Material.BUCKET, "§ePurge Expired Claims"));
        inv.setItem(21, createItem(Material.BOOK, "§eReload Config"));
        inv.setItem(22, createItem(Material.COMPASS, "§eCompass Settings"));
        inv.setItem(23, createItem(Material.REDSTONE_TORCH, "§eDebug Mode"));
        inv.setItem(24, createItem(Material.BARRIER, "§eSpawn Guard"));

        // NEW mob controls
        boolean repelEnabled = plugin.getConfig().getBoolean("protection.mobs.border-repel.enabled", true);
        boolean despawnEnabled = plugin.getConfig().getBoolean("protection.mobs.despawn-inside-claims", true);

        inv.setItem(28, createItem(
                repelEnabled ? Material.SHIELD : Material.GRAY_DYE,
                "§bMob Repel",
                repelEnabled ? "§aEnabled - mobs are pushed back" : "§cDisabled - mobs can walk in"
        ));

        inv.setItem(29, createItem(
                despawnEnabled ? Material.ENDER_EYE : Material.ENDER_PEARL,
                "§bMob Despawn Inside",
                despawnEnabled ? "§aEnabled - mobs despawn inside claims" : "§cDisabled - mobs can exist inside"
        ));

        inv.setItem(30, createItem(Material.ENDER_CHEST, "§eTeleport Tools"));
        inv.setItem(31, createItem(Material.ARROW, "§7Back"));
        inv.setItem(32, createItem(Material.BOOKSHELF, "§9Help"));

        fill(inv, createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        return inv;
    }

    /* ========================
       EVENT HOOK
    ======================== */

    public void handleButtonClick(Player player, String title, int slot) {
        if (title.contains("Admin")) {
            switch (slot) {
                case 10: player.performCommand("proshield admin toggle fire"); break;
                case 11: player.performCommand("proshield admin toggle explosions"); break;
                case 12: player.performCommand("proshield admin toggle entitygrief"); break;
                case 13: player.performCommand("proshield admin toggle pvp"); break;
                case 14: player.performCommand("proshield admin toggle keepitems"); break;
                case 20: player.performCommand("proshield purgeexpired"); break;
                case 21: player.performCommand("proshield reload"); break;
                case 22: player.performCommand("proshield admin compass"); break;
                case 23: player.performCommand("proshield debug toggle"); break;
                case 24: player.performCommand("proshield admin spawnguard"); break;

                case 28: // Toggle Mob Repel
                    boolean repel = !plugin.getConfig().getBoolean("protection.mobs.border-repel.enabled", true);
                    plugin.getConfig().set("protection.mobs.border-repel.enabled", repel);
                    plugin.saveConfig();
                    plugin.reloadAllConfigs();
                    player.sendMessage("§3[ProShield]§r Mob Repel is now " + (repel ? "§aENABLED" : "§cDISABLED"));
                    openAdminMenu(player);
                    break;

                case 29: // Toggle Mob Despawn Inside
                    boolean despawn = !plugin.getConfig().getBoolean("protection.mobs.despawn-inside-claims", true);
                    plugin.getConfig().set("protection.mobs.despawn-inside-claims", despawn);
                    plugin.saveConfig();
                    plugin.reloadAllConfigs();
                    player.sendMessage("§3[ProShield]§r Mob Despawn Inside Claims is now " + (despawn ? "§aENABLED" : "§cDISABLED"));
                    openAdminMenu(player);
                    break;

                case 30: player.performCommand("proshield admin tp-tools"); break;
                case 31: openPlayerMenu(player); break;
                case 32: player.performCommand("proshield help"); break;
            }
        } else { // Player menu
            switch (slot) {
                case 11: player.performCommand("proshield claim"); break;
                case 13: player.performCommand("proshield info"); break;
                case 15: player.performCommand("proshield unclaim"); break;
                case 20: player.performCommand("proshield trustmenu"); break;
                case 21: player.performCommand("proshield untrustmenu"); break;
                case 22: player.performCommand("proshield rolemenu"); break;
                case 23: player.performCommand("proshield transfermenu"); break;
                case 24: player.performCommand("proshield flagmenu"); break;
                case 31: player.performCommand("proshield help"); break;
                case 48: player.closeInventory(); break;
            }
        }
    }

    /* ========================
       OPENERS
    ======================== */

    public void openPlayerMenu(Player player) {
        player.openInventory(createPlayerMenu(player));
    }

    public void openAdminMenu(Player player) {
        player.openInventory(createAdminMenu(player));
    }
}
