package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryView;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    // Titles
    public static final String TITLE_MAIN  = ChatColor.DARK_AQUA + "ProShield";
    public static final String TITLE_ADMIN = ChatColor.DARK_RED + "ProShield Admin";
    public static final String TITLE_HELP  = ChatColor.DARK_GREEN + "ProShield Help";

    // PDC keys to mark our compass/items
    private final NamespacedKey KEY_COMPASS_KIND;
    private final NamespacedKey KEY_MENU_KIND;

    private final ProShield plugin;
    private final PlotManager plots;

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
        this.KEY_COMPASS_KIND = new NamespacedKey(plugin, "compass_kind"); // "admin" | "player"
        this.KEY_MENU_KIND = new NamespacedKey(plugin, "menu_kind");       // "main" | "admin" | "help"
    }

    /* -----------------------------------------------------------
     * Compass creation + detection
     * ----------------------------------------------------------- */

    public ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "ProShield Admin Compass");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click to open " + ChatColor.RED + "Admin" + ChatColor.GRAY + " menu");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(KEY_COMPASS_KIND, PersistentDataType.STRING, "admin");
        it.setItemMeta(meta);
        return it;
    }

    public ItemStack createPlayerCompass() {
        ItemStack it = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click to open ProShield menu");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(KEY_COMPASS_KIND, PersistentDataType.STRING, "player");
        it.setItemMeta(meta);
        return it;
    }

    public boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        if (!item.hasItemMeta()) return false;
        String kind = item.getItemMeta().getPersistentDataContainer().get(KEY_COMPASS_KIND, PersistentDataType.STRING);
        return "admin".equalsIgnoreCase(kind) || "player".equalsIgnoreCase(kind);
    }

    /** For commands that want to hand a compass explicitly */
    public void giveCompass(Player p, boolean admin) {
        ItemStack compass = admin ? createAdminCompass() : createPlayerCompass();
        var leftover = p.getInventory().addItem(compass);
        if (!leftover.isEmpty()) {
            boolean dropIfFull = plugin.getConfig().getBoolean("compass.drop-if-full", true);
            if (dropIfFull) {
                p.getWorld().dropItemNaturally(p.getLocation(), compass);
                p.sendMessage(color(prefix()) + "Inventory full—dropped your compass at your feet.");
            } else {
                p.sendMessage(color(prefix()) + "Inventory full. Free a slot or use /proshield compass again.");
            }
        } else if (plugin.isDebug()) {
            plugin.getLogger().info("[GUI] Gave " + (admin ? "admin" : "player") + " compass to " + p.getName());
        }
    }

    /* -----------------------------------------------------------
     * Menu helpers
     * ----------------------------------------------------------- */

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_MAIN);
        markMenu(inv, "main");
        // Layout (minimal but extensible)
        inv.setItem(11, icon(Material.OAK_SIGN, ChatColor.GREEN + "Claim Chunk", "Claim the current chunk"));
        inv.setItem(13, icon(Material.PAPER, ChatColor.AQUA + "Claim Info", "Owner, trusted, roles"));
        inv.setItem(15, icon(Material.BARRIER, ChatColor.RED + "Unclaim Chunk", "Remove your claim"));
        inv.setItem(21, icon(Material.PLAYER_HEAD, ChatColor.GOLD + "Trust Player", "Trust nearby/player by name"));
        inv.setItem(23, icon(Material.BOOK, ChatColor.YELLOW + "Roles", "Assign/change roles"));
        inv.setItem(31, icon(Material.MAP, ChatColor.GREEN + "Help", "Show commands you can use"));
        if (p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui")) {
            inv.setItem(33, icon(Material.REDSTONE_COMPARATOR, ChatColor.RED + "Admin Menu", "Open admin tools"));
        }
        inv.setItem(48, icon(Material.ARROW, ChatColor.GRAY + "Back", "Return to previous"));
        p.openInventory(inv);
    }

    public void openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_ADMIN);
        markMenu(inv, "admin");
        inv.setItem(10, icon(Material.FLINT_AND_STEEL, ChatColor.YELLOW + "Toggle Fire Prot", "Protect fire spread/ignite"));
        inv.setItem(11, icon(Material.TNT, ChatColor.YELLOW + "Toggle Explosions", "Protect TNT/creeper/ender etc."));
        inv.setItem(12, icon(Material.ZOMBIE_HEAD, ChatColor.YELLOW + "Toggle Mob Grief", "Disable mob grief in claims"));
        inv.setItem(13, icon(Material.SHIELD, ChatColor.YELLOW + "Toggle PvP", "Block/allow PvP in claims"));
        inv.setItem(14, icon(Material.CHEST, ChatColor.YELLOW + "Toggle Interactions", "Doors/buttons/containers"));
        inv.setItem(19, icon(Material.ITEM_FRAME, ChatColor.YELLOW + "Entity Interact Prot", "Item frames/armor stands"));
        inv.setItem(20, icon(Material.HOPPER, ChatColor.YELLOW + "Keep Drops", "Prevent despawn in claims"));
        inv.setItem(21, icon(Material.CLOCK, ChatColor.YELLOW + "Expiry Settings", "Preview/purge expired"));
        inv.setItem(22, icon(Material.COMPASS, ChatColor.YELLOW + "Compass Autogive", "Give on join + drop-if-full"));
        inv.setItem(24, icon(Material.WRITABLE_BOOK, ChatColor.GOLD + "Admin Help", "Tooltips & tips"));

        inv.setItem(48, icon(Material.ARROW, ChatColor.GRAY + "Back", "Return to main menu"));
        inv.setItem(49, icon(Material.BOOK, ChatColor.GREEN + "Help", "Show admin commands"));
        inv.setItem(53, icon(Material.NETHER_STAR, ChatColor.AQUA + "2.0 Preview", "More to come in 2.0"));
        p.openInventory(inv);
    }

    public void openHelp(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_HELP);
        markMenu(inv, "help");
        // This page should be populated per-permission in your existing code.
        inv.setItem(22, icon(Material.BOOK, ChatColor.GREEN + "Your Commands",
                "This list adapts to your permissions/role"));
        inv.setItem(48, icon(Material.ARROW, ChatColor.GRAY + "Back", "Return to previous"));
        p.openInventory(inv);
    }

    /* -----------------------------------------------------------
     * Inventory routing used by GUIListener
     * ----------------------------------------------------------- */

    public boolean isOurInventory(InventoryView view) {
        if (view == null) return false;
        String title = view.getTitle();
        return TITLE_MAIN.equals(title) || TITLE_ADMIN.equals(title) || TITLE_HELP.equals(title);
    }

    public void handleInventoryClick(Player p, int slot, ItemStack clicked, InventoryView view) {
        if (view == null) return;
        String title = view.getTitle();

        if (TITLE_MAIN.equals(title)) {
            switch (slot) {
                case 11 -> p.performCommand("proshield claim");
                case 13 -> p.performCommand("proshield info");
                case 15 -> p.performCommand("proshield unclaim");
                case 21 -> p.performCommand("proshield trust"); // your GUI may open a trust sub-page
                case 23 -> p.performCommand("proshield trusted"); // or open roles GUI
                case 31 -> openHelp(p);
                case 33 -> {
                    if (p.hasPermission("proshield.admin") || p.hasPermission("proshield.admin.gui")) openAdmin(p);
                    else p.sendMessage(color(prefix()) + "You don’t have permission for Admin menu.");
                }
                case 48 -> p.closeInventory();
                default -> {}
            }
        } else if (TITLE_ADMIN.equals(title)) {
            switch (slot) {
                case 10 -> p.performCommand("proshield toggle fire");
                case 11 -> p.performCommand("proshield toggle explosions");
                case 12 -> p.performCommand("proshield toggle mobgrief");
                case 13 -> p.performCommand("proshield toggle pvp");
                case 14 -> p.performCommand("proshield toggle interactions");
                case 19 -> p.performCommand("proshield toggle entities");
                case 20 -> p.performCommand("proshield toggle keepdrops");
                case 21 -> p.performCommand("proshield purgeexpired 30 dryrun");
                case 22 -> p.performCommand("proshield compass"); // could open a sub page
                case 48 -> openMain(p);
                case 49 -> openHelp(p);
                case 53 -> p.sendMessage(color(prefix()) + "2.0 features preview coming soon!");
                default -> {}
            }
        } else if (TITLE_HELP.equals(title)) {
            if (slot == 48) openMain(p);
        }
    }

    /* -----------------------------------------------------------
     * Util
     * ----------------------------------------------------------- */

    private void markMenu(Inventory inv, String kind) {
        // Optionally stash a flag on slot 0 item if you want; titles are enough here.
    }

    private ItemStack icon(Material mat, String name, String... loreLines) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        if (loreLines != null && loreLines.length > 0) {
            List<String> lore = new ArrayList<>();
            for (String l : loreLines) lore.add(ChatColor.GRAY + l);
            meta.setLore(lore);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
