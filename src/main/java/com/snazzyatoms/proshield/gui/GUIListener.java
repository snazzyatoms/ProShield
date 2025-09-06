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
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final PlotManager plotManager;
    private final GUIManager guiManager;

    public GUIListener(PlotManager plotManager, GUIManager guiManager) {
        this.plotManager = plotManager;
        this.guiManager = guiManager;
    }

    // Right-click ProShield compass opens the menu
    @EventHandler
    public void onCompassUse(PlayerInteractEvent e) {
        ItemStack it = e.getItem();
        if (it == null || it.getType() != Material.COMPASS || !it.hasItemMeta()) return;
        String name = it.getItemMeta().getDisplayName();
        if (name == null) return;

        if (ChatColor.stripColor(name).equalsIgnoreCase("ProShield Compass")) {
            e.setCancelled(true);
            guiManager.openMainGUI(e.getPlayer()); // ✅ correct method name
        }
    }

    // Handle clicks in the ProShield GUI
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        // ✅ use the view title (works across supported API versions)
        String title = e.getView().getTitle();
        if (!title.equals(ChatColor.DARK_GREEN + "ProShield Menu")) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

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
                    p.sendMessage(ChatColor.GOLD + "World: " + c.getWorld() + "  Chunk: " + c.getChunkX() + "," + c.get
