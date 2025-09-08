// path: src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
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
    private final GUIManager guiManager;

    public GUIListener(PlotManager plotManager, GUIManager guiManager) {
        this.plotManager = plotManager;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        ItemStack it = e.getItem();
        if (it == null || it.getType() != Material.COMPASS) return;
        if (!it.hasItemMeta() || !it.getItemMeta().hasDisplayName()) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName());
        if (!"ProShield Compass".equalsIgnoreCase(name)) return;

        e.setCancelled(true);
        guiManager.openMain(e.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null || e.getCurrentItem() == null) return;
        String title = e.getView().getTitle();
        final Player p = (e.getWhoClicked() instanceof Player) ? (Player) e.getWhoClicked() : null;
        if (p == null) return;

        // MAIN
        if (GUIManager.TITLE_MAIN.equals(title)) {
            e.setCancelled(true);
            int slot = e.getRawSlot();
            Location loc = p.getLocation();

            int SLOT_CLAIM   = ProShield.getInstance().getConfig().getInt("gui.slots.claim", 11);
            int SLOT_INFO    = ProShield.getInstance().getConfig().getInt("gui.slots.info", 13);
            int SLOT_UNCLAIM = ProShield.getInstance().getConfig().getInt("gui.slots.unclaim", 15);
            int SLOT_HELP    = ProShield.getInstance().getConfig().getInt("gui.slots.help", 31);
            int SLOT_ADMIN   = ProShield.getInstance().getConfig().getInt("gui.slots.admin", 33);

            if (slot == SLOT_CLAIM) {
                boolean ok = plotManager.createClaim(p.getUniqueId(), loc);
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim created for this chunk."
                        : ChatColor.RED + "Already claimed or you reached your limit."));
            } else if (slot == SLOT_INFO) {
                plotManager.getClaim(loc).ifPresentOrElse(c -> {
                    String owner = plotManager.ownerName(c.getOwner());
                    var trusted = plotManager.listTrusted(loc);
                    p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + owner);
                    p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (trusted.isEmpty() ? "(none)" : String.join(", ", trusted)));
                }, () -> p.sendMessage(prefix() + ChatColor.GRAY + "No claim in this chunk."));
            } else if (slot == SLOT_UNCLAIM) {
                boolean ok = plotManager.removeClaim(p.getUniqueId(), loc, false);
                p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim removed."
                        : ChatColor.RED + "No claim here or you are not the owner."));
            } else if (slot == SLOT_HELP) {
                guiManager.openHelp(p);
            } else if (slot == SLOT_ADMIN) {
                if (!p.hasPermission("proshield.admin")) {
                    p.sendMessage(prefix() + ChatColor.RED + "You don't have permission.");
                    return;
                }
                guiManager.openAdmin(p);
            }
            return;
        }

        // ADMIN
        if (GUIManager.TITLE_ADMIN.equals(title)) {
            e.setCancelled(true);
            int slot = e.getRawSlot();

            if (slot == 11) { // toggle keep-drops
                if (!p.hasPermission("proshield.admin.keepdrops")) {
                    p.sendMessage(prefix() + ChatColor.RED + "You don't have permission (proshield.admin.keepdrops).");
                    return;
                }
                boolean enabled = ProShield.getInstance().getConfig().getBoolean("claims.keep-items.enabled", false);
                enabled = !enabled;
                ProShield.getInstance().getConfig().set("claims.keep-items.enabled", enabled);
                ProShield.getInstance().saveConfig();
                p.sendMessage(prefix() + ChatColor.YELLOW + "Keep Items in Claims: " + (enabled ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
                guiManager.openAdmin(p); // refresh
            }
            return;
        }

        // HELP
        if (GUIManager.TITLE_HELP.equals(title)) {
            e.setCancelled(true);
        }
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                ProShield.getInstance().getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
