package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final PlotManager plotManager;
    private final GUIManager gui;

    public GUIListener(PlotManager plotManager, GUIManager guiManager) {
        this.plotManager = plotManager;
        this.gui = guiManager;
    }

    /* -------- Open main with the special compass -------- */
    @EventHandler
    public void onCompassUse(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        ItemStack it = e.getItem();
        if (it == null || it.getType() != Material.COMPASS) return;
        if (!it.hasItemMeta() || !it.getItemMeta().hasDisplayName()) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName());
        if (!"ProShield Compass".equalsIgnoreCase(name)) return;

        e.setCancelled(true);
        gui.openMain(e.getPlayer());
    }

    /* -------- Handle inventory clicks -------- */
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getView() == null) return;

        String title = e.getView().getTitle();
        ItemStack clicked = e.getCurrentItem();
        e.setCancelled(true); // keep GUIs non-takeable

        if (clicked == null) return;
        Location loc = p.getLocation();

        if (GUIManager.TITLE_MAIN.equals(title)) {
            int slot = e.getRawSlot();
            if (slot == gui.SLOT_MAIN_CREATE()) {
                boolean ok = plotManager.createClaim(p.getUniqueId(), loc);
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim created for this chunk."
                                             : ChatColor.RED + "Already claimed or you reached your limit."));
            } else if (slot == gui.SLOT_MAIN_INFO()) {
                plotManager.getClaim(loc).ifPresentOrElse(c -> {
                    String owner = plotManager.ownerName(c.getOwner());
                    var trusted = plotManager.listTrusted(loc);
                    p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + owner);
                    p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (trusted.isEmpty() ? "(none)" : String.join(", ", trusted)));
                }, () -> p.sendMessage(prefix() + ChatColor.GRAY + "No claim in this chunk."));
            } else if (slot == gui.SLOT_MAIN_REMOVE()) {
                boolean ok = plotManager.removeClaim(p.getUniqueId(), loc, false);
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim removed."
                                             : ChatColor.RED + "No claim here or you are not the owner."));
            } else if (slot == gui.SLOT_MAIN_ADMIN()) {
                if (p.hasPermission("proshield.admin")) {
                    gui.openAdmin(p);
                } else {
                    p.sendMessage(prefix() + ChatColor.RED + "You do not have permission to open the admin menu.");
                }
            } else if (slot == gui.SLOT_MAIN_BACK()) {
                p.closeInventory();
            }
            // Help item (SLOT_MAIN_HELP) is tooltip-only; no action
            return;
        }

        if (GUIManager.TITLE_ADMIN.equals(title)) {
            int slot = e.getRawSlot();
            if (slot == gui.SLOT_ADMIN_TOGGLE_DROP()) {
                boolean current = p.getServer().getPluginManager().getPlugin("ProShield")
                        .getConfig().getBoolean("compass.drop-if-full", true);
                boolean next = !current;
                p.getServer().getPluginManager().getPlugin("ProShield")
                        .getConfig().set("compass.drop-if-full", next);
                p.getServer().getPluginManager().getPlugin("ProShield").saveConfig();
                p.sendMessage(prefix() + ChatColor.GOLD + "Compass drop-if-full set to "
                        + (next ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF") + ChatColor.GOLD + ".");
                gui.openAdmin(p); // refresh
            } else if (slot == gui.SLOT_ADMIN_BACK()) {
                gui.openMain(p);
            }
            // Help item is tooltip-only; no action
        }
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                com.snazzyatoms.proshield.ProShield.getInstance()
                        .getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
