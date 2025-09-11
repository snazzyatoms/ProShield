// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlotSettings;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;

import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIManager implements Listener {

    private static final String TITLE_MAIN  = ChatColor.DARK_AQUA + "ProShield";
    private static final String TITLE_FLAGS = ChatColor.BLUE + "Flags";

    private final ProShield plugin;
    private final GUICache cache;
    private final PlotManager plots;
    private final MessagesUtil msg;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
        this.plots = plugin.getPlotManager();
        this.msg   = plugin.getMessagesUtil();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* ========================================================
     * COMPASS
     * ======================================================== */
    public void giveCompass(Player player, boolean force) {
        if (player == null) return;
        ItemStack compass = buildCompass();
        boolean has = player.getInventory().containsAtLeast(compass, 1);
        if (force || !has) {
            player.getInventory().addItem(compass);
        }
    }

    private ItemStack buildCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            m.setLore(Arrays.asList(
                    ChatColor.GRAY + "Manage claims & roles.",
                    ChatColor.YELLOW + "Right-click to open GUI."
            ));
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
        }
        return it;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCompassUse(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (!ChatColor.stripColor(meta.getDisplayName()).equals("ProShield Compass")) return;

        e.setCancelled(true);
        openMain(e.getPlayer());
    }

    /* ========================================================
     * PUBLIC API
     * ======================================================== */
    public void openMain(Player p)      { openMainInternal(p); }
    public void openFlagsMenu(Player p) { openFlagsInternal(p); }

    /* ========================================================
     * INVENTORY CLICK ROUTER
     * ======================================================== */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();

        if (TITLE_MAIN.equals(title)) {
            e.setCancelled(true);
            handleMainClick(p, e.getCurrentItem());
        } else if (TITLE_FLAGS.equals(title)) {
            e.setCancelled(true);
            handleFlagsClick(p, e.getCurrentItem(), e.getSlot());
        }
    }

    /* ========================================================
     * INTERNAL OPEN METHODS
     * ======================================================== */
    private void openMainInternal(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_MAIN);
        set(inv, 11, make(Material.PAPER, "Flags", List.of("Manage claim flags")));
        set(inv, 13, make(Material.BOOK, "Roles", List.of("Manage roles & trust")));
        set(inv, 15, make(Material.CHEST, "Transfer", List.of("Transfer ownership")));
        p.openInventory(inv);
    }

    private void openFlagsInternal(Player p) {
        Plot plot = plots.getPlot(p.getLocation());
        PlotSettings s = (plot != null) ? plot.getSettings() : new PlotSettings();

        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_FLAGS);

        set(inv, 10, toggleItem(Material.TNT, "Explosions", s.isExplosionsAllowed()));
        set(inv, 11, toggleItem(Material.BUCKET, "Buckets", s.isBucketAllowed()));
        set(inv, 12, toggleItem(Material.ITEM_FRAME, "Item Frames", s.isItemFramesAllowed()));
        set(inv, 13, toggleItem(Material.ARMOR_STAND, "Armor Stands", s.isArmorStandsAllowed()));
        set(inv, 14, toggleItem(Material.WHEAT, "Animals", s.isAnimalAccessAllowed()));
        set(inv, 15, toggleItem(Material.BONE, "Pets", s.isPetAccessAllowed()));
        set(inv, 16, toggleItem(Material.CHEST, "Containers", s.isContainersAllowed()));
        set(inv, 19, toggleItem(Material.MINECART, "Vehicles", s.isVehiclesAllowed()));
        set(inv, 20, toggleItem(Material.FLINT_AND_STEEL, "Fire", s.isFireAllowed()));
        set(inv, 21, toggleItem(Material.REDSTONE, "Redstone", s.isRedstoneAllowed()));
        set(inv, 22, make(Material.OAK_DOOR, "Back", List.of("Return to main menu")));
        set(inv, 23, toggleItem(Material.CREEPER_HEAD, "Entity Griefing", s.isEntityGriefingAllowed()));
        set(inv, 24, toggleItem(Material.DIAMOND_SWORD, "PvP", s.isPvpEnabled()));
        set(inv, 25, toggleItem(Material.BARRIER, "Mob Repel", s.isMobRepelEnabled()));
        set(inv, 26, toggleItem(Material.ROTTEN_FLESH, "Mob Despawn", s.isMobDespawnInsideEnabled()));

        p.openInventory(inv);
    }

    /* ========================================================
     * CLICK HANDLERS
     * ======================================================== */
    private void handleMainClick(Player p, ItemStack it) {
        if (!valid(it)) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName()).toLowerCase();
        if ("flags".equals(name)) {
            openFlagsInternal(p);
        }
    }

    private void handleFlagsClick(Player p, ItemStack it, int slot) {
        if (!valid(it)) return;
        Plot plot = plots.getPlot(p.getLocation());
        if (plot == null) {
            p.sendMessage(ChatColor.RED + "You must be inside a claim to edit flags.");
            return;
        }

        // Permission check: only owner or admin can edit
        UUID uid = p.getUniqueId();
        if (!p.hasPermission("proshield.admin") && !uid.equals(plot.getOwner())) {
            p.sendMessage(ChatColor.RED + "Only the claim owner or admins can change flags.");
            return;
        }

        PlotSettings s = plot.getSettings();
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName()).toLowerCase();
        boolean newState = false;

        switch (name) {
            case "explosions" -> { s.setExplosionsAllowed(!s.isExplosionsAllowed()); newState = s.isExplosionsAllowed(); }
            case "buckets" -> { s.setBucketAllowed(!s.isBucketAllowed()); newState = s.isBucketAllowed(); }
            case "item frames" -> { s.setItemFramesAllowed(!s.isItemFramesAllowed()); newState = s.isItemFramesAllowed(); }
            case "armor stands" -> { s.setArmorStandsAllowed(!s.isArmorStandsAllowed()); newState = s.isArmorStandsAllowed(); }
            case "animals" -> { s.setAnimalAccessAllowed(!s.isAnimalAccessAllowed()); newState = s.isAnimalAccessAllowed(); }
            case "pets" -> { s.setPetAccessAllowed(!s.isPetAccessAllowed()); newState = s.isPetAccessAllowed(); }
            case "containers" -> { s.setContainersAllowed(!s.isContainersAllowed()); newState = s.isContainersAllowed(); }
            case "vehicles" -> { s.setVehiclesAllowed(!s.isVehiclesAllowed()); newState = s.isVehiclesAllowed(); }
            case "fire" -> { s.setFireAllowed(!s.isFireAllowed()); newState = s.isFireAllowed(); }
            case "redstone" -> { s.setRedstoneAllowed(!s.isRedstoneAllowed()); newState = s.isRedstoneAllowed(); }
            case "entity griefing" -> { s.setEntityGriefingAllowed(!s.isEntityGriefingAllowed()); newState = s.isEntityGriefingAllowed(); }
            case "pvp" -> { s.setPvpEnabled(!s.isPvpEnabled()); newState = s.isPvpEnabled(); }
            case "mob repel" -> { s.setMobRepelEnabled(!s.isMobRepelEnabled()); newState = s.isMobRepelEnabled(); }
            case "mob despawn" -> { s.setMobDespawnInsideEnabled(!s.isMobDespawnInsideEnabled()); newState = s.isMobDespawnInsideEnabled(); }
            case "back" -> {
                openMainInternal(p);
                return;
            }
            default -> { return; }
        }

        // Update GUI instantly
        p.getOpenInventory().getTopInventory().setItem(
                slot,
                toggleItem(it.getType(), it.getItemMeta().getDisplayName(), newState)
        );

        // Debug log
        plugin.getLogger().info(p.getName() + " toggled flag '" + name + "' in claim " + plot.getDisplayNameSafe() + " -> " + (newState ? "ENABLED" : "DISABLED"));

        plots.saveAsync(plot);
    }

    /* ========================================================
     * HELPERS
     * ======================================================== */
    private boolean valid(ItemStack it) {
        return it != null && it.getType() != Material.AIR && it.hasItemMeta() && it.getItemMeta().hasDisplayName();
    }

    private void set(Inventory inv, int slot, ItemStack it) {
        if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, it);
    }

    private ItemStack make(Material type, String name, List<String> lore) {
        ItemStack it = new ItemStack(type);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.WHITE + name);
            if (lore != null) {
                List<String> colored = new ArrayList<>();
                for (String s : lore) colored.add(ChatColor.GRAY + s);
                m.setLore(colored);
            }
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
        }
        return it;
    }

    private ItemStack toggleItem(Material type, String name, boolean enabled) {
        ItemStack it = new ItemStack(type);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.WHITE + name);
            m.setLore(List.of(
                    ChatColor.GRAY + "State: " + (enabled ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"),
                    ChatColor.YELLOW + "Click to toggle."
            ));
            if (enabled) m.addEnchant(Enchantment.UNBREAKING, 1, true);
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
        }
        return it;
    }

    private InventoryHolder dummyHolder() {
        return () -> null;
    }
}
