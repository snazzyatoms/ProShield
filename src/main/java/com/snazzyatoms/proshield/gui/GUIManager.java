package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.plots.PlotSettings;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUIManager
 * - Preserves prior logic (compass, menus, role/flag toggles, cache, admin tools)
 * - EXPANDED: Admin → Wilderness Tools (inspect / claim self / claim for player / unclaim)
 * - Still opens main GUI on compass right-click
 */
public class GUIManager implements Listener {

    private static final String TITLE_MAIN           = ChatColor.DARK_AQUA + "ProShield";
    private static final String TITLE_FLAGS          = ChatColor.BLUE + "Flags";
    private static final String TITLE_ROLES          = ChatColor.BLUE + "Roles";
    private static final String TITLE_TRUST          = ChatColor.BLUE + "Trust Player";
    private static final String TITLE_UNTRUST        = ChatColor.BLUE + "Untrust Player";
    private static final String TITLE_TRANSFER       = ChatColor.BLUE + "Transfer Ownership";
    private static final String TITLE_ADMIN          = ChatColor.DARK_RED + "Admin";
    private static final String TITLE_ADMIN_WILD     = ChatColor.DARK_RED + "Admin • Wilderness";
    private static final String TITLE_PICK_PLAYER    = ChatColor.GOLD + "Pick Player • ";

    private final ProShield plugin;
    private final GUICache cache;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil msg;

    // Simple picker callbacks: opener UUID -> action(targetUUID)
    private final Map<UUID, java.util.function.Consumer<UUID>> pendingPickers = new ConcurrentHashMap<>();

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
        this.plots = plugin.getPlotManager();
        this.roles = plugin.getRoleManager();
        this.msg   = plugin.getMessagesUtil();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* =========================================================
     * COMPASS (kept)
     * ========================================================= */

    public void giveCompass(Player player, boolean force) {
        if (player == null) return;
        ItemStack compass = buildCompass();
        boolean has = player.getInventory().containsAtLeast(compass, 1);
        if (force || !has) {
            player.getInventory().addItem(compass);
            if (plugin.isDebugEnabled()) plugin.getLogger().info("Gave compass to " + player.getName());
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
        if (!ChatColor.stripColor(String.valueOf(meta.getDisplayName())).equals("ProShield Compass")) return;

        e.setCancelled(true);
        openMain(e.getPlayer());
    }

    /* =========================================================
     * PUBLIC API (kept names used elsewhere)
     * ========================================================= */

    public void openMain(Player p)       { openMainInternal(p); }
    public void openAdmin(Player p)      { openAdminInternal(p); }
    public void openFlagsMenu(Player p)  { openFlagsInternal(p); }
    public void openRolesMenu(Player p)  { openRolesInternal(p); }
    public void openTrustMenu(Player p)  { openTrustInternal(p); }
    public void openUntrustMenu(Player p){ openUntrustInternal(p); }
    public void openTransferMenu(Player p){ openTransferInternal(p); }

    /** Legacy signature kept. */
    public void openRolesGUI(Player p, Plot plot) { openRolesInternal(p, plot); }

    public GUICache getCache() { return cache; }
    public void clearCache()   { cache.clearCache(); }

    /* =========================================================
     * INVENTORY CLICK ROUTER
     * ========================================================= */

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        Inventory inv = e.getInventory();
        String title = inv.getTitle();

        try {
            if (title.equals(TITLE_MAIN)) {
                e.setCancelled(true);
                handleMainClick(p, e.getCurrentItem(), e.getClick());
            } else if (title.equals(TITLE_FLAGS)) {
                e.setCancelled(true);
                handleFlagsClick(p, e.getCurrentItem(), e.getClick());
            } else if (title.equals(TITLE_ROLES)) {
                e.setCancelled(true);
                handleRolesClick(p, e.getCurrentItem(), e.getClick());
            } else if (title.equals(TITLE_TRUST)) {
                e.setCancelled(true);
                handleTrustClick(p, e.getCurrentItem(), e.getClick());
            } else if (title.equals(TITLE_UNTRUST)) {
                e.setCancelled(true);
                handleUntrustClick(p, e.getCurrentItem(), e.getClick());
            } else if (title.equals(TITLE_TRANSFER)) {
                e.setCancelled(true);
                handleTransferClick(p, e.getCurrentItem(), e.getClick());
            } else if (title.equals(TITLE_ADMIN)) {
                e.setCancelled(true);
                handleAdminClick(p, e.getCurrentItem(), e.getClick());
            } else if (title.equals(TITLE_ADMIN_WILD)) {
                e.setCancelled(true);
                handleAdminWildernessClick(p, e.getCurrentItem(), e.getClick());
            } else if (title.startsWith(TITLE_PICK_PLAYER)) {
                e.setCancelled(true);
                handlePickPlayerClick(p, e.getCurrentItem(), e.getClick());
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("GUI click error: " + t.getMessage());
        }
    }

    /* =========================================================
     * MAIN (kept)
     * ========================================================= */

    private void openMainInternal(Player p) {
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_MAIN);

        set(inv, 10, make(Material.MAP, "Claim Info", l(
                "Shows info about your current chunk.",
                "Click to view."
        )));
        set(inv, 11, make(Material.PLAYER_HEAD, "Trust", l(
                "Trust an online player into your claim.",
                "Left-click to open."
        )));
        set(inv, 12, make(Material.BARRIER, "Untrust", l(
                "Remove trusted players.",
                "Left-click to open."
        )));
        set(inv, 13, make(Material.BOOK, "Roles", l(
                "Manage trusted player roles.",
                "Left-click to open."
        )));
        set(inv, 14, make(Material.LEVER, "Flags", l(
                "Toggle PvP, fire, explosions, mob repel...",
                "Left-click to open."
        )));
        set(inv, 15, make(Material.NAME_TAG, "Transfer", l(
                "Transfer ownership to another player.",
                "Left-click to open."
        )));
        if (p.hasPermission("proshield.admin")) {
            set(inv, 16, make(Material.REDSTONE_BLOCK, ChatColor.RED + "Admin", l(
                    "Admin tools: reload, wilderness tools, debug...",
                    "Left-click to open."
            )));
        }

        p.openInventory(inv);
    }

