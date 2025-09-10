package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIManager {
    private final ProShield plugin;
    private final PlotManager plots;
    private final GUICache cache;

    public GUIManager(ProShield plugin, PlotManager plots, GUICache cache) {
        this.plugin = plugin;
        this.plots = plots;
        this.cache = cache;
    }

    // --- Utility to build an item ---
    private ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    // --- Fill background ---
    private void fillBackground(Inventory inv) {
        ItemStack filler = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    // --- Main GUI ---
    public void openMain(Player player, boolean adminView) {
        Inventory inv = cache.getOrBuild("main", () -> {
            Inventory gui = Bukkit.createInventory(null, 54, "ProShield Menu");
            ConfigurationSection slots = plugin.getConfig().getConfigurationSection("gui.slots.main");

            if (slots != null) {
                gui.setItem(slots.getInt("create"), makeItem(Material.GRASS_BLOCK, "§aClaim Chunk"));
                gui.setItem(slots.getInt("info"), makeItem(Material.BOOK, "§bClaim Info"));
                gui.setItem(slots.getInt("remove"), makeItem(Material.BARRIER, "§cUnclaim Chunk"));
                gui.setItem(slots.getInt("help"), makeItem(Material.PAPER, "§eHelp"));
                gui.setItem(slots.getInt("admin"), makeItem(Material.COMPASS, "§6Admin Menu"));
                gui.setItem(slots.getInt("back"), makeItem(Material.ARROW, "§7Back"));
            }

            fillBackground(gui);
            return gui;
        });

        player.openInventory(inv);
    }

    // --- Admin GUI ---
    public void openAdmin(Player player) {
        Inventory inv = cache.getOrBuild("admin", () -> {
            Inventory gui = Bukkit.createInventory(null, 54, "ProShield Admin");
            ConfigurationSection slots = plugin.getConfig().getConfigurationSection("gui.slots.admin");

            if (slots != null) {
                gui.setItem(slots.getInt("fire"), makeItem(Material.FLINT_AND_STEEL, "§cToggle Fire"));
                gui.setItem(slots.getInt("explosions"), makeItem(Material.TNT, "§cToggle Explosions"));
                gui.setItem(slots.getInt("entity-grief"), makeItem(Material.ENDERMAN_SPAWN_EGG, "§cEntity Grief"));
                gui.setItem(slots.getInt("interactions"), makeItem(Material.OAK_DOOR, "§cToggle Interactions"));
                gui.setItem(slots.getInt("pvp"), makeItem(Material.DIAMOND_SWORD, "§cToggle PvP"));
                gui.setItem(slots.getInt("keep-items"), makeItem(Material.CHEST, "§cToggle Item Keep"));
                gui.setItem(slots.getInt("purge-expired"), makeItem(Material.LAVA_BUCKET, "§cPurge Expired Claims"));
                gui.setItem(slots.getInt("reload"), makeItem(Material.REDSTONE, "§cReload Config"));
                gui.setItem(slots.getInt("debug"), makeItem(Material.COMPARATOR, "§cDebug Mode"));
                gui.setItem(slots.getInt("compass-drop-if-full"), makeItem(Material.COMPASS, "§cCompass Drop Toggle"));
                gui.setItem(slots.getInt("spawn-guard"), makeItem(Material.BEDROCK, "§cSpawn Guard"));
                gui.setItem(slots.getInt("tp-tools"), makeItem(Material.ENDER_PEARL, "§cTeleport Tools"));
                gui.setItem(slots.getInt("help"), makeItem(Material.PAPER, "§eHelp"));
                gui.setItem(slots.getInt("back"), makeItem(Material.ARROW, "§7Back"));
            }

            fillBackground(gui);
            return gui;
        });

        player.openInventory(inv);
    }

    // --- Player GUI (NEW in 1.2.5) ---
    public void openPlayer(Player player) {
        Inventory inv = cache.getOrBuild("player", () -> {
            Inventory gui = Bukkit.createInventory(null, 54, "ProShield Player");
            ConfigurationSection slots = plugin.getConfig().getConfigurationSection("gui.slots.player");

            if (slots != null) {
                gui.setItem(slots.getInt("trust"), makeItem(Material.PLAYER_HEAD, "§aTrust Player"));
                gui.setItem(slots.getInt("untrust"), makeItem(Material.ZOMBIE_HEAD, "§cUntrust Player"));
                gui.setItem(slots.getInt("roles"), makeItem(Material.NAME_TAG, "§bManage Roles"));
                gui.setItem(slots.getInt("transfer"), makeItem(Material.WRITABLE_BOOK, "§eTransfer Claim"));
                gui.setItem(slots.getInt("flags"), makeItem(Material.BANNER, "§dClaim Flags"));
                gui.setItem(slots.getInt("preview"), makeItem(Material.GLASS, "§fPreview Claim"));
                gui.setItem(slots.getInt("help"), makeItem(Material.PAPER, "§eHelp"));
                gui.setItem(slots.getInt("back"), makeItem(Material.ARROW, "§7Back"));
            }

            fillBackground(gui);
            return gui;
        });

        player.openInventory(inv);
    }

    // --- Cache invalidation ---
    public void reloadGUICache() {
        cache.clear();
    }
}
