package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Supplier;

public class GUIManager {

    /* Titles (used by GUIListener) */
    public static final String TITLE_MAIN  = ChatColor.LIGHT_PURPLE + "ProShield";
    public static final String TITLE_ADMIN = ChatColor.LIGHT_PURPLE + "ProShield — Admin";
    public static final String TITLE_HELP  = ChatColor.AQUA + "ProShield — Help";

    private final ProShield plugin;
    private final PlotManager plots;

    /* slot map (configurable, with hardcoded fallbacks) */
    private int slotMainCreate = 11;
    private int slotMainInfo   = 13;
    private int slotMainRemove = 15;
    private int slotMainAdmin  = 33;
    private int slotMainHelp   = 49;
    private int slotMainBack   = 48;

    private int slotAdminFire      = 10;
    private int slotAdminExplode   = 11;
    private int slotAdminInteract  = 12;
    private int slotAdminMobGrief  = 13;
    private int slotAdminPvp       = 14;
    private int slotAdminPurge     = 16;

    private int slotAdminKeepItems = 20;
    private int slotAdminHelp      = 22;
    private int slotAdminDebug     = 24;
    private int slotAdminReload    = 25;
    private int slotAdminBack      = 31;

    private NamespacedKey compassKey;

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots  = plots;
        this.compassKey = new NamespacedKey(JavaPlugin.getProvidingPlugin(plugin.getClass()), "proshield_admin_compass");
        loadSlotsFromConfig();
    }

    /* =========================
     * Public API used elsewhere
     * ========================= */

    public void registerCompassRecipe() {
        // simple compass craft (compass + lime dye) -> "ProShield Compass"
        try {
            ShapedRecipe recipe = new ShapedRecipe(compassKey, createAdminCompass());
            recipe.shape(" L ", " C ", "   ");
            recipe.setIngredient('L', Material.LIME_DYE);
            recipe.setIngredient('C', Material.COMPASS);
            Bukkit.removeRecipe(compassKey); // avoid duplicates on /reload
            Bukkit.addRecipe(recipe);
        } catch (Throwable t) {
            plugin.getLogger().warning("Unable to register compass recipe: " + t.getMessage());
        }
    }

    public void onConfigReload() {
        loadSlotsFromConfig();
    }

    public ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Right-click to open " + ChatColor.WHITE + "ProShield",
                ChatColor.DARK_GRAY + "(admin menu inside)"
            ));
            it.setItemMeta(meta);
        }
        return it;
    }

    public void giveCompass(Player p, boolean dropIfFull) {
        ItemStack compass = createAdminCompass();
        HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(compass);
        if (!leftover.isEmpty() && dropIfFull) {
            p.getWorld().dropItemNaturally(p.getLocation(), compass);
        }
    }

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_MAIN);

        inv.setItem(slotMainCreate, icon(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Chunk",
                "Claim the chunk you are standing in."));
        inv.setItem(slotMainInfo, icon(Material.OAK_SIGN, ChatColor.YELLOW + "Claim Info",
                "See owner, trusted players, roles."));
        inv.setItem(slotMainRemove, icon(Material.BARRIER, ChatColor.RED + "Unclaim",
                "Remove your claim from this chunk."));

        if (p.hasPermission("proshield.admin.gui")) {
            inv.setItem(slotMainAdmin, icon(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Admin Menu",
                    "Open the ProShield admin tools."));
        }

        inv.setItem(slotMainHelp, icon(Material.BOOK, ChatColor.AQUA + "Help",
                "Shows commands available to your permissions."));

        inv.setItem(slotMainBack, backIcon());

        p.openInventory(inv);
    }

    public void openHelp(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_HELP);

        List<String> lines = new ArrayList<>();
        boolean isAdmin = p.hasPermission("proshield.admin");

        // base commands based on perms
        lines.add(line("/proshield claim", "Claim your current chunk"));
        lines.add(line("/proshield info", "Show claim details"));
        lines.add(line("/proshield trust <player> [role]", "Trust a player with optional role"));
        lines.add(line("/proshield untrust <player>", "Remove trust"));
        lines.add(line("/proshield trusted", "List trusted players"));
        lines.add(line("/proshield compass", "Get the ProShield compass"));

        if (isAdmin) {
            lines.add("");
            lines.add(ChatColor.LIGHT_PURPLE + "Admin");
            lines.add(line("/proshield bypass <on|off|toggle>", "Temporarily ignore protection"));
            lines.add(line("/proshield purgeexpired <days> [dryrun]", "Cleanup inactive claims"));
            lines.add(line("/proshield reload", "Reload configs"));
            lines.add(line("/proshield debug <on|off|toggle>", "Toggle debug logs"));
        }

        // Render as a single book item with lore
        ItemStack book = icon(Material.BOOK,
                ChatColor.AQUA + "Commands you can use",
                lines.toArray(new String[0]));
        inv.setItem(22, book);
        inv.setItem(31, backIcon());
        p.openInventory(inv);
    }

    public void openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, TITLE_ADMIN);

        // read current toggles from config
        boolean fireEnabled       = plugin.getConfig().getBoolean("protection.fire.enabled", true);
        boolean explodeEnabled    = plugin.getConfig().getBoolean("protection.explosions.enabled", true);
        boolean interactEnabled   = plugin.getConfig().getBoolean("protection.interactions.enabled", true);
        boolean mobGriefEnabled   = plugin.getConfig().getBoolean("protection.entity-grief.enabled", true);
        boolean pvpInClaims       = plugin.getConfig().getBoolean("protection.pvp-in-claims", false);
        boolean keepItemsEnabled  = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        boolean debugEnabled      = plugin.isDebug();

        ensure(inv, slotAdminFire,
                toggleIcon(fireEnabled, () -> icon(Material.FLINT_AND_STEEL,
                        labelToggle("Fire", fireEnabled),
                        "Toggle fire protection (spread/ignite/burn)")),
                p, "proshield.admin");

        ensure(inv, slotAdminExplode,
                toggleIcon(explodeEnabled, () -> icon(Material.TNT,
                        labelToggle("Explosions", explodeEnabled),
                        "Toggle explosion protection in claims")),
                p, "proshield.admin");

        ensure(inv, slotAdminInteract,
                toggleIcon(interactEnabled, () -> icon(Material.LEVER,
                        labelToggle("Interactions", interactEnabled),
                        "Doors, buttons, levers…")),
                p, "proshield.admin");

        ensure(inv, slotAdminMobGrief,
                toggleIcon(mobGriefEnabled, () -> icon(Material.CREEPER_HEAD,
                        labelToggle("Mob Grief", mobGriefEnabled),
                        "Endermen pick up, Ravagers, etc.")),
                p, "proshield.admin");

        ensure(inv, slotAdminPvp,
                icon(pvpInClaims ? Material.IRON_SWORD : Material.SHIELD,
                        ChatColor.GOLD + "PvP in Claims: " + (pvpInClaims ? green("ON") : red("OFF")),
                        "If ON, players can fight inside claims.",
                        "If OFF, PvP is blocked inside claims."),
                p, "proshield.admin");

        ensure(inv, slotAdminPurge,
                icon(Material.LAVA_BUCKET, ChatColor.RED + "Purge Expired Claims",
                        "Preview and remove inactive claims via",
                        ChatColor.YELLOW + "/proshield purgeexpired <days> [dryrun]"),
                p, "proshield.admin.expired.purge");

        ensure(inv, slotAdminKeepItems,
                toggleIcon(keepItemsEnabled, () -> icon(Material.LIME_DYE,
                        labelToggle("Keep Items in Claims", keepItemsEnabled),
                        "Prevent dropped items in claims from despawning.",
                        "Configurable in claims.keep-items")),
                p, "proshield.admin.keepitems");

        ensure(inv, slotAdminHelp,
                icon(Material.WRITABLE_BOOK, ChatColor.AQUA + "Admin Help",
                        "Short reference of admin commands.",
                        "More tools coming in 2.0!"),
                p, "proshield.admin");

        ensure(inv, slotAdminDebug,
                toggleIcon(debugEnabled, () -> icon(Material.REDSTONE_TORCH,
                        labelToggle("Debug Logs", debugEnabled),
                        "Toggle verbose logging for troubleshooting.")),
                p, "proshield.admin.debug");

        ensure(inv, slotAdminReload,
                icon(Material.REPEATER, ChatColor.GREEN + "Reload Config",
                        "Apply changes from config.yml"),
                p, "proshield.admin.reload");

        inv.setItem(slotAdminBack, backIcon());

        p.openInventory(inv);
    }

    /* =========================
     * Helpers
     * ========================= */

    private void loadSlotsFromConfig() {
        try {
            slotMainCreate = plugin.getConfig().getInt("gui.slots.main.create", slotMainCreate);
            slotMainInfo   = plugin.getConfig().getInt("gui.slots.main.info",   slotMainInfo);
            slotMainRemove = plugin.getConfig().getInt("gui.slots.main.remove", slotMainRemove);
            slotMainAdmin  = plugin.getConfig().getInt("gui.slots.main.admin",  slotMainAdmin);
            slotMainHelp   = plugin.getConfig().getInt("gui.slots.main.help",   slotMainHelp);
            slotMainBack   = plugin.getConfig().getInt("gui.slots.main.back",   slotMainBack);

            slotAdminFire      = plugin.getConfig().getInt("gui.slots.admin.toggle-fire",      slotAdminFire);
            slotAdminExplode   = plugin.getConfig().getInt("gui.slots.admin.toggle-explosions",slotAdminExplode);
            slotAdminInteract  = plugin.getConfig().getInt("gui.slots.admin.toggle-interactions",slotAdminInteract);
            slotAdminMobGrief  = plugin.getConfig().getInt("gui.slots.admin.toggle-mobgrief",  slotAdminMobGrief);
            slotAdminPvp       = plugin.getConfig().getInt("gui.slots.admin.toggle-pvp",       slotAdminPvp);
            slotAdminPurge     = plugin.getConfig().getInt("gui.slots.admin.purgeexpired",     slotAdminPurge);
            slotAdminKeepItems = plugin.getConfig().getInt("gui.slots.admin.toggle-keepitems", slotAdminKeepItems);
            slotAdminHelp      = plugin.getConfig().getInt("gui.slots.admin.help",             slotAdminHelp);
            slotAdminDebug     = plugin.getConfig().getInt("gui.slots.admin.toggle-debug",     slotAdminDebug);
            slotAdminReload    = plugin.getConfig().getInt("gui.slots.admin.reload",           slotAdminReload);
            slotAdminBack      = plugin.getConfig().getInt("gui.slots.admin.back",             slotAdminBack);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid gui.slot entries. Using defaults.");
        }
    }

    private ItemStack icon(Material mat, String name, String... loreLines) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (loreLines != null && loreLines.length > 0) {
                List<String> lore = new ArrayList<>();
                for (String s : loreLines) lore.add(ChatColor.GRAY + s);
                meta.setLore(lore);
            }
            it.setItemMeta(meta);
        }
        return it;
    }

    private ItemStack toggleIcon(boolean on, Supplier<ItemStack> supplier) {
        return supplier.get(); // visual handled in label/color
    }

    private ItemStack backIcon() {
        return icon(Material.ARROW, ChatColor.WHITE + "Back",
                "Return to previous menu.");
    }

    private String labelToggle(String label, boolean on) {
        return ChatColor.GOLD + label + ": " + (on ? green("ON") : red("OFF"));
    }

    private String green(String s) { return ChatColor.GREEN + s; }
    private String red(String s)   { return ChatColor.RED + s; }

    private String line(String cmd, String desc) {
        return ChatColor.YELLOW + cmd + ChatColor.GRAY + " — " + desc;
    }

    private void ensure(Inventory inv, int slot, ItemStack item, Player viewer, String perm) {
        if (viewer.hasPermission(perm)) {
            inv.setItem(slot, item);
        }
    }
}