    private void handleMainClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName());
        switch (name.toLowerCase(Locale.ROOT)) {
            case "claim info" -> p.performCommand("info");
            case "trust"      -> openTrustInternal(p);
            case "untrust"    -> openUntrustInternal(p);
            case "roles"      -> openRolesInternal(p);
            case "flags"      -> openFlagsInternal(p);
            case "transfer"   -> openTransferInternal(p);
            case "admin"      -> { if (p.hasPermission("proshield.admin")) openAdminInternal(p); }
        }
    }

    /* =========================================================
     * FLAGS (kept)
     * ========================================================= */

    private void openFlagsInternal(Player p) {
        Plot plot = plots.getPlot(p.getLocation());
        if (plot == null) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "You are not in a claim."); return; }

        Inventory inv = Bukkit.createInventory(dummyHolder(), 54, TITLE_FLAGS);
        PlotSettings s = plot.getSettings();

        set(inv, 10, toggleItem(Material.IRON_SWORD, "PvP", s.isPvpEnabled()));
        set(inv, 11, toggleItem(Material.FLINT_AND_STEEL, "Fire", s.isFireAllowed()));
        set(inv, 12, toggleItem(Material.TNT, "Explosions", s.isExplosionsAllowed()));
        set(inv, 13, toggleItem(Material.REDSTONE, "Redstone", s.isRedstoneAllowed()));
        set(inv, 14, toggleItem(Material.CREEPER_HEAD, "Entity Griefing", s.isEntityGriefingAllowed()));
        set(inv, 19, toggleItem(Material.TOTEM_OF_UNDYING, "Keep Items", s.isKeepItemsEnabled()));
        set(inv, 20, toggleItem(Material.SHIELD, "Mob Repel", s.isMobRepelEnabled()));
        set(inv, 21, toggleItem(Material.ZOMBIE_HEAD, "Despawn Inside", s.isMobDespawnInsideEnabled()));

        // Damage sub-flags
        set(inv, 28, toggleItem(Material.BOW, "Damage: Projectiles", s.isDamageProjectilesEnabled()));
        set(inv, 29, toggleItem(Material.DIAMOND_SWORD, "Damage: PvE", s.isDamagePveEnabled()));
        set(inv, 30, toggleItem(Material.LAVA_BUCKET, "Damage: Fire/Lava", s.isDamageFireLavaEnabled()));
        set(inv, 31, toggleItem(Material.FEATHER, "Damage: Fall", s.isDamageFallEnabled()));
        set(inv, 32, toggleItem(Material.TNT_MINECART, "Damage: Explosions", s.isDamageExplosionsEnabled()));
        set(inv, 33, toggleItem(Material.WATER_BUCKET, "Damage: Drown/Void/Suffocate", s.isDamageDrownVoidSuffocateEnabled()));
        set(inv, 34, toggleItem(Material.SPIDER_EYE, "Damage: Poison/Wither", s.isDamagePoisonWitherEnabled()));
        set(inv, 40, toggleItem(Material.OAK_LEAVES, "Damage: Environment", s.isDamageEnvironmentEnabled()));

        p.openInventory(inv);
    }

    private void handleFlagsClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        Plot plot = plots.getPlot(p.getLocation());
        if (plot == null) { p.closeInventory(); return; }
        PlotSettings s = plot.getSettings();
        String key = ChatColor.stripColor(it.getItemMeta().getDisplayName()).toLowerCase(Locale.ROOT);

        switch (key) {
            case "pvp" -> s.setPvpEnabled(!s.isPvpEnabled());
            case "fire" -> s.setFireAllowed(!s.isFireAllowed());
            case "explosions" -> s.setExplosionsAllowed(!s.isExplosionsAllowed());
            case "redstone" -> s.setRedstoneAllowed(!s.isRedstoneAllowed());
            case "entity griefing" -> s.setEntityGriefingAllowed(!s.isEntityGriefingAllowed());
            case "keep items" -> s.setKeepItemsEnabled(!s.isKeepItemsEnabled());
            case "mob repel" -> s.setMobRepelEnabled(!s.isMobRepelEnabled());
            case "despawn inside" -> s.setMobDespawnInsideEnabled(!s.isMobDespawnInsideEnabled());

            case "damage: projectiles" -> s.setDamageProjectilesEnabled(!s.isDamageProjectilesEnabled());
            case "damage: pve" -> s.setDamagePveEnabled(!s.isDamagePveEnabled());
            case "damage: fire/lava" -> s.setDamageFireLavaEnabled(!s.isDamageFireLavaEnabled());
            case "damage: fall" -> s.setDamageFallEnabled(!s.isDamageFallEnabled());
            case "damage: explosions" -> s.setDamageExplosionsEnabled(!s.isDamageExplosionsEnabled());
            case "damage: drown/void/suffocate" -> s.setDamageDrownVoidSuffocateEnabled(!s.isDamageDrownVoidSuffocateEnabled());
            case "damage: poison/wither" -> s.setDamagePoisonWitherEnabled(!s.isDamagePoisonWitherEnabled());
            case "damage: environment" -> s.setDamageEnvironmentEnabled(!s.isDamageEnvironmentEnabled());
            default -> { return; }
        }
        plots.saveAsync(plot);
        openFlagsInternal(p);
    }

    /* =========================================================
     * ROLES (kept)
     * ========================================================= */

    private void openRolesInternal(Player p) { openRolesInternal(p, plots.getPlot(p.getLocation())); }

    private void openRolesInternal(Player p, Plot plot) {
        if (plot == null) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "You are not in a claim."); return; }
        Inventory inv = Bukkit.createInventory(dummyHolder(), 54, TITLE_ROLES);

        // owner
        set(inv, 4, head(plot.getOwner(), ChatColor.GOLD + "Owner", List.of("The claim owner.")));

        // trusted list
        int slot = 9;
        for (Map.Entry<UUID, ClaimRole> e : plot.getTrusted().entrySet()) {
            if (slot >= 54) break;
            UUID id = e.getKey();
            ClaimRole r = e.getValue();
            set(inv, slot++, head(id,
                    ChatColor.AQUA + nameOf(id),
                    List.of(
                            ChatColor.GRAY + "Role: " + ChatColor.YELLOW + r.name(),
                            ChatColor.GRAY + "Left-click: cycle role",
                            ChatColor.GRAY + "Shift+Right: untrust"
                    )));
        }

        p.openInventory(inv);
    }

    private void handleRolesClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        Plot plot = plots.getPlot(p.getLocation());
        if (plot == null) { p.closeInventory(); return; }

        if (!canEdit(p, plot)) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "You cannot edit roles here."); return; }

        UUID target = skullOwner(it);
        if (target == null) return;

        if (plot.getOwner() != null && plot.getOwner().equals(target)) return;

        if (click.isShiftClick() && click.isRightClick()) {
            plot.getTrusted().remove(target);
            plots.saveAsync(plot);
            openRolesInternal(p);
            return;
        }

        ClaimRole current = plot.getTrusted().getOrDefault(target, ClaimRole.VISITOR);
        ClaimRole next = nextRole(current);
        plot.getTrusted().put(target, next);
        plots.saveAsync(plot);
        openRolesInternal(p);
    }

    private boolean canEdit(Player p, Plot plot) {
        UUID id = p.getUniqueId();
        return (plot.getOwner() != null && plot.getOwner().equals(id)) || p.hasPermission("proshield.admin");
    }

    private ClaimRole nextRole(ClaimRole r) {
        ClaimRole[] order = { ClaimRole.VISITOR, ClaimRole.CONTAINER, ClaimRole.BUILDER, ClaimRole.COOWNER, ClaimRole.OWNER };
        for (int i = 0; i < order.length; i++) {
            if (order[i] == r) return order[(i + 1) % order.length];
        }
        return ClaimRole.BUILDER;
    }

    /* =========================================================
     * TRUST / UNTRUST (kept)
     * ========================================================= */

    private void openTrustInternal(Player p) {
        Plot plot = plots.getPlot(p.getLocation());
        if (plot == null) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "You are not in a claim."); return; }
        Inventory inv = Bukkit.createInventory(dummyHolder(), 54, TITLE_TRUST);

        int i = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (i >= 54) break;
            if (plot.getOwner() != null && plot.getOwner().equals(online.getUniqueId())) continue;
            if (plot.getTrusted().containsKey(online.getUniqueId())) continue;
            set(inv, i++, head(online.getUniqueId(),
                    ChatColor.GREEN + online.getName(),
                    List.of("Left-click to trust as BUILDER", "Right-click to trust as CONTAINER", "Shift-click for VISITOR")));
        }
        p.openInventory(inv);
    }

    private void handleTrustClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        Plot plot = plots.getPlot(p.getLocation());
        if (plot == null) { p.closeInventory(); return; }
        if (!canEdit(p, plot)) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "You cannot modify this claim."); return; }

        UUID target = skullOwner(it);
        if (target == null) return;

        ClaimRole role = switch (click) {
            case RIGHT -> ClaimRole.CONTAINER;
            case SHIFT_LEFT, SHIFT_RIGHT -> ClaimRole.VISITOR;
            default -> ClaimRole.BUILDER;
        };
        plot.getTrusted().put(target, role);
        plots.saveAsync(plot);
        openRolesInternal(p);
    }

    private void openUntrustInternal(Player p) {
        Plot plot = plots.getPlot(p.getLocation());
        if (plot == null) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "You are not in a claim."); return; }
        Inventory inv = Bukkit.createInventory(dummyHolder(), 54, TITLE_UNTRUST);

        int i = 0;
        for (UUID id : plot.getTrusted().keySet()) {
            if (i >= 54) break;
            set(inv, i++, head(id,
                    ChatColor.RED + nameOf(id),
                    List.of("Click to untrust")));
        }
        p.openInventory(inv);
    }

    private void handleUntrustClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        Plot plot = plots.getPlot(p.getLocation());
        if (plot == null) { p.closeInventory(); return; }
        if (!canEdit(p, plot)) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "You cannot modify this claim."); return; }

        UUID target = skullOwner(it);
        if (target == null) return;
        plot.getTrusted().remove(target);
        plots.saveAsync(plot);
        openRolesInternal(p);
    }

    /* =========================================================
     * TRANSFER (kept)
     * ========================================================= */

    private void openTransferInternal(Player p) {
        Plot plot = plots.getPlot(p.getLocation());
        if (plot == null) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "You are not in a claim."); return; }
        if (!canEdit(p, plot)) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "Only owners/admin can transfer."); return; }

        Inventory inv = Bukkit.createInventory(dummyHolder(), 54, TITLE_TRANSFER);
        int i = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (i >= 54) break;
            if (online.getUniqueId().equals(plot.getOwner())) continue;
            set(inv, i++, head(online.getUniqueId(),
                    ChatColor.GOLD + online.getName(),
                    List.of("Click to transfer ownership")));
        }
        p.openInventory(inv);
    }

    private void handleTransferClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        Plot plot = plots.getPlot(p.getLocation()));
        if (plot == null) { p.closeInventory(); return; }
        if (!canEdit(p, plot)) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "Only owners/admin can transfer."); return; }

        UUID target = skullOwner(it);
        if (target == null) return;

        plot.setOwner(target);
        plots.saveAsync(plot);
        p.closeInventory();
        p.sendMessage(ProShield.PREFIX + ChatColor.GREEN + "Ownership transferred to " + nameOf(target));
    }

    /* =========================================================
     * ADMIN (expanded)
     * ========================================================= */

    private void openAdminInternal(Player p) {
        if (!p.hasPermission("proshield.admin")) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "No permission."); return; }
        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_ADMIN);

        set(inv, 10, make(Material.PAPER, "Reload", l("Reload configs and messages.")));
        set(inv, 11, make(Material.COMPASS, "Give Compass", l("Give yourself the ProShield compass.")));
        set(inv, 12, make(Material.BEDROCK, "Debug Toggle", l("Toggle global debug logging.")));
        set(inv, 13, make(Material.GRASS_BLOCK, "Wilderness Tools", l("Manage the current chunk.","Claim / Unclaim / Inspect")));
        set(inv, 16, make(Material.OAK_DOOR, "Back", l("Return to main menu.")));

        p.openInventory(inv);
    }

    private void handleAdminClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName()).toLowerCase(Locale.ROOT);
        switch (name) {
            case "reload" -> plugin.reloadAll();
            case "give compass" -> giveCompass(p, true);
            case "debug toggle" -> {
                boolean on = plugin.toggleDebug();
                p.sendMessage(ProShield.PREFIX + ChatColor.YELLOW + "Debug: " + (on ? "ON" : "OFF"));
            }
            case "wilderness tools" -> openAdminWilderness(p);
            case "back" -> openMainInternal(p);
        }
    }

    /* ----------------- Wilderness submenu ------------------ */

    private void openAdminWilderness(Player p) {
        if (!p.hasPermission("proshield.admin")) { p.sendMessage(ProShield.PREFIX + ChatColor.RED + "No permission."); return; }

        Plot plot = plots.getPlot(p.getLocation());
        boolean claimed = plot != null;

        Inventory inv = Bukkit.createInventory(dummyHolder(), 27, TITLE_ADMIN_WILD);

        // Status / info
        String status = claimed
                ? ChatColor.GREEN + "Claimed by " + nameOf(plot.getOwner())
                : ChatColor.RED + "Wilderness";
        set(inv, 10, make(Material.MAP, "Status", l(status)));

        // Actions
        set(inv, 12, make(Material.EMERALD, "Claim Here (Self)", l("Make yourself the owner of this chunk.")));
        set(inv, 13, make(Material.PLAYER_HEAD, "Claim Here (For Player)", l("Open player picker to assign owner.")));
        set(inv, 14, make(Material.BARRIER, "Unclaim Here", l("Remove the claim in this chunk.")));

        // Back
        set(inv, 16, make(Material.OAK_DOOR, "Back", l("Return to Admin")));

        p.openInventory(inv);
    }

    private void handleAdminWildernessClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName()).toLowerCase(Locale.ROOT);

        switch (name) {
            case "status" -> {
                Plot plot = plots.getPlot(p.getLocation());
                if (plot == null) {
                    p.sendMessage(ProShield.PREFIX + ChatColor.YELLOW + "This chunk is wilderness.");
                } else {
                    p.sendMessage(ProShield.PREFIX + ChatColor.YELLOW + "Owner: " + nameOf(plot.getOwner())
                            + ChatColor.GRAY + " • Trusted: " + plot.getTrusted().size());
                }
            }
            case "claim here (self)" -> {
                Plot existing = plots.getPlot(p.getLocation());
                if (existing != null) {
                    p.sendMessage(ProShield.PREFIX + ChatColor.RED + "Already claimed by " + nameOf(existing.getOwner()));
                } else {
                    Plot created = plots.createClaim(p.getUniqueId(), p.getLocation());
                    if (created != null) {
                        p.sendMessage(ProShield.PREFIX + ChatColor.GREEN + "Claimed this chunk.");
                    }
                }
                openAdminWilderness(p);
            }
            case "claim here (for player)" -> openPlayerPicker(p, "Assign Owner Here", target -> {
                Plot existing = plots.getPlot(p.getLocation());
                if (existing != null) {
                    p.sendMessage(ProShield.PREFIX + ChatColor.RED + "Already claimed by " + nameOf(existing.getOwner()));
                    return;
                }
                Plot created = plots.createClaim(target, p.getLocation());
                if (created != null) {
                    p.sendMessage(ProShield.PREFIX + ChatColor.GREEN + "Claimed this chunk for " + nameOf(target));
                }
            }, () -> openAdminWilderness(p));
            case "unclaim here" -> {
                Plot existing = plots.getPlot(p.getLocation());
                if (existing == null) {
                    p.sendMessage(ProShield.PREFIX + ChatColor.YELLOW + "This chunk is not claimed.");
                } else {
                    plots.unclaim(p.getLocation().getChunk());
                    p.sendMessage(ProShield.PREFIX + ChatColor.GREEN + "Unclaimed this chunk.");
                }
                openAdminWilderness(p);
            }
            case "back" -> openAdminInternal(p);
        }
    }

    /* ----------------- Generic Player Picker ---------------- */

    private void openPlayerPicker(Player opener, String purpose, java.util.function.Consumer<UUID> onPick, Runnable onBack) {
        pendingPickers.put(opener.getUniqueId(), onPick);

        Inventory inv = Bukkit.createInventory(dummyHolder(), 54, TITLE_PICK_PLAYER + purpose);
        int i = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (i >= 53) break; // keep last slot for "Back"
            set(inv, i++, head(online.getUniqueId(),
                    ChatColor.GREEN + online.getName(),
                    List.of("Click to select")));
        }
        set(inv, 53, make(Material.OAK_DOOR, "Back", l("Return")));
        opener.openInventory(inv);

        // Store simple callback for "back" by reopening previous menu
        // We'll just use onBack.run() when "Back" clicked
        // (no extra state storage needed)
        // We'll call onBack in the click handler below.
        // To keep it small, we pass it via a tiny map too:
        backActions.put(opener.getUniqueId(), onBack);
    }

    private final Map<UUID, Runnable> backActions = new ConcurrentHashMap<>();

    private void handlePickPlayerClick(Player p, ItemStack it, ClickType click) {
        if (!valid(it)) return;
        String dn = ChatColor.stripColor(it.getItemMeta().getDisplayName());
        if (dn.equalsIgnoreCase("Back")) {
            Runnable r = backActions.remove(p.getUniqueId());
            if (r != null) r.run();
            return;
        }
        UUID picked = skullOwner(it);
        if (picked == null) return;

        java.util.function.Consumer<UUID> cb = pendingPickers.remove(p.getUniqueId());
        backActions.remove(p.getUniqueId());
        if (cb != null) {
            try { cb.accept(picked); }
            catch (Throwable t) { plugin.getLogger().warning("Picker callback error: " + t.getMessage()); }
        }
        // Return to admin wilderness for continuity
        openAdminWilderness(p);
    }

    /* =========================================================
     * ITEM HELPERS (kept)
     * ========================================================= */

    private static List<String> l(String... lines) { return Arrays.asList(lines); }

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
            if (enabled) {
                m.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            }
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(m);
        }
        return it;
    }

    private ItemStack head(UUID uuid, String name, List<String> lore) {
        ItemStack it = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = it.getItemMeta();
        if (meta instanceof SkullMeta sm) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            sm.setOwningPlayer(op);
            sm.setDisplayName(name);
            if (lore != null) {
                List<String> colored = new ArrayList<>();
                for (String s : lore) colored.add(ChatColor.GRAY + s);
                sm.setLore(colored);
            }
            it.setItemMeta(sm);
        }
        return it;
    }

    private String nameOf(UUID id) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(id);
        String n = op != null ? op.getName() : null;
        return n != null ? n : id.toString().substring(0, 8);
    }

    private UUID skullOwner(ItemStack skull) {
        if (skull == null || skull.getType() != Material.PLAYER_HEAD) return null;
        ItemMeta m = skull.getItemMeta();
        if (!(m instanceof SkullMeta sm)) return null;
        OfflinePlayer op = sm.getOwningPlayer();
        return (op != null) ? op.getUniqueId() : null;
    }

    private boolean valid(ItemStack it) {
        return it != null && it.getType() != Material.AIR && it.hasItemMeta() && it.getItemMeta().hasDisplayName();
    }

    private InventoryHolder dummyHolder() {
        return new InventoryHolder() { @Override public Inventory getInventory() { return null; } };
    }
}
