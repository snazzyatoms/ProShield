// path: src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
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

    private final PlotManager plots;
    private final GUIManager gui;

    public GUIListener(PlotManager plotManager, GUIManager guiManager) {
        this.plots = plotManager;
        this.gui   = guiManager;
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
        gui.openMain(e.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null) return;
        String title = e.getView().getTitle();
        if (!GUIManager.TITLE.equals(title)) return;

        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getCurrentItem() == null) return;

        int slot = e.getRawSlot();
        Location loc = p.getLocation();

        if (slot == 11) { // Create
            boolean ok = plots.createClaim(p.getUniqueId(), loc);
            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim created for this chunk."
                                         : ChatColor.RED + "Already claimed or you reached your limit."));
        } else if (slot == 13) { // Info
            plots.getClaim(loc).ifPresentOrElse(c -> {
                String owner = plots.ownerName(c.getOwner());
                var trusted = plots.listTrusted(loc);
                p.sendMessage(prefix() + ChatColor.AQUA + "Owner: " + owner);
                p.sendMessage(prefix() + ChatColor.AQUA + "Trusted: " + (trusted.isEmpty() ? "(none)" : String.join(", ", trusted)));
            }, () -> p.sendMessage(prefix() + ChatColor.GRAY + "No claim in this chunk."));
        } else if (slot == 15) { // Remove
            boolean ok = plots.removeClaim(p.getUniqueId(), loc, false);
            p.sendMessage(prefix() + (ok ? ChatColor.GREEN + "Claim removed."
                                         : ChatColor.RED + "No claim here or you are not the owner."));
        }
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&',
                com.snazzyatoms.proshield.ProShield.getInstance().getConfig().getString("messages.prefix", "&3[ProShield]&r "));
    }
}
