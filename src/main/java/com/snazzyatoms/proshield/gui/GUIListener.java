package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;

/**
 * Handles ProShield GUI clicks and the compass opener.
 *
 * - Inventory title gated by GUIManager.TITLE
 * - Uses slot positions read by GUIManager (which loads from config)
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUIManager gui;

    // Cached slots (read on each click from config so /proshield reload reflects immediately)
    private int MAIN_SLOT_CREATE = 11;
    private int MAIN_SLOT_INFO   = 13;
    private int MAIN_SLOT_REMOVE = 15;
    private int MAIN_SLOT_BACK   = 48;
    private int MAIN_SLOT_HELP   = 49;
    private int MAIN_SLOT_ADMIN  = 33;

    private int ADMIN_SLOT_TOGGLE_DROP_IF_FULL = 20;
    private int ADMIN_SLOT_HELP                = 22;
    private int ADMIN_SLOT_BACK                = 31;

    public GUIListener(PlotManager plots, GUIManager gui) {
        this.plugin = ProShield.getInstance();
        this.plots  = plots;
        this.gui    = gui;
        loadSlots();
    }

    private void loadSlots() {
        MAIN_SLOT_CREATE = plugin.getConfig().getInt("gui.slots.main.create", 11);
        MAIN_SLOT_INFO   = plugin.getConfig().getInt("gui.slots.main.info", 13);
        MAIN_SLOT_REMOVE = plugin.getConfig().getInt("gui.slots.main.remove", 15);
        MAIN_SLOT_BACK   = plugin.getConfig().getInt("gui.slots.main.back", 48);
        MAIN_SLOT_HELP   = plugin.getConfig().getInt("gui.slots.main.help", 49);
        MAIN_SLOT_ADMIN  = plugin.getConfig().getInt("gui.slots.main.admin", 33);

        ADMIN_SLOT_TOGGLE_DROP_IF_FULL = plugin.getConfig().getInt("gui.slots.admin.toggle-drop-if-full", 20);
        ADMIN_SLOT_HELP                = plugin.getConfig().getInt("gui.slots.admin.help", 22);
        ADMIN_SLOT_BACK                = plugin.getConfig().getInt("gui.slots.admin.back", 31);
    }

    /* -----------------------------------------------------------
       Open GUI via ProShield Compass
       ----------------------------------------------------------- */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onCompassUse(PlayerInteractEvent event) {
        // Only right-click with main hand
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());
        if (name == null) return;
        if (!name.equalsIgnoreCase("ProShield Compass")) return;

        event.setCancelled(true);
        gui.openMain(event.getPlayer());
    }

    /* -----------------------------------------------------------
       Handle GUI Clicks
       ----------------------------------------------------------- */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        if (event.getView() == null || event.getView().getTitle() == null) return;

        // Gentle refresh of slots so /proshield reload reflects live
        loadSlots();

        String title = ChatColor.stripColor(event.getView().getTitle());
        String expected = ChatColor.stripColor(GUIManager.TITLE);
        if (!expected.equalsIgnoreCase(title)) return; // not our GUI

        event.setCancelled(true); // Prevent taking items from our menus

        int slot = event.getRawSlot();
        boolean topInv = slot < event.getView().getTopInventory().getSize();
        if (!topInv) return; // clicks in player inventory are ignored here

        // Which screen are we on? We use button identity rather than tracking state machine.
        // Main screen actions:
        if (slot == MAIN_SLOT_CREATE) {
            // Claim current chunk
            boolean ok = plots.createClaim(p.getUniqueId(), p.getLocation());
            if (ok) {
                p.sendMessage(prefix() + ChatColor.GREEN + "Chunk claimed.");
            } else {
                p.sendMessage(prefix() + ChatColor.RED + "Cannot claim here.");
            }
            p.closeInventory();
            return;
        }

        if (slot == MAIN_SLOT_INFO) {
            plots.getClaim(p.getLocation()).ifPresentOrElse(c -> {
                String owner = plots.ownerName(c.getOwner());
                p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + ChatColor.WHITE + owner);
                var trusted = plots.listTrusted(p.getLocation());
                p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + ChatColor.WHITE +
                        (trusted.isEmpty() ? "(none)" : String.join(", ", trusted)));
            }, () -> p.sendMessage(prefix() + ChatColor.RED + "This chunk is not claimed."));
            p.closeInventory();
            return;
        }

        if (slot == MAIN_SLOT_REMOVE) {
            boolean ok = plots.removeClaim(p.getUniqueId(), p.getLocation(), false);
            if (ok) {
                p.sendMessage(prefix() + ChatColor.YELLOW + "Claim removed.");
            } else {
                p.sendMessage(prefix() + ChatColor.RED + "You are not the owner of this claim.");
            }
            p.closeInventory();
            return;
        }

        if (slot == MAIN_SLOT_HELP) {
            gui.openHelp(p);
            return;
        }

        if (slot == MAIN_SLOT_ADMIN) {
            if (!p.hasPermission("proshield.admin.gui") && !p.hasPermission("proshield.admin")) {
                p.sendMessage(prefix() + ChatColor.RED + "You do not have permission to open the Admin panel.");
                return;
            }
            gui.openAdmin(p);
            return;
        }

        if (slot == MAIN_SLOT_BACK) {
            gui.openMain(p);
            return;
        }

        // Admin screen actions:
        if (slot == ADMIN_SLOT_TOGGLE_DROP_IF_FULL) {
            if (!p.hasPermission("proshield.admin")) {
                p.sendMessage(prefix() + ChatColor.RED + "You do not have permission to change admin settings.");
                return;
            }
            boolean current = plugin.getConfig().getBoolean("compass.drop-if-full", true);
            plugin.getConfig().set("compass.drop-if-full", !current);
            plugin.saveConfig();
            p.sendMessage(prefix() + ChatColor.YELLOW + "Compass drop-if-full set to " +
                    (plugin.getConfig().getBoolean("compass.drop-if-full") ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
            // refresh screen
            gui.openAdmin(p);
            return;
        }

        if (slot == ADMIN_SLOT_HELP) {
            // purely informational (set by GUI)
            p.sendMessage(prefix() + ChatColor.GRAY + "Admin panel. Configure spawn no-claim radius and misc toggles in config.yml.");
            return;
        }

        if (slot == ADMIN_SLOT_BACK) {
            gui.openMain(p);
        }
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
