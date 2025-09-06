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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final PlotManager plotManager;
    private final GUIManager guiManager;

    // 🔧 FIX: expect both managers
    public GUIListener(PlotManager plotManager, GUIManager guiManager) {
        this.plotManager = plotManager;
        this.guiManager = guiManager;
    }

    // Right-click compass opens menu
    @EventHandler
    public void onCompassUse(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        ItemStack it = e.getItem();
        if (it.getType() != Material.COMPASS || !it.hasItemMeta()) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName());
        if (name == null) return;

        if (name.equalsIgnoreCase("ProShield Compass")) {
            e.setCancelled(true);
            guiManager.openMain(e.getPlayer());
        }
    }

    // Handle menu clicks
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        if (inv == null || inv.getHolder() != null) return; // only our simple GUI

        if (!inv.getTitle().contains("ProShield Menu")) return; // Paper <1.20 API; for newer use view.title()
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null) return;
        Material type = clicked.getType();
        Location loc = p.getLocation();

        switch (type) {
            case GRASS_BLOCK: // Create Claim
                if (plotManager.createClaim(p.getUniqueId(), loc)) {
                    p.sendMessage(ChatColor.GREEN + "Chunk claimed!");
                    ProShield.getInstance().getBorderVisualizer().showChunkBorder(p, loc);
                } else {
                    p.sendMessage(ChatColor.RED + "Cannot claim here (already claimed or limit reached).");
                }
                p.closeInventory();
                break;

            case PAPER: // Claim Info
                plotManager.getClaim(loc).ifPresentOrElse(c -> {
                    p.sendMessage(ChatColor.GOLD + "Owner: " + plotManager.ownerName(c.getOwner()));
                    p.sendMessage(ChatColor.GOLD + "World: " + c.getWorld() + " Chunk: " + c.getChunkX() + "," + c.getChunkZ());
                    p.sendMessage(ChatColor.GOLD + "Trusted: " +
                            (c.getTrusted().isEmpty() ? "(none)" : c.getTrusted().size() + " player(s)"));
                    ProShield.getInstance().getBorderVisualizer().showChunkBorder(p, loc);
                }, () -> p.sendMessage(ChatColor.GRAY + "This chunk is not claimed."));
                break;

            case BARRIER: // Remove Claim
                // 🔧 FIX: new PlotManager signature requires adminForce boolean
                if (plotManager.removeClaim(p.getUniqueId(), loc, false)) {
                    p.sendMessage(ChatColor.YELLOW + "Chunk unclaimed.");
                } else {
                    p.sendMessage(ChatColor.RED + "You don't own this claim.");
                }
                p.closeInventory();
                break;

            default:
                break;
        }
    }
}
