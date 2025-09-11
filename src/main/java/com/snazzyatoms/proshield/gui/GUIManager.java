// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
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

public class GUIManager implements Listener {

    private static final String TITLE_MAIN        = ChatColor.DARK_AQUA + "ProShield";
    private static final String TITLE_FLAGS       = ChatColor.BLUE + "Flags";
    private static final String TITLE_ROLES       = ChatColor.BLUE + "Roles";
    private static final String TITLE_TRUST       = ChatColor.BLUE + "Trust Player";
    private static final String TITLE_UNTRUST     = ChatColor.BLUE + "Untrust Player";
    private static final String TITLE_TRANSFER    = ChatColor.BLUE + "Transfer Ownership";
    private static final String TITLE_ADMIN       = ChatColor.DARK_RED + "Admin";
    private static final String TITLE_ADMIN_WILD  = ChatColor.DARK_RED + "Admin • Wilderness";
    private static final String TITLE_PICK_PLAYER = ChatColor.GOLD + "Pick Player • ";

    private final ProShield plugin;
    private final GUICache cache;
    private final PlotManager plots;
    private final ClaimRoleManager roles;
    private final MessagesUtil msg;

    private final Map<UUID, java.util.function.Consumer<UUID>> pendingPickers = new ConcurrentHashMap<>();
    private final Map<UUID, Runnable> backActions = new ConcurrentHashMap<>();

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
        this.plots = plugin.getPlotManager();
        this.roles = plugin.getRoleManager();
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
        if (!ChatColor.stripColor(meta.getDisplayName()).equals("ProShield Compass")) return;

        e.setCancelled(true);
        openMain(e.getPlayer());
    }

    /* ========================================================
     * PUBLIC API
     * ======================================================== */
    public void openMain(Player p)        { openMainInternal(p); }
    public void openAdmin(Player p)       { openAdminInternal(p); }
    public void openFlagsMenu(Player p)   { openFlagsInternal(p); }
    public void openRolesMenu(Player p)   { openRolesInternal(p); }
    public void openTrustMenu(Player p)   { openTrustInternal(p); }
    public void openUntrustMenu(Player p) { openUntrustInternal(p); }
    public void openTransferMenu(Player p){ openTransferInternal(p); }

    public void openRolesGUI(Player p, Plot plot) { openRolesInternal(p, plot); }

    public GUICache getCache() { return cache; }
    public void clearCache()   { cache.clearCache(); }

    /* ========================================================
     * INVENTORY CLICK ROUTER
     * ======================================================== */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();

        try {
            if (TITLE_MAIN.equals(title)) {
                e.setCancelled(true);
                handleMainClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_FLAGS.equals(title)) {
                e.setCancelled(true);
                handleFlagsClick(p, e.getCurrentItem(), e.getClick(), e.getSlot());
            } else if (TITLE_ROLES.equals(title)) {
                e.setCancelled(true);
                handleRolesClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_TRUST.equals(title)) {
                e.setCancelled(true);
                handleTrustClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_UNTRUST.equals(title)) {
                e.setCancelled(true);
                handleUntrustClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_TRANSFER.equals(title)) {
                e.setCancelled(true);
                handleTransferClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_ADMIN.equals(title)) {
                e.setCancelled(true);
                handleAdminClick(p, e.getCurrentItem(), e.getClick());
            } else if (TITLE_ADMIN_WILD.equals(title)) {
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

    /* ========================================================
     * FLAGS CLICK HANDLER
     * ======================================================== */
    private void handleFlagsClick(Player p, ItemStack it, ClickType click, int slot) {
        if (!valid(it)) return;

        Plot plot = plots.getPlot(p.getLocation());
        if (plot == null) return;

        // ✅ Restrict editing: only owners or admins
        if (!canEdit(p, plot)) {
            p.sendMessage(ChatColor.RED + "Only claim owners or admins can change flags.");
            return;
        }

        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName()).toLowerCase();
        PlotSettings settings = plot.getSettings();

        boolean newState = switch (name) {
            case "explosions" -> toggle(settings.isExplosionsAllowed(), settings::setExplosionsAllowed);
            case "buckets" -> toggle(settings.isBucketAllowed(), settings::setBucketAllowed);
            case "item frames" -> toggle(settings.isItemFramesAllowed(), settings::setItemFramesAllowed);
            case "armor stands" -> toggle(settings.isArmorStandsAllowed(), settings::setArmorStandsAllowed);
            case "animals" -> toggle(settings.isAnimalAccessAllowed(), settings::setAnimalAccessAllowed);
            case "pets" -> toggle(settings.isPetAccessAllowed(), settings::setPetAccessAllowed);
            case "containers" -> toggle(settings.isContainersAllowed(), settings::setContainersAllowed);
            case "vehicles" -> toggle(settings.isVehiclesAllowed(), settings::setVehiclesAllowed);
            case "fire" -> toggle(settings.isFireAllowed(), settings::setFireAllowed);
            case "redstone" -> toggle(settings.isRedstoneAllowed(), settings::setRedstoneAllowed);
            case "entity griefing" -> toggle(settings.isEntityGriefingAllowed(), settings::setEntityGriefingAllowed);
            case "pvp" -> toggle(settings.isPvpEnabled(), settings::setPvpEnabled);
            case "mob repel" -> toggle(settings.isMobRepelEnabled(), settings::setMobRepelEnabled);
            case "mob despawn" -> toggle(settings.isMobDespawnInsideEnabled(), settings::setMobDespawnInsideEnabled);
            case "keep items" -> toggle(settings.isKeepItemsEnabled(), settings::setKeepItemsEnabled);
            default -> null;
        };

        if (newState == null) return;

        // Update GUI slot immediately
        p.getOpenInventory().getTopInventory().setItem(
                slot,
                toggleItem(it.getType(), it.getItemMeta().getDisplayName(), newState)
        );

        // 🔊 Sound feedback instead of chat spam
        if (newState) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.5f);
        } else {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
        }

        // Log for admins
        plugin.getLogger().info(p.getName() + " toggled flag '" + name + "' in claim " +
                plot.getDisplayNameSafe() + " -> " + (newState ? "ENABLED" : "DISABLED"));

        plots.saveAsync(plot);
    }

    private Boolean toggle(boolean current, java.util.function.Consumer<Boolean> setter) {
        boolean newState = !current;
        setter.accept(newState);
        return newState;
    }

    /* ========================================================
     * REMAINING METHODS (same as before, unchanged)
     * ======================================================== */
    // ... (keep your openMainInternal, openAdminInternal, etc. exactly as you have them now)
    // ... (helpers like make(), toggleItem(), nameOf(), skullOwner(), dummyHolder(), canEdit() stay the same)
}
