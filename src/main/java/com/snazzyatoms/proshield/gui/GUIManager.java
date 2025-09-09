// path: src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GUIManager {

    public static final String TITLE = ChatColor.DARK_AQUA + "ProShield";
    public static final String ADMIN_TITLE = ChatColor.DARK_RED + "ProShield Admin";

    private final ProShield plugin;
    private final PlotManager plots;

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots  = plots;
    }

    /* ========= EXISTING: Main/admin compass factory ========= */
    public static ItemStack createAdminCompass() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.AQUA + "ProShield Compass");
            m.setLore(List.of(ChatColor.GRAY + "Right-click to open the ProShield menu"));
            it.setItemMeta(m);
        }
        return it;
    }

    /* ========= Player Main GUI (kept as you had it) ========= */
    public Inventory buildMain(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        inv.setItem(11, make(Material.OAK_FENCE, ChatColor.GREEN + "Claim Chunk",
                List.of(ChatColor.GRAY + "Claim the current chunk.")));
        inv.setItem(13, make(Material.PAPER, ChatColor.AQUA + "Claim Info",
                List.of(ChatColor.GRAY + "Owner & trusted players.")));
        inv.setItem(15, make(Material.BARRIER, ChatColor.RED + "Unclaim Chunk",
                List.of(ChatColor.GRAY + "Remove your claim here.")));

        // NEW: Help & Admin buttons (from 1.2.2)
        inv.setItem(31 - 27, make(Material.BOOK, ChatColor.GOLD + "Help",
                List.of(ChatColor.GRAY + "Shows commands you can use.")));
        if (p.hasPermission("proshield.admin")) {
            inv.setItem(33 - 27, make(Material.NETHER_STAR, ChatColor.RED + "Admin Menu",
                    List.of(ChatColor.GRAY + "Open the admin tools panel.")));
        }

        return inv;
    }

    public void openMain(Player p) {
        p.openInventory(buildMain(p));
    }

    /* ========= NEW: Admin GUI (54 slots) ========= */
    public Inventory buildAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, ADMIN_TITLE);

        // Read current effective toggles from config
        boolean keepItems  = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        boolean fire       = plugin.getConfig().getBoolean("protection.fire.enabled", true);
        boolean explosions = plugin.getConfig().getBoolean("protection.explosions.enabled", true);
        boolean mobGrief   = plugin.getConfig().getBoolean("protection.entity-grief.enabled", true);
        boolean redstone   = plugin.getConfig().getBoolean("protection.interactions.enabled", true);
        boolean pvp        = !plugin.getConfig().getBoolean("protection.pvp-in-claims", false); // false means block PvP
        boolean debug      = plugin.isDebug();

        // Top row core toggles
        inv.setItem(10, make(Material.BARRIER, colorToggle("Bypass (admin)", false),
                List.of(l("Use /proshield bypass toggle"), dim("Permission: proshield.admin"))));
        inv.setItem(11, toggleItem(Material.TOTEM_OF_UNDYING, "Keep Items in Claims", keepItems,
                "Protect dropped items from despawn inside claims",
                "proshield.admin.keepitems"));
        inv.setItem(12, toggleItem(Material.BLAZE_POWDER, "Fire Protection", fire,
                "Block fire spread/burn/ignition in claims", "proshield.admin"));
        inv.setItem(13, toggleItem(Material.TNT, "Explosion Protection", explosions,
                "Block TNT/creeper/wither/dragon damage in claims", "proshield.admin"));
        inv.setItem(14, toggleItem(Material.SHEARS, "Mob Grief Protection", mobGrief,
                "Block Enderman/Ravager/etc. grief in claims", "proshield.admin"));
        inv.setItem(15, toggleItem(Material.REDSTONE_TORCH, "Redstone Interactions", redstone,
                "Control buttons/levers/plates mode", "proshield.admin"));
        inv.setItem(16, toggleItem(Material.IRON_SWORD, "PvP Allowed (claims)", pvp,
                "Toggle if PvP is allowed inside claims", "proshield.admin"));

        // Middle row claim mgmt
        inv.setItem(19, make(Material.COMPASS, ChatColor.AQUA + "Teleport to Claim",
                List.of(l("Open selector (soon)"), dim("2.0 will add richer tools"))));
        inv.setItem(20, make(Material.BARRIER, ChatColor.RED + "Force Unclaim",
                List.of(l("Click a claimed chunk to remove"), dim("Confirmation required"))));
        inv.setItem(21, make(Material.PAPER, ChatColor.YELLOW + "Transfer Ownership",
                List.of(l("Transfer current claim to player"), dim("Select player later"))));
        inv.setItem(22, make(Material.OAK_PLANKS, ChatColor.GREEN + "Merge Claims (Preview)",
                List.of(l("Preview borders; not destructive"), dim("2.0 will enhance"))));
        inv.setItem(23, make(Material.CLOCK, ChatColor.GOLD + "Preview Expired Claims",
                List.of(l("Run dry purge now"), dim("/proshield purgeexpired <days> dryrun"))));
        inv.setItem(24, make(Material.NETHER_STAR, ChatColor.AQUA + "Reload Configs",
                List.of(l("Hot-reload config & protections"), dim("/proshield reload"))));

        // Bottom row insights & tools
        inv.setItem(28, make(Material.BOOK, ChatColor.AQUA + "Claim Stats",
                List.of(l("Totals, top owners (soon)"), dim("Light summary in chat"))));
        inv.setItem(29, make(Material.COMPARATOR, ChatColor.YELLOW + "List Expired Claims",
                List.of(l("Show count (dryrun) now"))));
        inv.setItem(30, toggleItem(Material.OAK_SIGN, "Debug Mode", debug,
                "Verbose logging in console", "proshield.admin"));
        inv.setItem(31, make(Material.MAP, ChatColor.GREEN + "Claim Flags Editor",
                List.of(l("Per-claim flags editor (soon)"), dim("2.0 feature"))));
        inv.setItem(32, make(Material.PLAYER_HEAD, ChatColor.AQUA + "Most Active Claims",
                List.of(l("Coming soon"))));
        inv.setItem(33, make(Material.BLACK_BANNER, ChatColor.RED + "Next Expired Claim TP",
                List.of(l("Teleport to oldest expired (soon)"))));

        // Navigation & info
        inv.setItem(48, make(Material.ARROW, ChatColor.YELLOW + "Back",
                List.of(l("Return to ProShield main menu"))));
        inv.setItem(50, make(Material.WRITABLE_BOOK, ChatColor.GOLD + "Admin Help",
                List.of(l("Show short help in chat"))));
        inv.setItem(52, make(Material.BEACON, ChatColor.LIGHT_PURPLE + "ProShield 2.0 — Teaser",
                List.of(l("Towns, shops, flags, map overlay… soon™"))));

        return inv;
    }

    public void openAdmin(Player p) {
        p.openInventory(buildAdmin(p));
    }

    /* ========= Small helpers ========= */
    private ItemStack make(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(name);
            if (lore != null) m.setLore(lore);
            m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
        }
        return it;
    }

    private ItemStack toggleItem(Material mat, String label, boolean on, String explain, String perm) {
        return make(mat, colorToggle(label, on),
                List.of(
                        ChatColor.GRAY + explain,
                        ChatColor.DARK_GRAY + "(" + perm + ")",
                        ChatColor.YELLOW + "Current: " + (on ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"),
                        ChatColor.GRAY + "Click to toggle"
                ));
    }

    private String colorToggle(String label, boolean on) {
        return (on ? ChatColor.GREEN : ChatColor.RED) + label;
    }

    private String l(String s) { return ChatColor.GRAY + s; }
    private String dim(String s){ return ChatColor.DARK_GRAY + s; }

    /* ========= Toggle actions wired for Admin GUI ========= */

    public void toggleKeepItems(HumanEntity clicker) {
        boolean cur = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        plugin.getConfig().set("claims.keep-items.enabled", !cur);
        plugin.saveConfig();
        clicker.sendMessage(prefix() + ChatColor.GOLD + "Keep Items in Claims: " + (cur ? ChatColor.RED + "OFF" : ChatColor.GREEN + "ON"));
        reopenAdmin(clicker);
    }

    public void toggleFire(HumanEntity clicker) {
        boolean cur = plugin.getConfig().getBoolean("protection.fire.enabled", true);
        plugin.getConfig().set("protection.fire.enabled", !cur);
        plugin.saveConfig();
        plugin.reloadAllConfigs();
        clicker.sendMessage(prefix() + ChatColor.GOLD + "Fire Protection: " + (!cur ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        reopenAdmin(clicker);
    }

    public void toggleExplosions(HumanEntity clicker) {
        boolean cur = plugin.getConfig().getBoolean("protection.explosions.enabled", true);
        plugin.getConfig().set("protection.explosions.enabled", !cur);
        plugin.saveConfig();
        plugin.reloadAllConfigs();
        clicker.sendMessage(prefix() + ChatColor.GOLD + "Explosion Protection: " + (!cur ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        reopenAdmin(clicker);
    }

    public void toggleMobGrief(HumanEntity clicker) {
        boolean cur = plugin.getConfig().getBoolean("protection.entity-grief.enabled", true);
        plugin.getConfig().set("protection.entity-grief.enabled", !cur);
        plugin.saveConfig();
        plugin.reloadAllConfigs();
        clicker.sendMessage(prefix() + ChatColor.GOLD + "Mob Grief Protection: " + (!cur ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        reopenAdmin(clicker);
    }

    public void toggleRedstone(HumanEntity clicker) {
        boolean cur = plugin.getConfig().getBoolean("protection.interactions.enabled", true);
        plugin.getConfig().set("protection.interactions.enabled", !cur);
        plugin.saveConfig();
        plugin.reloadAllConfigs();
        clicker.sendMessage(prefix() + ChatColor.GOLD + "Redstone/Interactions: " + (!cur ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        reopenAdmin(clicker);
    }

    public void togglePvPAllowed(HumanEntity clicker) {
        // our config uses 'pvp-in-claims: false' to BLOCK pvp. So invert meaning.
        boolean blockPvp = plugin.getConfig().getBoolean("protection.pvp-in-claims", false);
        boolean allowPvp = !(!blockPvp); // i.e., allowPvp = !blockPvp
        // flip: if currently blocked, set true to allow; else set false to block
        plugin.getConfig().set("protection.pvp-in-claims", allowPvp ? true : false);
        plugin.saveConfig();
        plugin.reloadAllConfigs();
        clicker.sendMessage(prefix() + ChatColor.GOLD + "PvP inside claims: " + (allowPvp ? ChatColor.GREEN + "ALLOWED" : ChatColor.RED + "BLOCKED"));
        reopenAdmin(clicker);
    }

    public void toggleDebug(HumanEntity clicker) {
        boolean d = plugin.isDebug();
        plugin.setDebug(!d);
        clicker.sendMessage(prefix() + ChatColor.GOLD + "Debug: " + (!d ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        reopenAdmin(clicker);
    }

    public void runExpiryPreview(HumanEntity clicker) {
        int days = plugin.getConfig().getInt("expiry.days", 30);
        int count = plugin.getPlotManager().cleanupExpiredClaims(days, true);
        clicker.sendMessage(prefix() + ChatColor.YELLOW + "Expiry preview: " + ChatColor.WHITE + count + ChatColor.YELLOW + " claim(s) would be removed.");
        clicker.getWorld().playSound(clicker.getLocation(), Sound.UI_TOAST_IN, 1f, 1.2f);
    }

    public void reloadPlugin(HumanEntity clicker) {
        plugin.reloadAllConfigs();
        clicker.sendMessage(prefix() + ChatColor.GREEN + "Configurations reloaded.");
        clicker.getWorld().playSound(clicker.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        reopenAdmin(clicker);
    }

    public void showAdminHelp(Player p) {
        p.sendMessage(prefix() + ChatColor.GOLD + "Admin Help:");
        p.sendMessage(ChatColor.GRAY + " • Keep Items: prevent item despawn inside claims");
        p.sendMessage(ChatColor.GRAY + " • Fire/Explosions/Mob Grief: master claim-safe toggles");
        p.sendMessage(ChatColor.GRAY + " • PvP Allowed: allow or block PvP inside claims");
        p.sendMessage(ChatColor.GRAY + " • Reload: hot-reload config & protections");
        p.sendMessage(ChatColor.DARK_GRAY + "More coming in ProShield 2.0 (towns, shop protection, flags editor)!");
    }

    private void reopenAdmin(HumanEntity clicker) {
        if (clicker instanceof Player pl) {
            Bukkit.getScheduler().runTask(plugin, () -> openAdmin(pl));
        }
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
