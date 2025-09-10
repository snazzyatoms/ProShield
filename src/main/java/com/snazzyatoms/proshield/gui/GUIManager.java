package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI Manager for ProShield
 * Handles Player GUI + Admin GUI + Cache
 */
public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final GUICache cache;

    public GUIManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.cache = new GUICache();
    }

    // ========================
    // INVENTORY CREATION UTILS
    // ========================

    private ItemStack makeItem(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fill(Inventory inv, Material mat) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, new ItemStack(mat));
            }
        }
    }

    // ========================
    // PLAYER GUI
    // ========================

    public void openPlayerMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "ProShield Menu");

        inv.setItem(11, makeItem(Material.GRASS_BLOCK, "&aClaim Chunk", "&7Protect this land"));
        inv.setItem(13, makeItem(Material.BOOK, "&eClaim Info", "&7View info about this claim"));
        inv.setItem(15, makeItem(Material.BARRIER, "&cUnclaim", "&7Remove this claim"));
        inv.setItem(31, makeItem(Material.PAPER, "&bHelp", "&7Show ProShield help"));
        inv.setItem(33, makeItem(Material.COMPASS, "&cAdmin Menu", "&7Admins only"));
        inv.setItem(48, makeItem(Material.ARROW, "&fBack", "&7Return to previous menu"));

        fill(inv, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(inv);
        cache.setOpenGUI(player, "main");
    }

    // ========================
    // ADMIN GUI
    // ========================

    public void openAdmin(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "ProShield Admin Menu");

        // Core protections
        inv.setItem(10, makeItem(Material.FLINT_AND_STEEL, "&cFire Toggle"));
        inv.setItem(11, makeItem(Material.TNT, "&cExplosions Toggle"));
        inv.setItem(12, makeItem(Material.ENDERMAN_SPAWN_EGG, "&cEntity Grief Toggle"));
        inv.setItem(13, makeItem(Material.LEVER, "&cInteractions Toggle"));
        inv.setItem(14, makeItem(Material.IRON_SWORD, "&cPvP Toggle"));

        // Utility
        inv.setItem(20, makeItem(Material.CHEST, "&bKeep Items Toggle"));
        inv.setItem(21, makeItem(Material.CLOCK, "&ePurge Expired"));
        inv.setItem(22, makeItem(Material.REDSTONE, "&eReload Configs"));
        inv.setItem(23, makeItem(Material.COMMAND_BLOCK, "&bDebug Mode"));
        inv.setItem(24, makeItem(Material.COMPASS, "&aCompass Drop Full"));
        inv.setItem(25, makeItem(Material.BEACON, "&eSpawn Guard"));
        inv.setItem(30, makeItem(Material.ENDER_PEARL, "&aTeleport Tools"));

        // NEW Mob Control
        inv.setItem(32, makeItem(Material.ZOMBIE_SPAWN_EGG, "&cMob Spawn Toggle", "&7Block/Allow natural mob spawns"));
        inv.setItem(33, makeItem(Material.SKELETON_SKULL, "&cMob Despawn Toggle", "&7Despawn mobs inside claims"));
        inv.setItem(34, makeItem(Material.SLIME_BALL, "&aMob Repel Toggle", "&7Push mobs back at claim borders"));
        inv.setItem(41, makeItem(Material.SLIME_BLOCK, "&aRepel Radius", "&7Current: " + plugin.getConfig().getDouble("protection.mobs.border-repel.radius")));
        inv.setItem(42, makeItem(Material.PISTON, "&aRepel Force", "&7H: " + plugin.getConfig().getDouble("protection.mobs.border-repel.horizontal-push"),
                                                             "&7V: " + plugin.getConfig().getDouble("protection.mobs.border-repel.vertical-push")));

        inv.setItem(48, makeItem(Material.ARROW, "&fBack", "&7Return to previous menu"));
        inv.setItem(49, makeItem(Material.PAPER, "&bHelp"));

        fill(inv, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(inv);
        cache.setOpenGUI(player, "admin");
    }

    // ========================
    // CACHE / LOOKUP
    // ========================

    public boolean isOurInventory(Player player, String title) {
        String open = cache.getOpenGUI(player);
        return open != null && title.contains("ProShield");
    }

    public void handleButtonClick(Player player, int slot) {
        String gui = cache.getOpenGUI(player);
        if (gui == null) return;

        if (gui.equals("main")) {
            switch (slot) {
                case 11: player.performCommand("proshield claim"); break;
                case 13: player.performCommand("proshield info"); break;
                case 15: player.performCommand("proshield unclaim"); break;
                case 31: player.performCommand("proshield help"); break;
                case 33: openAdmin(player); break;
                case 48: player.closeInventory(); break;
            }
        } else if (gui.equals("admin")) {
            switch (slot) {
                case 10: player.performCommand("proshield toggle fire"); break;
                case 11: player.performCommand("proshield toggle explosions"); break;
                case 12: player.performCommand("proshield toggle entitygrief"); break;
                case 13: player.performCommand("proshield toggle interactions"); break;
                case 14: player.performCommand("proshield toggle pvp"); break;
                case 20: player.performCommand("proshield toggle keepitems"); break;
                case 21: player.performCommand("proshield purgeexpired"); break;
                case 22: player.performCommand("proshield reload"); break;
                case 23: player.performCommand("proshield debug toggle"); break;
                case 24: player.performCommand("proshield toggle compassdrop"); break;
                case 25: player.performCommand("proshield toggle spawnguard"); break;
                case 30: player.performCommand("proshield admintools tp"); break;

                // NEW Mob control
                case 32: player.performCommand("proshield toggle mobspawn"); break;
                case 33: player.performCommand("proshield toggle mobdespawn"); break;
                case 34: player.performCommand("proshield toggle repel"); break;
                case 41: player.performCommand("proshield set repelradius"); break;
                case 42: player.performCommand("proshield set repelforce"); break;

                case 48: openPlayerMain(player); break;
                case 49: player.performCommand("proshield help"); break;
            }
        }
    }
}
