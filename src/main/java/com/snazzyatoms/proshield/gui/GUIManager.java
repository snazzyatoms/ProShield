package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GUIManager {

    // Titles (match via InventoryView#getTitle)
    public static final String TITLE_MAIN  = ChatColor.DARK_AQUA + "ProShield";
    public static final String TITLE_ADMIN = ChatColor.DARK_RED + "ProShield Admin";
    public static final String TITLE_HELP  = ChatColor.DARK_GREEN + "ProShield Help";

    private final ProShield plugin;
    private final PlotManager plots;

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    /* -------------------------------------------------------
     * Compass helpers
     * ------------------------------------------------------ */

    public ItemStack createPlayerCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Right-click to open",
                    ChatColor.DARK_GRAY + "Claim / Info / Unclaim"
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);
        }
        return it;
    }

    public ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "ProShield Admin Compass");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Right-click to open",
                    ChatColor.DARK_RED + "Admin tools & toggles"
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);
        }
        return it;
    }

    /** Back-compat: older code calls this signature. */
    public void giveCompass(Player p, boolean dropIfFull) {
        boolean isAdmin = p.hasPermission("proshield.admin.gui") || p.hasPermission("proshield.admin") || p.isOp();
        ItemStack compass = isAdmin ? createAdminCompass() : createPlayerCompass();
        addToInventoryOrDrop(p, compass, dropIfFull);
    }

    public boolean isProShieldCompass(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getType() != Material.COMPASS && stack.getType() != Material.RECOVERY_COMPASS) return false;
        ItemMeta m = stack.getItemMeta();
        if (m == null || !m.hasDisplayName()) return false;
        String name = ChatColor.stripColor(m.getDisplayName()).toLowerCase();
        return name.contains("proshield");
    }

    private void addToInventoryOrDrop(Player p, ItemStack item, boolean dropIfFull) {
        if (p.getInventory().firstEmpty() == -1) {
            if (dropIfFull) {
                p.getWorld().dropItemNaturally(p.getLocation(), item);
                p.sendMessage(prefix() + ChatColor.YELLOW + "Inventory full. Dropped your ProShield compass at your feet.");
            } else {
                p.sendMessage(prefix() + ChatColor.RED + "Inventory full — free a slot or use /proshield compass.");
            }
        } else {
            p.getInventory().addItem(item);
        }
    }

    /* -------------------------------------------------------
     * Openers
     * ------------------------------------------------------ */

    /** Back-compat overload (ignored flag). */
    public void openMain(Player player, boolean ignored) { openMain(player); }

    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_MAIN);
        decorate(inv);

        int slotCreate = plugin.getConfig().getInt("gui.slots.main.create", 11);
        int slotInfo   = plugin.getConfig().getInt("gui.slots.main.info", 13);
        int slotRemove = plugin.getConfig().getInt("gui.slots.main.remove", 15);
        int slotHelp   = plugin.getConfig().getInt("gui.slots.main.help", 31);
        int slotAdmin  = plugin.getConfig().getInt("gui.slots.main.admin", 33);
        int slotBack   = plugin.getConfig().getInt("gui.slots.main.back", 48);

        inv.setItem(slotCreate, icon(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Chunk",
                Arrays.asList(ChatColor.GRAY + "Claim the current chunk as yours.")));
        inv.setItem(slotInfo, icon(Material.PAPER, ChatColor.AQUA + "Claim Info",
                Arrays.asList(ChatColor.GRAY + "See owner, trusted players, etc.")));
        inv.setItem(slotRemove, icon(Material.BARRIER, ChatColor.RED + "Unclaim",
                Arrays.asList(ChatColor.GRAY + "Unclaim the current chunk.")));
        inv.setItem(slotHelp, icon(Material.BOOK, ChatColor.GOLD + "Help",
                Arrays.asList(ChatColor.GRAY + "Commands & tips.")));

        boolean canAdmin = player.hasPermission("proshield.admin.gui") || player.hasPermission("proshield.admin") || player.isOp();
        if (canAdmin) {
            inv.setItem(slotAdmin, icon(Material.NETHER_STAR, ChatColor.RED + "Admin Menu",
                    Arrays.asList(ChatColor.GRAY + "Open Admin tools & toggles.")));
        } else {
            inv.setItem(slotAdmin, icon(Material.GRAY_DYE, ChatColor.DARK_GRAY + "Admin Menu",
                    Arrays.asList(ChatColor.RED + "No permission.")));
        }

        // Back on MAIN = close
        inv.setItem(slotBack, icon(Material.OAK_DOOR, ChatColor.YELLOW + "Close",
                Arrays.asList(ChatColor.GRAY + "Close this menu.")));

        player.openInventory(inv);
        clickSound(player);
    }

    public void openAdmin(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_ADMIN);
        decorate(inv);

        int sFire        = plugin.getConfig().getInt("gui.slots.admin.fire", 10);
        int sExpl        = plugin.getConfig().getInt("gui.slots.admin.explosions", 11);
        int sGrief       = plugin.getConfig().getInt("gui.slots.admin.entity-grief", 12);
        int sInter       = plugin.getConfig().getInt("gui.slots.admin.interactions", 13);
        int sPvp         = plugin.getConfig().getInt("gui.slots.admin.pvp", 14);
        int sKeepItems   = plugin.getConfig().getInt("gui.slots.admin.keep-items", 20);
        int sPurge       = plugin.getConfig().getInt("gui.slots.admin.purge-expired", 21);
        int sDebug       = plugin.getConfig().getInt("gui.slots.admin.debug", 23);
        int sCompassDrop = plugin.getConfig().getInt("gui.slots.admin.compass-drop-if-full", 24);
        int sReload      = plugin.getConfig().getInt("gui.slots.admin.reload", 25);
        int sTpTools     = plugin.getConfig().getInt("gui.slots.admin.tp-tools", 30);
        int sBack        = plugin.getConfig().getInt("gui.slots.admin.back", 31);
        int sHelp        = plugin.getConfig().getInt("gui.slots.admin.help", 22);

        boolean fire      = plugin.getConfig().getBoolean("protection.fire.enabled", true);
        boolean expl      = plugin.getConfig().getBoolean("protection.explosions.enabled", true);
        boolean egrief    = plugin.getConfig().getBoolean("protection.entity-grief.enabled", true);
        boolean inter     = plugin.getConfig().getBoolean("protection.interactions.enabled", true);
        boolean pvpIn     = plugin.getConfig().getBoolean("protection.pvp-in-claims", false);
        boolean keepItems = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        boolean debug     = plugin.isDebug();
        boolean dropIfFull= plugin.getConfig().getBoolean("compass.drop-if-full", true);

        inv.setItem(sFire, toggleIcon(Material.FLINT_AND_STEEL, "Fire", fire));
        inv.setItem(sExpl, toggleIcon(Material.TNT, "Explosions", expl));
        inv.setItem(sGrief, toggleIcon(Material.ENDERMAN_SPAWN_EGG, "Entity Grief", egrief));
        inv.setItem(sInter, toggleIcon(Material.LEVER, "Interactions", inter));
        inv.setItem(sPvp, toggleIcon(Material.IRON_SWORD, "PvP in Claims (ON = PvP allowed)", pvpIn));
        inv.setItem(sKeepItems, toggleIcon(Material.CHEST, "Keep Dropped Items in Claims", keepItems));
        inv.setItem(sPurge, icon(Material.HOPPER, ChatColor.YELLOW + "Purge Expired Claims",
                Arrays.asList(ChatColor.GRAY + "Run /proshield purgeexpired <days> [dryrun]")));
        inv.setItem(sDebug, toggleIcon(Material.REDSTONE, "Debug Logging", debug));
        inv.setItem(sCompassDrop, toggleIcon(Material.DROPPER, "Compass: Drop If Full", dropIfFull));
        inv.setItem(sReload, icon(Material.SPYGLASS, ChatColor.GREEN + "Reload ProShield",
                Arrays.asList(ChatColor.GRAY + "Reload config & refresh caches.")));
        inv.setItem(sTpTools, icon(Material.ENDER_PEARL, ChatColor.AQUA + "Teleport Tools",
                Arrays.asList(ChatColor.GRAY + "Quick admin TP helpers (coming soon).")));
        inv.setItem(sHelp, icon(Material.BOOK, ChatColor.GOLD + "Admin Help",
                Arrays.asList(ChatColor.GRAY + "Short tips & docs link.",
                        ChatColor.DARK_GRAY + "More coming in 2.0.")));
        inv.setItem(sBack, icon(Material.ARROW, ChatColor.YELLOW + "Back to Main",
                Arrays.asList(ChatColor.GRAY + "Return to the main menu.")));

        player.openInventory(inv);
        clickSound(player);
    }

    public void openHelp(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_HELP);
        decorate(inv);

        inv.setItem(22, icon(Material.WRITABLE_BOOK, ChatColor.GOLD + "Quick Help",
                Arrays.asList(
                        ChatColor.YELLOW + "/proshield claim " + ChatColor.GRAY + "– claim your chunk",
                        ChatColor.YELLOW + "/proshield trust <player> [role]" + ChatColor.GRAY + " – allow access",
                        ChatColor.YELLOW + "/proshield info " + ChatColor.GRAY + "– show claim info",
                        ChatColor.YELLOW + "/proshield unclaim " + ChatColor.GRAY + "– remove claim",
                        ChatColor.DARK_GRAY + "See /proshield for more."
                )));
        inv.setItem(48, icon(Material.ARROW, ChatColor.YELLOW + "Back",
                Arrays.asList(ChatColor.GRAY + "Return to the main menu.")));

        player.openInventory(inv);
        clickSound(player);
    }

    /* -------------------------------------------------------
     * Listener helpers
     * ------------------------------------------------------ */

    public boolean isOurInventory(InventoryView view) {
        if (view == null) return false;
        String title = view.getTitle();
        return Objects.equals(title, TITLE_MAIN) ||
               Objects.equals(title, TITLE_ADMIN) ||
               Objects.equals(title, TITLE_HELP);
    }

    public void handleInventoryClick(Player player, int slot, ItemStack clicked, InventoryView view) {
        if (view == null) return;
        String title = view.getTitle();

        if (Objects.equals(title, TITLE_MAIN)) {
            int sCreate = plugin.getConfig().getInt("gui.slots.main.create", 11);
            int sInfo   = plugin.getConfig().getInt("gui.slots.main.info", 13);
            int sRemove = plugin.getConfig().getInt("gui.slots.main.remove", 15);
            int sHelp   = plugin.getConfig().getInt("gui.slots.main.help", 31);
            int sAdmin  = plugin.getConfig().getInt("gui.slots.main.admin", 33);
            int sBack   = plugin.getConfig().getInt("gui.slots.main.back", 48);

            if (slot == sCreate) { player.closeInventory(); player.performCommand("proshield claim"); clickSound(player); return; }
            if (slot == sInfo)   { player.closeInventory(); player.performCommand("proshield info");  clickSound(player); return; }
            if (slot == sRemove) { player.closeInventory(); player.performCommand("proshield unclaim"); clickSound(player); return; }
            if (slot == sHelp)   { openHelp(player); return; }
            if (slot == sAdmin) {
                boolean canAdmin = player.hasPermission("proshield.admin.gui") || player.hasPermission("proshield.admin") || player.isOp();
                if (canAdmin) openAdmin(player); else player.sendMessage(prefix() + ChatColor.RED + "You lack permission for Admin Menu.");
                return;
            }
            if (slot == sBack) { player.closeInventory(); return; }
        }

        if (Objects.equals(title, TITLE_ADMIN)) {
            int sFire        = plugin.getConfig().getInt("gui.slots.admin.fire", 10);
            int sExpl        = plugin.getConfig().getInt("gui.slots.admin.explosions", 11);
            int sGrief       = plugin.getConfig().getInt("gui.slots.admin.entity-grief", 12);
            int sInter       = plugin.getConfig().getInt("gui.slots.admin.interactions", 13);
            int sPvp         = plugin.getConfig().getInt("gui.slots.admin.pvp", 14);
            int sKeepItems   = plugin.getConfig().getInt("gui.slots.admin.keep-items", 20);
            int sPurge       = plugin.getConfig().getInt("gui.slots.admin.purge-expired", 21);
            int sDebug       = plugin.getConfig().getInt("gui.slots.admin.debug", 23);
            int sCompassDrop = plugin.getConfig().getInt("gui.slots.admin.compass-drop-if-full", 24);
            int sReload      = plugin.getConfig().getInt("gui.slots.admin.reload", 25);
            int sTpTools     = plugin.getConfig().getInt("gui.slots.admin.tp-tools", 30);
            int sBack        = plugin.getConfig().getInt("gui.slots.admin.back", 31);
            int sHelp        = plugin.getConfig().getInt("gui.slots.admin.help", 22);

            if (slot == sFire)        { flip("protection.fire.enabled");          reopenAdmin(player); return; }
            if (slot == sExpl)        { flip("protection.explosions.enabled");    reopenAdmin(player); return; }
            if (slot == sGrief)       { flip("protection.entity-grief.enabled");  reopenAdmin(player); return; }
            if (slot == sInter)       { flip("protection.interactions.enabled");  reopenAdmin(player); return; }
            if (slot == sPvp)         { flip("protection.pvp-in-claims");         reopenAdmin(player); return; }
            if (slot == sKeepItems)   { flip("claims.keep-items.enabled");        reopenAdmin(player); return; }
            if (slot == sDebug)       { plugin.setDebug(!plugin.isDebug()); msg(player, "Debug: " + plugin.isDebug()); reopenAdmin(player); return; }
            if (slot == sCompassDrop) { flip("compass.drop-if-full");             reopenAdmin(player); return; }

            if (slot == sPurge) { player.closeInventory(); player.performCommand("proshield purgeexpired 30 dryrun"); return; }

            if (slot == sReload) {
                plugin.reloadAllConfigs();
                Bukkit.getScheduler().runTaskLater(plugin, () -> openAdmin(player), 2L);
                return;
            }

            if (slot == sTpTools) { player.sendMessage(prefix() + ChatColor.YELLOW + "Teleport tools will arrive in 2.0."); clickSound(player); return; }
            if (slot == sHelp)    { openHelp(player); return; }
            if (slot == sBack)    { openMain(player); return; }
        }

        if (Objects.equals(title, TITLE_HELP)) {
            if (slot == 48) { openMain(player); }
        }
    }

    private void flip(String path) {
        boolean cur = plugin.getConfig().getBoolean(path, false);
        plugin.getConfig().set(path, !cur);
        plugin.saveConfig();
    }

    private void reopenAdmin(Player p) {
        clickSound(p);
        Bukkit.getScheduler().runTask(plugin, () -> openAdmin(p));
    }

    /* -------------------------------------------------------
     * UI helpers
     * ------------------------------------------------------ */

    private void decorate(Inventory inv) {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta m = pane.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.DARK_GRAY.toString());
            pane.setItemMeta(m);
        }
        fill(inv, pane);
    }

    private void fill(Inventory inv, ItemStack filler) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack cur = inv.getItem(i);
            if (cur == null || cur.getType() == Material.AIR) {
                inv.setItem(i, filler);
            }
        }
    }

    private ItemStack icon(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);
        }
        return it;
    }

    private ItemStack toggleIcon(Material mat, String label, boolean on) {
        String name = (on ? ChatColor.GREEN : ChatColor.RED) + label + ChatColor.GRAY + " [" + (on ? "ON" : "OFF") + "]";
        return icon(mat, name, Arrays.asList(ChatColor.DARK_GRAY + "Click to toggle"));
    }

    private void clickSound(Player p) {
        try { p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.4f); } catch (Throwable ignored) {}
    }

    private void msg(Player p, String s) {
        p.sendMessage(prefix() + ChatColor.GRAY + s);
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }

    /* -------------------------------------------------------
     * Config reload hook (called by ProShield#reloadAllConfigs)
     * ------------------------------------------------------ */
    public void onConfigReload() {
        // Currently we read config values each time we open a GUI,
        // so no cache to refresh. This hook exists to satisfy calls
        // from ProShield and for future caching if needed.
    }

    /* -------------------------------------------------------
     * Exposed getters
     * ------------------------------------------------------ */
    public ProShield getPlugin() { return plugin; }
    public PlotManager getPlots() { return plots; }
}
