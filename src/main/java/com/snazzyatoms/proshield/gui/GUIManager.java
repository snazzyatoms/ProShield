// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* ---------------------------------------------------------
     * MAIN MENUS
     * --------------------------------------------------------- */

    public void openMain(Player player) {
        String title = plugin.getConfig().getString("gui.main.title", "§bProShield Menu");
        Inventory inv = Bukkit.createInventory(player, 27, title);

        fill(inv);

        inv.setItem(10, itemFromConfig("gui.tooltips.claim", Material.GRASS_BLOCK, "§aClaim Chunk"));
        inv.setItem(11, itemFromConfig("gui.tooltips.unclaim", Material.BARRIER, "§cUnclaim Chunk"));
        inv.setItem(12, itemFromConfig("gui.tooltips.info", Material.BOOK, "§eClaim Info"));
        inv.setItem(13, itemFromConfig("gui.tooltips.trust", Material.PAPER, "§aTrust Menu"));
        inv.setItem(14, itemFromConfig("gui.tooltips.untrust", Material.BOOKSHELF, "§cUntrust Menu"));
        inv.setItem(15, itemFromConfig("gui.tooltips.flags", Material.IRON_SWORD, "§cFlags"));
        inv.setItem(16, itemFromConfig("gui.tooltips.roles", Material.NAME_TAG, "§dRoles"));
        inv.setItem(22, itemFromConfig("gui.tooltips.back", Material.ARROW, "§7Back"));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openAdminMain(Player player) {
        String title = plugin.getConfig().getString("gui.admin.title", "§4ProShield Admin Menu");
        Inventory inv = Bukkit.createInventory(player, 27, title);

        fill(inv);

        // Admin tools
        inv.setItem(10, itemFromConfig("gui.tooltips.admin-teleport", Material.COMPASS, "§bTeleport to Claim"));
        inv.setItem(11, itemFromConfig("gui.tooltips.admin-unclaim", Material.TNT, "§cForce Unclaim"));
        inv.setItem(12, itemFromConfig("gui.tooltips.flags", Material.CHEST, "§6Manage Flags"));
        inv.setItem(13, itemFromConfig("gui.tooltips.admin-purge", Material.HOPPER, "§ePurge Claims"));
        inv.setItem(14, itemFromConfig("gui.tooltips.transfer", Material.BOOK, "§dTransfer Claim"));
        inv.setItem(22, itemFromConfig("gui.tooltips.back", Material.ARROW, "§7Back"));

        cache.setAdminMenu(player, inv);
        player.openInventory(inv);
    }

    /* ---------------------------------------------------------
     * SUB MENUS
     * --------------------------------------------------------- */

    public void openFlagsMenu(Player player, boolean fromAdmin) {
        String title = plugin.getConfig().getString("gui.flags.title", "§dClaim Flags");
        Inventory inv = Bukkit.createInventory(player, 27, title);

        fill(inv);

        Plot plot = plugin.getPlotManager().getPlot(player.getLocation());
        PlotSettings settings = plot != null ? plot.getSettings() : new PlotSettings();

        inv.setItem(10, statefulItem("gui.tooltips.pvp", Material.IRON_SWORD, "§cPvP", settings.isPvpEnabled()));
        inv.setItem(11, statefulItem("gui.tooltips.explosions", Material.TNT, "§4Explosions", settings.isExplosionsAllowed()));
        inv.setItem(12, statefulItem("gui.tooltips.fire", Material.FLINT_AND_STEEL, "§6Fire", settings.isFireAllowed()));
        inv.setItem(13, statefulItem("gui.tooltips.redstone", Material.REDSTONE, "§cRedstone", settings.isRedstoneAllowed()));
        inv.setItem(14, statefulItem("gui.tooltips.containers", Material.CHEST, "§aContainers", settings.isContainersAllowed()));
        inv.setItem(15, statefulItem("gui.tooltips.buckets", Material.BUCKET, "§bBuckets", settings.isBucketAllowed()));
        inv.setItem(16, statefulItem("gui.tooltips.itemframes", Material.ITEM_FRAME, "§6Item Frames", settings.isItemFramesAllowed()));
        inv.setItem(20, statefulItem("gui.tooltips.armorstands", Material.ARMOR_STAND, "§eArmor Stands", settings.isArmorStandsAllowed()));
        inv.setItem(21, statefulItem("gui.tooltips.animals", Material.SADDLE, "§aAnimals", settings.isAnimalAccessAllowed()));
        inv.setItem(22, statefulItem("gui.tooltips.pets", Material.BONE, "§dPets", settings.isPetAccessAllowed()));
        inv.setItem(23, statefulItem("gui.tooltips.vehicles", Material.MINECART, "§2Vehicles", settings.isVehiclesAllowed()));
        inv.setItem(24, statefulItem("gui.tooltips.entitygrief", Material.ROTTEN_FLESH, "§4Entity Griefing", settings.isEntityGriefingAllowed()));

        inv.setItem(26, itemFromConfig("gui.tooltips.back", Material.ARROW, "§7Back"));

        if (fromAdmin) cache.setAdminMenu(player, inv); else cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openRolesGUI(Player player, Plot plot, boolean fromAdmin) {
        String title = plugin.getConfig().getString("gui.roles.title", "§6Claim Roles");
        Inventory inv = Bukkit.createInventory(player, 27, title);

        fill(inv);

        inv.setItem(11, itemFromConfig("gui.tooltips.builder", Material.STONE_PICKAXE, "§aBuilder"));
        inv.setItem(12, itemFromConfig("gui.tooltips.moderator", Material.IRON_SWORD, "§cModerator"));
        inv.setItem(13, itemFromConfig("gui.tooltips.manager", Material.DIAMOND, "§eManager"));
        inv.setItem(15, itemFromConfig("gui.tooltips.trust", Material.BOOK, "§aTrusted List"));
        inv.setItem(22, itemFromConfig("gui.tooltips.back", Material.ARROW, "§7Back"));

        if (fromAdmin) cache.setAdminMenu(player, inv); else cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openTrustMenu(Player player, boolean fromAdmin) {
        String title = plugin.getConfig().getString("gui.trust.title", "§aTrust Player");
        Inventory inv = Bukkit.createInventory(player, 9, title);

        fill(inv);

        inv.setItem(4, itemFromConfig("gui.tooltips.trust", Material.BOOK, "§aTrust Player"));
        inv.setItem(8, itemFromConfig("gui.tooltips.back", Material.ARROW, "§7Back"));

        if (fromAdmin) cache.setAdminMenu(player, inv); else cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openUntrustMenu(Player player, boolean fromAdmin) {
        String title = plugin.getConfig().getString("gui.untrust.title", "§cUntrust Player");
        Inventory inv = Bukkit.createInventory(player, 9, title);

        fill(inv);

        inv.setItem(4, itemFromConfig("gui.tooltips.untrust", Material.BARRIER, "§cUntrust Player"));
        inv.setItem(8, itemFromConfig("gui.tooltips.back", Material.ARROW, "§7Back"));

        if (fromAdmin) cache.setAdminMenu(player, inv); else cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    /* ---------------------------------------------------------
     * HELPERS
     * --------------------------------------------------------- */

    private ItemStack itemFromConfig(String path, Material fallbackMat, String fallbackName) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        String name = fallbackName;
        String[] lore = new String[]{};

        if (section != null) {
            name = section.getString("name", fallbackName);
            lore = section.getStringList("lore").toArray(new String[0]);
        }

        return createItem(fallbackMat, name, lore);
    }

    private ItemStack statefulItem(String path, Material mat, String fallbackName, boolean enabled) {
        ItemStack item = itemFromConfig(path, mat, fallbackName);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Now: " + (enabled ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fill(Inventory inv) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }
    }

    public void clearCache() {
        cache.clearCache();
    }

    public GUICache getCache() {
        return cache;
    }

    public ProShield getPlugin() {
        return plugin;
    }
}
